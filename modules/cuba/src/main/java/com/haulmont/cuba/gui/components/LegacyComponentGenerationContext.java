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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.components.ComponentGenerationContext;
import io.jmix.ui.components.data.Options;
import io.jmix.ui.components.data.ValueSource;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
public class LegacyComponentGenerationContext extends ComponentGenerationContext {

    @Deprecated
    protected Datasource datasource;
    @Deprecated
    protected CollectionDatasource optionsDatasource;

    /**
     * Creates an instance of ComponentGenerationContext.
     *
     * @param metaClass the entity for which the component is created
     * @param property  the entity attribute for which the component is created
     */
    public LegacyComponentGenerationContext(MetaClass metaClass, String property) {
        super(metaClass, property);
    }

    /**
     * @return a datasource that can be used to create the component
     * @deprecated Use {@link #getValueSource()} instead
     */
    @Deprecated
    @Nullable
    public Datasource getDatasource() {
        return datasource;
    }

    /**
     * Sets a datasource, using fluent API method.
     *
     * @param datasource a datasource
     * @return this object
     * @deprecated Use {@link #setValueSource(ValueSource)} instead
     */

    @Deprecated
    public LegacyComponentGenerationContext setDatasource(Datasource datasource) {
        this.datasource = datasource;
        return this;
    }

    /**
     * @return a datasource that can be used to show options
     * @deprecated Use {@link #getOptions()} instead
     */
    @Deprecated
    @Nullable
    public CollectionDatasource getOptionsDatasource() {
        return optionsDatasource;
    }

    /**
     * Sets a datasource that can be used to show options, using fluent API method.
     *
     * @param optionsDatasource a datasource that can be used as optional to create the component
     * @return this object
     * @deprecated Use {@link #setOptions(Options)} instead
     */
    @Deprecated
    public LegacyComponentGenerationContext setOptionsDatasource(CollectionDatasource optionsDatasource) {
        this.optionsDatasource = optionsDatasource;
        return this;
    }
}
