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

package io.jmix.ui.model.impl;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.entity.Entity;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.InstancePropertyContainer;
import org.springframework.context.ApplicationContext;

public class InstancePropertyContainerImpl<E extends Entity>
        extends InstanceContainerImpl<E> implements InstancePropertyContainer<E> {

    protected InstanceContainer master;
    protected String property;

    public InstancePropertyContainerImpl(ApplicationContext applicationContext,
                                         MetaClass metaClass, InstanceContainer master, String property) {
        super(applicationContext, metaClass);
        this.master = master;
        this.property = property;
    }

    @Override
    public InstanceContainer getMaster() {
        return master;
    }

    @Override
    public String getProperty() {
        return property;
    }
}
