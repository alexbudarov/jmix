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

package io.jmix.core;

import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.datatypes.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.Session;

import java.util.List;

/**
 * Central interface to provide metadata-related functionality.
 *
 */
public interface Metadata extends Session {

    String NAME = "jmix_Metadata";

    /**
     * Get Metadata session - interface providing access to MetaClasses and MetaProperties.
     * @return  current metadata session
     */
    Session getSession();

    /**
     * Convenient access to {@link ViewRepository} bean.
     * @return  ViewRepository instance
     * @deprecated Use DI.
     */
    @Deprecated
    ViewRepository getViewRepository();

    /**
     * Convenient access to {@link ExtendedEntities} bean.
     * @return ExtendedEntities instance
     * @deprecated Use DI.
     */
    @Deprecated
    ExtendedEntities getExtendedEntities();

    /**
     * Convenient access to {@link MetadataTools} bean.
     * @return  MetadataTools instance
     * @deprecated Use DI.
     */
    @Deprecated
    MetadataTools getTools();

    /**
     * Convenient access to {@link DatatypeRegistry} bean.
     * @deprecated Use DI.
     */
    @Deprecated
    DatatypeRegistry getDatatypes();

    /**
     * Returns MetaClass of the given entity.
     * @param entity entity instance
     * @return      MetaClass instance
     */
    MetaClass getClass(Entity entity);

    /**
     * Instantiate an entity, taking into account extended entities.
     * @param entityClass   entity class
     * @return              entity instance
     */
    <T extends Entity> T create(Class<T> entityClass);

    /**
     * Instantiate an entity, taking into account extended entities.
     * @param metaClass     entity MetaClass
     * @return              entity instance
     */
    Entity create(MetaClass metaClass);

    /**
     * Instantiate an entity, taking into account extended entities.
     * @param entityName    entity name
     * @return              entity instance
     */
    Entity create(String entityName);

    /**
     * @return list of root packages of all application components and the application itself
     */
    List<String> getRootPackages();
}
