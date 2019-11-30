/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package io.jmix.core.impl.jpql.pointer;

import io.jmix.core.impl.jpql.DomainModel;

public class NoPointer implements Pointer {
    private static final NoPointer instance = new NoPointer();

    private NoPointer() {
    }

    public static Pointer instance() {
        return instance;
    }

    @Override
    public Pointer next(DomainModel model, String field) {
        return this;
    }
}