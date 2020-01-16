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

import io.jmix.core.metamodel.annotations.MetaClass;
import io.jmix.core.entity.annotation.UnavailableInSecurityConstraints;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Base class for entities with Long identifier.
 */
@MappedSuperclass
@MetaClass(name = "sys$BaseLongIdEntity")
@UnavailableInSecurityConstraints
public abstract class BaseLongIdEntity extends BaseGenericIdEntity<Long> {

    private static final long serialVersionUID = 1748237513475338490L;

    @Id
    @Column(name = "ID")
    protected Long id;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
}