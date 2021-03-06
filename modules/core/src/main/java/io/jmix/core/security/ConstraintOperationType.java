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

package io.jmix.core.security;

import io.jmix.core.metamodel.datatypes.impl.EnumClass;

import java.util.Objects;

/**
 * Area of constraint application.
 */
public enum ConstraintOperationType implements EnumClass<String> {

    CREATE("create"),
    READ("read"),
    UPDATE("update"),
    DELETE("delete"),
    ALL("all"),
    CUSTOM("custom");

    private String id;

    ConstraintOperationType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public static ConstraintOperationType fromId(String id) {
        for (ConstraintOperationType area : ConstraintOperationType.values()) {
            if (Objects.equals(id, area.getId()))
                return area;
        }
        return null; // unknown id
    }
}