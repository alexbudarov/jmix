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

package io.jmix.data;

import io.jmix.core.*;
import io.jmix.core.entity.EmbeddableEntity;
import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.model.Instance;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.Basic;
import javax.persistence.FetchType;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

/**
 * Fetches entities by views by accessing reference attributes.
 */
@Component(EntityFetcher.NAME)
public class EntityFetcher {

    public static final String NAME = "cuba_EntityFetcher";

    private static final Logger log = LoggerFactory.getLogger(EntityFetcher.class);

    @Inject
    protected Metadata metadata;

    @Inject
    protected ViewRepository viewRepository;

    @Inject
    protected Persistence persistence;

    @Inject
    protected EntityStates entityStates;

    @Inject
    protected MetadataTools metadataTools;

    /**
     * Fetch instance by view object.
     */
    public void fetch(Entity instance, View view) {
        if (view == null)
            return;
        fetch(instance, view, new HashMap<>(), false);
    }

    /**
     * Fetch instance by view name.
     */
    public void fetch(Entity instance, String viewName) {
        if (viewName == null)
            return;
        View view = viewRepository.getView(instance.getClass(), viewName);
        fetch(instance, view, new HashMap<>(), false);
    }

    /**
     * Fetch instance by view object.
     *
     * @param optimizeForDetached if true, detached objects encountered in the graph will be first checked whether all
     *                            required attributes are already loaded, and reloaded only when needed.
     *                            If the argument is false, all detached objects are reloaded anyway.
     */
    public void fetch(Entity instance, View view, boolean optimizeForDetached) {
        if (view == null)
            return;
        fetch(instance, view, new HashMap<>(), optimizeForDetached);
    }

    /**
     * Fetch instance by view name.
     *
     * @param optimizeForDetached if true, detached objects encountered in the graph will be first checked whether all
     *                            required attributes are already loaded, and reloaded only when needed.
     *                            If the argument is false, all detached objects are reloaded anyway.
     */
    public void fetch(Entity instance, String viewName, boolean optimizeForDetached) {
        if (viewName == null)
            return;
        View view = viewRepository.getView(instance.getClass(), viewName);
        fetch(instance, view, new HashMap<>(), optimizeForDetached);
    }

    protected void fetch(Entity entity, View view, Map<Instance, Set<View>> visited, boolean optimizeForDetached) {
        Set<View> views = visited.get(entity);
        if (views == null) {
            views = new HashSet<>();
            visited.put(entity, views);
        } else if (views.contains(view)) {
            return;
        }
        views.add(view);

        if (log.isTraceEnabled()) log.trace("Fetching instance " + entity);

        MetaClass metaClass = metadata.getClassNN(entity.getClass());
        for (ViewProperty property : view.getProperties()) {
            MetaProperty metaProperty = metaClass.getPropertyNN(property.getName());
            if (!metaProperty.getRange().isClass() && !isLazyFetchedLocalAttribute(metaProperty))
                continue;

            if (log.isTraceEnabled()) log.trace("Fetching property " + property.getName());

            Object value = entity.getValue(property.getName());
            View propertyView = property.getView();
            if (value != null && propertyView != null) {
                if (value instanceof Collection) {
                    for (Object item : ((Collection) value)) {
                        if (item instanceof Entity)
                            fetch((Entity) item, propertyView, visited, optimizeForDetached);
                    }
                } else if (value instanceof Entity) {
                    Entity e = (Entity) value;
                    if (!metaProperty.isReadOnly() && entityStates.isDetached(value) && !(value instanceof EmbeddableEntity)) {
                        if (!optimizeForDetached || needReloading(e, propertyView)) {
                            if (log.isTraceEnabled()) {
                                log.trace("Object " + value + " is detached, loading it");
                            }
                            String storeName = metadataTools.getStoreName(metadata.getClass(e));
                            if (storeName != null) {
                                try (Transaction tx = persistence.getTransaction(storeName)) {
                                    EntityManager em = persistence.getEntityManager(storeName);
                                    @SuppressWarnings("unchecked")
                                    Entity managed = em.find(e.getClass(), e.getId());
                                    if (managed != null) { // the instance here can be null if it has been deleted
                                        entity.setValue(property.getName(), managed);
                                        fetch(managed, propertyView, visited, optimizeForDetached);
                                    }
                                    tx.commit();
                                }
                            }
                        }
                    } else {
                        fetch(e, propertyView, visited, optimizeForDetached);
                    }
                }
            }
        }
    }

    protected boolean needReloading(Entity entity, View view) {
        for (ViewProperty viewProperty : view.getProperties()) {
            if (!entityStates.isLoaded(entity, viewProperty.getName())) {
                return true;
            }
        }
        return false;
    }

    protected boolean isLazyFetchedLocalAttribute(MetaProperty metaProperty) {
        AnnotatedElement annotatedElement = metaProperty.getAnnotatedElement();
        Basic annotation = annotatedElement.getAnnotation(Basic.class);
        return annotation != null && annotation.fetch() == FetchType.LAZY;
    }
}
