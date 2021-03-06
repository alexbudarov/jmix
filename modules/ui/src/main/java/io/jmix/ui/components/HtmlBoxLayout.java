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
package io.jmix.ui.components;

/**
 * A container component with freely designed layout and style. The layout consists of items with textually represented
 * locations. Each item contains one sub-component, which can be any component, such as a layout. The adapter and theme
 * are responsible for rendering the layout with a given style by placing the items in the defined locations.
 */
public interface HtmlBoxLayout extends ComponentContainer, Component.BelongToFrame, Component.HasIcon,
        Component.HasCaption, HasContextHelp, HasHtmlCaption, HasHtmlDescription, HasRequiredIndicator {

    String NAME = "htmlBox";

    /**
     * Return filename of the related HTML template.
     */
    String getTemplateName();

    /**
     * Set filename of the related HTML template inside theme/layouts directory.
     */
    void setTemplateName(String templateName);

    /**
     * @return the contents of the template
     */
    String getTemplateContents();

    /**
     * Set the contents of the template used to draw the custom layout.
     */
    void setTemplateContents(String templateContents);
}