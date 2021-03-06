/*
 * Copyright 2015 John Ahlroos
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.ui.widgets.client.addons.dragdroplayouts.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import io.jmix.ui.widgets.client.addons.dragdroplayouts.ui.interfaces.DDLayoutState;
import io.jmix.ui.widgets.client.addons.dragdroplayouts.ui.interfaces.VDragImageProvider;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorMap;
import com.vaadin.client.Util;
import com.vaadin.shared.Connector;

public class VDDLayoutStateDragImageProvider implements VDragImageProvider {

    private final DDLayoutState state;

    public VDDLayoutStateDragImageProvider(DDLayoutState state) {
        this.state = state;
    }

    @Override
    public Element getDragImageElement(Widget w) {
        ComponentConnector component = Util.findConnectorFor(w);
        Connector dragImage = state.referenceImageComponents.get(component);

        if (dragImage != null) {
            return ConnectorMap.get(component.getConnection())
                    .getElement(dragImage.getConnectorId());
        }

        return null;
    }

}
