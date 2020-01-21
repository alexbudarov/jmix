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

package com.haulmont.cuba.gui.xml.layout.loaders;

import io.jmix.ui.components.Component;
import io.jmix.ui.xml.layout.loaders.AbstractComponentLoader;

import static com.google.common.base.Preconditions.checkState;

public abstract class LegacyComponentLoader<T extends Component>
        extends AbstractComponentLoader<T> {

    @Override
    protected LegacyComponentLoaderContext getComponentContext() {
        checkState(context instanceof LegacyComponentLoaderContext,
            "'context' must implement io.jmix.ui.xml.layout.ComponentLoader.ComponentContext");

        return (LegacyComponentLoaderContext) context;
    }
}
