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

package io.jmix.ui.components.impl;

import io.jmix.ui.components.LinkButton;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

public class WebLinkButton extends WebButton implements LinkButton {

    public WebLinkButton() {
        component.addStyleName(ValoTheme.BUTTON_LINK);
    }

    @Override
    public void setStyleName(String name) {
        super.setStyleName(name);
        component.addStyleName(ValoTheme.BUTTON_LINK);
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(ValoTheme.BUTTON_LINK, ""));
    }
}