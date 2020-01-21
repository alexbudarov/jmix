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

package com.haulmont.cuba.gui.xml.layout;

import com.haulmont.cuba.gui.components.BulkEditor;
import com.haulmont.cuba.gui.components.FieldGroup;
import com.haulmont.cuba.gui.xml.layout.loaders.BulkEditorLoader;
import com.haulmont.cuba.gui.xml.layout.loaders.FieldGroupLoader;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.LoaderConfig;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("rawtypes")
@Component(LegacyLoaderConfig.NAME)
public class LegacyLoaderConfig implements LoaderConfig {

    public static final String NAME = "cuba_LegacyLoaderConfig";

    protected Map<String, Class<? extends ComponentLoader>> loaders = new ConcurrentHashMap<>();

    public LegacyLoaderConfig() {
        initStandardLoaders();
    }

    @Override
    public boolean supports(Element element) {
        return loaders.containsKey(element.getName());
    }

    @Override
    public Class<? extends ComponentLoader> getLoader(Element element) {
        return null;
    }

    protected void initStandardLoaders() {
        loaders.put(FieldGroup.NAME, FieldGroupLoader.class);
        loaders.put(BulkEditor.NAME, BulkEditorLoader.class);
    }
}
