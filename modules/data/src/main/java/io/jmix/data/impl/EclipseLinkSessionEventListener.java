/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.data.impl;

import com.google.common.base.Strings;
import io.jmix.core.AppBeans;
import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.core.entity.Entity;
import io.jmix.core.entity.SoftDelete;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.persistence.annotations.CacheCoordinationType;
import org.eclipse.persistence.config.CacheIsolationType;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorEventListener;
import org.eclipse.persistence.descriptors.InheritancePolicy;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.platform.database.*;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventAdapter;
import org.springframework.core.env.Environment;

import javax.persistence.OneToOne;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EclipseLinkSessionEventListener extends SessionEventAdapter {

    private Environment environment = AppBeans.get(Environment.class);

    private Metadata metadata = AppBeans.get(Metadata.NAME);

    private EntityStates entityStates = AppBeans.get(EntityStates.NAME);

    private DescriptorEventListener descriptorEventListener = AppBeans.get(EclipseLinkDescriptorEventListener.NAME);

    @Override
    public void preLogin(SessionEvent event) {

        Session session = event.getSession();
        setPrintInnerJoinOnClause(session);

        Map<Class, ClassDescriptor> descriptorMap = session.getDescriptors();
        boolean hasMultipleTableConstraintDependency = hasMultipleTableConstraintDependency();
        for (Map.Entry<Class, ClassDescriptor> entry : descriptorMap.entrySet()) {
            MetaClass metaClass = metadata.getSession().getClassNN(entry.getKey());
            ClassDescriptor desc = entry.getValue();

            setCacheable(metaClass, desc, session);

            if (hasMultipleTableConstraintDependency) {
                setMultipleTableConstraintDependency(metaClass, desc);
            }

            if (Entity.class.isAssignableFrom(desc.getJavaClass())) {
                // set DescriptorEventManager that doesn't invoke listeners for base classes
                desc.setEventManager(new DescriptorEventManagerWrapper(desc.getDescriptorEventManager()));
                desc.getEventManager().addListener(descriptorEventListener);
            }

            if (SoftDelete.class.isAssignableFrom(desc.getJavaClass())) {
                desc.getQueryManager().setAdditionalCriteria("this.deleteTs is null");
                desc.setDeletePredicate(entity -> entity instanceof SoftDelete &&
                        entityStates.isLoaded(entity, "deleteTs") &&
                        ((SoftDelete) entity).isDeleted());
            }

            List<DatabaseMapping> mappings = desc.getMappings();
            for (DatabaseMapping mapping : mappings) {
                // support UUID
                String attributeName = mapping.getAttributeName();
                MetaProperty metaProperty = metaClass.getPropertyNN(attributeName);
                if (metaProperty.getRange().isDatatype()) {
                    if (metaProperty.getJavaType().equals(UUID.class)) {
                        ((DirectToFieldMapping) mapping).setConverter(UuidConverter.getInstance());
                        setDatabaseFieldParameters(session, mapping.getField());
                    }
                } else if (metaProperty.getRange().isClass() && !metaProperty.getRange().getCardinality().isMany()) {
                    MetaClass refMetaClass = metaProperty.getRange().asClass();
                    MetaProperty refPkProperty = metadata.getTools().getPrimaryKeyProperty(refMetaClass);
                    if (refPkProperty != null && refPkProperty.getJavaType().equals(UUID.class)) {
                        for (DatabaseField field : ((OneToOneMapping) mapping).getForeignKeyFields()) {
                            setDatabaseFieldParameters(session, field);
                        }
                    }
                }
                // embedded attributes
                if (mapping instanceof AggregateObjectMapping) {
                    EmbeddedParameters embeddedParameters =
                            metaProperty.getAnnotatedElement().getAnnotation(EmbeddedParameters.class);
                    if (embeddedParameters != null && !embeddedParameters.nullAllowed())
                        ((AggregateObjectMapping) mapping).setIsNullAllowed(false);
                }

                if (mapping.isOneToManyMapping()) {
                    OneToManyMapping oneToManyMapping = (OneToManyMapping) mapping;
                    if (SoftDelete.class.isAssignableFrom(oneToManyMapping.getReferenceClass())) {
                        oneToManyMapping.setAdditionalJoinCriteria(new ExpressionBuilder().get("deleteTs").isNull());
                    }
                }

                if (mapping.isOneToOneMapping()) {
                    OneToOneMapping oneToOneMapping = (OneToOneMapping) mapping;
                    if (SoftDelete.class.isAssignableFrom(oneToOneMapping.getReferenceClass())) {
                        if (mapping.isManyToOneMapping()) {
                            oneToOneMapping.setSoftDeletionForBatch(false);
                            oneToOneMapping.setSoftDeletionForValueHolder(false);
                        } else {
                            OneToOne oneToOne = metaProperty.getAnnotatedElement().getAnnotation(OneToOne.class);
                            if (oneToOne != null) {
                                if (Strings.isNullOrEmpty(oneToOne.mappedBy())) {
                                    oneToOneMapping.setSoftDeletionForBatch(false);
                                    oneToOneMapping.setSoftDeletionForValueHolder(false);
                                } else {
                                    oneToOneMapping.setAdditionalJoinCriteria(
                                            new ExpressionBuilder().get("deleteTs").isNull());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void setCacheable(MetaClass metaClass, ClassDescriptor desc, Session session) {
        String property = (String) session.getProperty("eclipselink.cache.shared.default");
        boolean defaultCache = property == null || Boolean.valueOf(property);

        if ((defaultCache && !desc.isIsolated())
                || desc.getCacheIsolation() == CacheIsolationType.SHARED
                || desc.getCacheIsolation() == CacheIsolationType.PROTECTED) {
            metaClass.getAnnotations().put("cacheable", true);
            desc.getCachePolicy().setCacheCoordinationType(CacheCoordinationType.INVALIDATE_CHANGED_OBJECTS);
        }
    }

    private void setMultipleTableConstraintDependency(MetaClass metaClass, ClassDescriptor desc) {
        InheritancePolicy policy = desc.getInheritancePolicyOrNull();
        if (policy != null && policy.isJoinedStrategy() && policy.getParentClass() != null) {
            boolean hasOneToMany = metaClass.getOwnProperties().stream().anyMatch(metaProperty ->
                    metadata.getTools().isPersistent(metaProperty)
                            && metaProperty.getRange().isClass()
                            && metaProperty.getRange().getCardinality() == Range.Cardinality.ONE_TO_MANY);
            if (hasOneToMany) {
                desc.setHasMultipleTableConstraintDependecy(true);
            }
        }
    }

    private boolean hasMultipleTableConstraintDependency() {
        String value = environment.getProperty("cuba.hasMultipleTableConstraintDependency");
        return value == null || BooleanUtils.toBoolean(value);
    }

    private void setDatabaseFieldParameters(Session session, DatabaseField field) {
        if (session.getPlatform() instanceof PostgreSQLPlatform) {
            field.setSqlType(Types.OTHER);
            field.setType(UUID.class);
            field.setColumnDefinition("UUID");
        } else if (session.getPlatform() instanceof MySQLPlatform) {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
            field.setColumnDefinition("varchar(32)");
        } else if (session.getPlatform() instanceof HSQLPlatform) {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
            field.setColumnDefinition("varchar(36)");
        } else if (session.getPlatform() instanceof SQLServerPlatform) {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
            field.setColumnDefinition("uniqueidentifier");
        } else if (session.getPlatform() instanceof OraclePlatform) {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
            field.setColumnDefinition("varchar2(32)");
        } else {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
        }
    }

    private void setPrintInnerJoinOnClause(Session session) {
        boolean useInnerJoinOnClause = BooleanUtils.toBoolean(
                environment.getProperty("cuba.useInnerJoinOnClause"));
        session.getPlatform().setPrintInnerJoinInWhereClause(!useInnerJoinOnClause);
    }
}