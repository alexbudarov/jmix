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

package com.haulmont.cuba.gui.data.impl;

import io.jmix.core.View;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.keyvalue.KeyValueMetaClass;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.logging.UIPerformanceLogger;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.GroupDatasource;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * {@link GroupDatasource} that supports {@link KeyValueEntity}.
 */
public class ValueGroupDatasourceImpl
        extends GroupDatasourceImpl<KeyValueEntity, Object>
        implements ValueDatasource {

    protected final ValueDatasourceDelegate delegate;

    public ValueGroupDatasourceImpl() {
        delegate = new ValueDatasourceDelegate(this);
    }

    @Override
    public void setup(DsContext dsContext, DataSupplier dataSupplier, String id, MetaClass metaClass, @Nullable View view) {
        this.id = id;
        this.dsContext = dsContext;
        this.dataSupplier = dataSupplier;
        this.metaClass = new KeyValueMetaClass();
    }

    @Override
    public ValueGroupDatasourceImpl setIdName(String name) {
        delegate.setIdName(name);
        return this;
    }

    public ValueGroupDatasourceImpl addProperty(String name) {
        delegate.addProperty(name);
        return this;
    }

    public ValueGroupDatasourceImpl addProperty(String name, Class aClass) {
        delegate.addProperty(name, aClass);
        return this;
    }

    public ValueGroupDatasourceImpl addProperty(String name, Datatype datatype) {
        delegate.addProperty(name, datatype);
        return this;
    }

    @Override
    protected void loadData(Map<String, Object> params) {
        String tag = getLoggingTag("VGDS");
        StopWatch sw = new Slf4JStopWatch(tag, LoggerFactory.getLogger(UIPerformanceLogger.class));

        delegate.loadData(params);

        sw.stop();
    }

    @Override
    public void includeItem(KeyValueEntity item) {
        super.includeItem(item);
        item.setInstanceMetaClass(metaClass);
    }

    @Override
    public void addItem(KeyValueEntity item) {
        super.addItem(item);
        item.setInstanceMetaClass(metaClass);
    }

    public void setStoreName(String storeName) {
        this.delegate.setStoreName(storeName);
    }
}
