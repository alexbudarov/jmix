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
package io.jmix.core.entity;

import io.jmix.core.entity.annotation.UnavailableInSecurityConstraints;
import io.jmix.core.metamodel.model.impl.AbstractInstance;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.IOException;
import java.util.Objects;

import static io.jmix.core.entity.BaseEntityInternalAccess.*;

/**
 * Base class for entities.
 * <br>
 * When choosing a base class for your entity, consider more specific base classes defining the primary key type:
 * <ul>
 * <li>{@link BaseUuidEntity}</li>
 * <li>{@link BaseLongIdEntity}</li>
 * <li>{@link BaseIntegerIdEntity}</li>
 * <li>{@link BaseStringIdEntity}</li>
 * </ul>
 * or most commonly used {@link StandardEntity}.
 */
@MappedSuperclass
@io.jmix.core.metamodel.annotations.MetaClass(name = "sys$BaseGenericIdEntity")
@UnavailableInSecurityConstraints
public abstract class BaseGenericIdEntity<T> extends AbstractInstance implements Entity<T> {

    private static final long serialVersionUID = -8400641366148656528L;

    @Transient
    protected byte __state = BaseEntityInternalAccess.NEW;

    @Transient
    protected SecurityState __securityState;

    // todo dynamic attributes
//    @Transient
//    protected Map<String, CategoryAttributeValue> dynamicAttributes = null;

    public abstract void setId(T id);

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        if (isManaged(this)) {
            setManaged(this, false);
            setDetached(this, true);
        }
        out.defaultWriteObject();
    }

    /**
     * Copies the state which is not related to attributes registered in metadata.
     * Override this method in subclasses that have additional state which must be preserved when creating copies.
     */
    @SuppressWarnings("unchecked")
    protected void copySystemState(BaseGenericIdEntity src) {
        __state = src.__state;
        __securityState = src.__securityState;
        // todo dynamic attributes
//        dynamicAttributes = src.dynamicAttributes;
    }

    // todo dynamic attributes
//    @Override
//    public void setValue(String property, Object newValue, boolean checkEquals) {
//        if (DynamicAttributesUtils.isDynamicAttribute(property)) {
//            Preconditions.checkState(dynamicAttributes != null, "Dynamic attributes should be loaded explicitly");
//            String attributeCode = DynamicAttributesUtils.decodeAttributeCode(property);
//            CategoryAttributeValue categoryAttributeValue = dynamicAttributes.get(attributeCode);
//            Object oldValue = categoryAttributeValue != null ? categoryAttributeValue.getValue() : null;
//
//            if (newValue == null) {
//                if (categoryAttributeValue != null) {
//                    categoryAttributeValue.setValue(null);
//                    categoryAttributeValue.setDeleteTs(AppBeans.get(TimeSource.class).currentTimestamp());
//                    propertyChanged(property, oldValue, null);
//                }
//            } else if (!Objects.equals(oldValue, newValue)) {
//                if (categoryAttributeValue != null) {
//                    categoryAttributeValue.setValue(newValue);
//                    categoryAttributeValue.setDeleteTs(null);
//                } else {
//                    Metadata metadata = AppBeans.get(Metadata.NAME);
//                    ReferenceToEntitySupport referenceToEntitySupport = AppBeans.get(ReferenceToEntitySupport.class);
//
//                    categoryAttributeValue = metadata.create(CategoryAttributeValue.class);
//                    categoryAttributeValue.setValue(newValue);
//                    categoryAttributeValue.setObjectEntityId(referenceToEntitySupport.getReferenceId(this));
//                    categoryAttributeValue.setCode(attributeCode);
//                    DynamicAttributes dynamicAttributesBean = AppBeans.get(DynamicAttributes.NAME);
//                    categoryAttributeValue.setCategoryAttribute(
//                            dynamicAttributesBean.getAttributeForMetaClass(getMetaClass(), attributeCode));
//                    dynamicAttributes.put(attributeCode, categoryAttributeValue);
//                }
//                propertyChanged(property, oldValue, newValue);
//            }
//        } else {
//            super.setValue(property, newValue, checkEquals);
//        }
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public <V> V getValue(String property) {
//        if (DynamicAttributesUtils.isDynamicAttribute(property)) {
//            if (PersistenceHelper.isNew(this) && dynamicAttributes == null) {
//                dynamicAttributes = new HashMap<>();
//            }
//
//            Preconditions.checkState(dynamicAttributes != null, "Dynamic attributes should be loaded explicitly");
//            CategoryAttributeValue categoryAttributeValue = dynamicAttributes.get(DynamicAttributesUtils.decodeAttributeCode(property));
//            if (categoryAttributeValue != null) {
//                return (V) categoryAttributeValue.getValue();
//            } else {
//                return null;
//            }
//        } else {
//            return super.getValue(property);
//        }
//    }
//
//    public void setDynamicAttributes(Map<String, CategoryAttributeValue> dynamicAttributes) {
//        this.dynamicAttributes = dynamicAttributes;
//    }
//
//    @Nullable
//    public Map<String, CategoryAttributeValue> getDynamicAttributes() {
//        return dynamicAttributes;
//    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;

        if (other == null || getClass() != other.getClass())
            return false;

        return Objects.equals(getId(), ((BaseGenericIdEntity) other).getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        String state = "";
        if (isNew(this))
            state += "new,";
        if (isManaged(this))
            state += "managed,";
        if (isDetached(this))
            state += "detached,";
        if (isRemoved(this))
            state += "removed,";
        if (state.length() > 0)
            state = state.substring(0, state.length() - 1);
        return getClass().getName() + "-" + getId() + " [" + state + "]";
    }
}