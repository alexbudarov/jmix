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
package io.jmix.ui.widgets.client.addons.dragdroplayouts.ui.horizontalsplitpanel;

import io.jmix.ui.widgets.client.addons.dragdroplayouts.ui.Constants;
import io.jmix.ui.widgets.client.addons.dragdroplayouts.ui.VDDAbstractDropHandler;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ui.dd.VAcceptCallback;
import com.vaadin.client.ui.dd.VDragEvent;

public class VDDHorizontalSplitPanelDropHandler
        extends VDDAbstractDropHandler<VDDHorizontalSplitPanel> {

    public VDDHorizontalSplitPanelDropHandler(ComponentConnector connector) {
        super(connector);
    }

    @Override
    protected void dragAccepted(VDragEvent drag) {
        dragOver(drag);
    }

    @Override
    public boolean drop(VDragEvent drag) {

        // Un-emphasis any selections
        getLayout().deEmphasis();

        // Update the details
        getLayout().updateDragDetails(drag);
        return getLayout().postDropHook(drag) && super.drop(drag);
    };

    @Override
    public void dragOver(VDragEvent drag) {

        getLayout().deEmphasis();

        getLayout().updateDragDetails(drag);

        getLayout().postOverHook(drag);

        ComponentConnector widgetConnector = (ComponentConnector) drag
                .getTransferable()
                .getData(Constants.TRANSFERABLE_DETAIL_COMPONENT);

        if (widgetConnector != null
                && getLayout().equals(widgetConnector.getWidget())) {
            return;
        }

        // Validate the drop
        validate(new VAcceptCallback() {
            public void accepted(VDragEvent event) {
                getLayout().emphasis(event.getElementOver());
            }
        }, drag);
    };

    @Override
    public void dragEnter(VDragEvent drag) {
        super.dragEnter(drag);
        getLayout().updateDragDetails(drag);
        getLayout().postEnterHook(drag);
    }

    @Override
    public void dragLeave(VDragEvent drag) {
        getLayout().deEmphasis();
        getLayout().updateDragDetails(drag);
        getLayout().postLeaveHook(drag);
    };
}
