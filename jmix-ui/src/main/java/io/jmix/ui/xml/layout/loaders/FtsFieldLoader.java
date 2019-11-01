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

package io.jmix.ui.xml.layout.loaders;

import com.haulmont.cuba.gui.components.mainwindow.FtsField;

public class FtsFieldLoader extends AbstractComponentLoader<FtsField> {
    @Override
    public void createComponent() {
        resultComponent = factory.create(FtsField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        loadStyleName(resultComponent, element);
        loadAlign(resultComponent, element);

        loadIcon(resultComponent, element);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);

        loadEnable(resultComponent, element);
        loadVisible(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);
    }
}