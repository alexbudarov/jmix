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

package io.jmix.core.impl.keyvalue;

import io.jmix.core.AppBeans;
import io.jmix.core.Metadata;
import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.Datatypes;
import io.jmix.core.metamodel.datatypes.impl.EnumClass;
import io.jmix.core.metamodel.datatypes.impl.EnumerationImpl;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.metamodel.model.Session;
import io.jmix.core.metamodel.model.impl.ClassRange;
import io.jmix.core.metamodel.model.impl.DatatypeRange;
import io.jmix.core.metamodel.model.impl.EnumerationRange;
import io.jmix.core.metamodel.model.impl.MetadataObjectImpl;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * MetaProperty for {@link io.jmix.core.entity.KeyValueEntity}.
 */
public class KeyValueMetaProperty extends MetadataObjectImpl implements MetaProperty {

    private static final long serialVersionUID = 839160118855669248L;

    protected final MetaClass metaClass;
    protected final transient Range range;
    protected final Class javaClass;
    protected final Boolean mandatory;
    protected final AnnotatedElement annotatedElement = new FakeAnnotatedElement();
    protected final Type type;

    public KeyValueMetaProperty(MetaClass metaClass, String name, Class javaClass) {
        this.name = name;
        this.javaClass = javaClass;
        this.metaClass = metaClass;
        this.mandatory = false;

        Metadata metadata = AppBeans.get(Metadata.NAME);
        Session metadataSession = metadata.getSession();
        if (Entity.class.isAssignableFrom(javaClass)) {
            range = new ClassRange(metadataSession.getClass(javaClass));
            this.type = Type.ASSOCIATION;
        } else if (EnumClass.class.isAssignableFrom(javaClass)) {
            @SuppressWarnings("unchecked")
            EnumerationImpl enumeration = new EnumerationImpl(javaClass);
            this.range = new EnumerationRange(enumeration);
            this.type = Type.ENUM;
        } else {
            @SuppressWarnings("unchecked")
            Datatype datatype = Datatypes.getNN(javaClass);

            this.range = new DatatypeRange(datatype);
            this.type = Type.DATATYPE;
        }
    }

    public KeyValueMetaProperty(MetaClass metaClass, String name, Datatype datatype) {
        this.name = name;
        this.javaClass = datatype.getJavaClass();
        this.metaClass = metaClass;
        this.mandatory = false;

        this.range = new DatatypeRange(datatype);
        this.type = Type.DATATYPE;
    }

    @Override
    public Session getSession() {
        return metaClass.getSession();
    }

    @Override
    public MetaClass getDomain() {
        return metaClass;
    }

    @Override
    public Range getRange() {
        return range;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isMandatory() {
        return Boolean.TRUE.equals(mandatory);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public MetaProperty getInverse() {
        return null;
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return annotatedElement;
    }

    @Override
    public Class<?> getJavaType() {
        return javaClass;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return null;
    }

    protected static class FakeAnnotatedElement implements AnnotatedElement, Serializable {

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return false;
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return null;
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyValueMetaProperty)) return false;

        KeyValueMetaProperty that = (KeyValueMetaProperty) o;

        return metaClass.equals(that.metaClass) && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return 31 * metaClass.hashCode() + name.hashCode();
    }
}
