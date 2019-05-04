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

package io.jmix.core.impl;

import io.jmix.core.JmixModuleDescriptor;
import io.jmix.core.JmixModules;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ResourceLoader;

import java.util.ListIterator;

public class JmixMessageSource extends ReloadableResourceBundleMessageSource {

    private ResourceLoader resourceLoader;

    public JmixMessageSource(JmixModules modules, ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        setDefaultEncoding("UTF-8");
        setResourceLoader(resourceLoader);

        ListIterator<JmixModuleDescriptor> iterator = modules.getComponents().listIterator(modules.getComponents().size());
        while (iterator.hasPrevious()) {
            JmixModuleDescriptor module = iterator.previous();
            addBasenames(addModuleBasename(module.getId()));
        }
    }

    protected String addModuleBasename(String moduleId) {
        return moduleId.replace('.', '/') + "/messages";
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        // Always use the resource loader provided in constructor. Otherwise it gets overridden by ApplicationContext.
        super.setResourceLoader(this.resourceLoader);
    }
}
