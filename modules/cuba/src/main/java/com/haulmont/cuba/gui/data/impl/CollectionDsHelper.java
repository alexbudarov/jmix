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

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.PropertyDatasource;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CollectionDsHelper {

    public static List<MetaPropertyPath> createProperties(View view, MetaClass metaClass) {
        List<MetaPropertyPath> properties = new ArrayList<>();
        MetadataTools metadataTools = AppBeans.get(MetadataTools.NAME);

        if (view != null && metadataTools.isPersistent(metaClass)) {
            for (ViewProperty property : view.getProperties()) {
                final String name = property.getName();

                final MetaProperty metaProperty = metaClass.getProperty(name);
                if (metaProperty == null) {
                    String message = String.format("Unable to find property %s for entity %s", name, metaClass.getName());
                    throw new DevelopmentException(message);
                }

                if (!metadataTools.isPersistent(metaProperty)) {
                    String message = String.format(
                            "Specified transient property %s in view for datasource with persistent entity %s",
                            name, metaClass.getName());

                    LoggerFactory.getLogger(CollectionDsHelper.class).warn(message);
                    continue;
                }

                final Range range = metaProperty.getRange();
                if (range == null) {
                    continue;
                }

                final Range.Cardinality cardinality = range.getCardinality();
                if (!cardinality.isMany()) {
                    properties.add(new MetaPropertyPath(metaClass, metaProperty));
                }
            }

            // add all non-persistent properties
            for (MetaProperty metaProperty : metaClass.getProperties()) {
                if (metadataTools.isNotPersistent(metaProperty)) {
                    properties.add(new MetaPropertyPath(metaClass, metaProperty));
                }
            }
        } else {
            if (view != null) {
                LoggerFactory.getLogger(CollectionDsHelper.class).
                        warn("Specified view {} for datasource with not persistent entity {}",
                                view.getName(), metaClass.getName());
            }

            for (MetaProperty metaProperty : metaClass.getProperties()) {
                final Range range = metaProperty.getRange();
                if (range == null) continue;

                final Range.Cardinality cardinality = range.getCardinality();
                if (!cardinality.isMany()) {
                    properties.add(new MetaPropertyPath(metaClass, metaProperty));
                }
            }
        }

        return properties;
    }

    public static void autoRefreshInvalid(CollectionDatasource datasource, boolean autoRefresh) {
        if (datasource instanceof PropertyDatasource) {
            return;
        }
        if (autoRefresh && Datasource.State.INVALID.equals(datasource.getState())) {
            DsContext dsContext = datasource.getDsContext();
            if (dsContext == null || !WindowParams.DISABLE_AUTO_REFRESH.getBool(dsContext.getFrameContext())) {
                if (datasource instanceof CollectionDatasource.Suspendable)
                    ((CollectionDatasource.Suspendable) datasource).refreshIfNotSuspended();
                else
                    datasource.refresh();
            }
        }
    }
}
