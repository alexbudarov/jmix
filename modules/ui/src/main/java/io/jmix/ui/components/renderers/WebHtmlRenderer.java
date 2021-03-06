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

package io.jmix.ui.components.renderers;

import com.vaadin.ui.renderers.HtmlRenderer;
import io.jmix.core.entity.Entity;
import io.jmix.ui.components.DataGrid;
import io.jmix.ui.components.impl.WebAbstractDataGrid;

/**
 * A renderer for presenting HTML content.
 */
public class WebHtmlRenderer extends WebAbstractDataGrid.AbstractRenderer<Entity, String> implements DataGrid.HtmlRenderer {

    public WebHtmlRenderer() {
        this("");
    }

    public WebHtmlRenderer(String nullRepresentation) {
        super(nullRepresentation);
    }

    @Override
    public HtmlRenderer getImplementation() {
        return (HtmlRenderer) super.getImplementation();
    }

    @Override
    protected HtmlRenderer createImplementation() {
        return new HtmlRenderer(getNullRepresentation());
    }

    @Override
    public String getNullRepresentation() {
        return super.getNullRepresentation();
    }

    @Override
    public void setNullRepresentation(String nullRepresentation) {
        super.setNullRepresentation(nullRepresentation);
    }
}
