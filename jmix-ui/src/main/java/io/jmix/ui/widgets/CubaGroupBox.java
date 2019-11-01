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

package io.jmix.ui.widgets;

import com.haulmont.cuba.web.widgets.client.groupbox.CubaGroupBoxServerRpc;
import com.haulmont.cuba.web.widgets.client.groupbox.CubaGroupBoxState;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class CubaGroupBox extends Panel implements ComponentContainer {
    protected ExpandChangeHandler expandChangeHandler = null;

    public CubaGroupBox() {
        registerRpc((CubaGroupBoxServerRpc) expanded -> {
            if (getState().collapsable) {
                setExpanded(expanded, true);
            }
        });

        Layout content = new CubaVerticalActionsLayout();
        setContent(content);

        setWidth(100, Unit.PERCENTAGE);
    }

    @Override
    protected CubaGroupBoxState getState() {
        return (CubaGroupBoxState) super.getState();
    }

    @Override
    protected CubaGroupBoxState getState(boolean markAsDirty) {
        return (CubaGroupBoxState) super.getState(markAsDirty);
    }

    @Override
    public void setWidth(float width, Unit unit) {
        super.setWidth(width, unit);

        if (getContent() != null) {
            if (width < 0) {
                getContent().setWidth(-1, Unit.PIXELS);
            } else {
                getContent().setWidth(100, Unit.PERCENTAGE);
            }
        }
    }

    @Override
    public void setHeight(float height, Unit unit) {
        super.setHeight(height, unit);

        if (getContent() != null) {
            if (height < 0) {
                getContent().setHeight(-1, Unit.PIXELS);
            } else {
                getContent().setHeight(100, Unit.PERCENTAGE);
            }
        }
    }

    @Override
    public void setContent(Component content) {
        super.setContent(content);

        if (content != null) {
            if (getHeight() < 0) {
                getContent().setHeight(-1, Unit.PIXELS);
            } else {
                getContent().setHeight(100, Unit.PERCENTAGE);
            }

            if (getWidth() < 0) {
                getContent().setWidth(-1, Unit.PIXELS);
            } else {
                getContent().setWidth(100, Unit.PERCENTAGE);
            }
        }
    }

    public boolean isExpanded() {
        return !getState(false).collapsable || getState(false).expanded;
    }

    public void setExpanded(boolean expanded) {
        setExpanded(expanded, false);
    }

    public void setExpanded(boolean expanded, boolean invokedByUser) {
        if (expanded != getState(false).expanded) {
            getContent().setVisible(expanded);
            markAsDirtyRecursive();
        }

        getState().expanded = expanded;
        if (expandChangeHandler != null)
            expandChangeHandler.expandStateChanged(expanded, invokedByUser);
    }

    public boolean isCollapsable() {
        return getState(false).collapsable;
    }

    public void setCollapsable(boolean collapsable) {
        getState().collapsable = collapsable;
        if (collapsable)
            setExpanded(true);
    }

    public ExpandChangeHandler getExpandChangeHandler() {
        return expandChangeHandler;
    }

    public void setExpandChangeHandler(ExpandChangeHandler expandChangeHandler) {
        this.expandChangeHandler = expandChangeHandler;
    }

    @Override
    public ComponentContainer getContent() {
        return (ComponentContainer) super.getContent();
    }

    @Override
    public void addComponent(Component c) {
        getContent().addComponent(c);
    }

    @Override
    public void addComponents(Component... components) {
        getContent().addComponents(components);
    }

    @Override
    public void removeComponent(Component c) {
        getContent().addComponent(c);
    }

    @Override
    public void removeAllComponents() {
        getContent().removeAllComponents();
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        getContent().replaceComponent(oldComponent, newComponent);
    }

    @Override
    public Iterator<Component> getComponentIterator() {
        return getContent().iterator();
    }

    @Override
    public void moveComponentsFrom(ComponentContainer source) {
        getContent().moveComponentsFrom(source);
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        if (getState(false).showAsPanel && getState(false).outerMarginsBitmask != 0) {
            LoggerFactory.getLogger(CubaGroupBox.class)
                    .warn("GroupBox's 'showAsPanel' and 'outerMargin' properties are set simultaneously");
        }
    }

    public interface ExpandChangeHandler {
        void expandStateChanged(boolean expanded, boolean invokedByUser);
    }

    public void setShowAsPanel(boolean showAsPanel) {
        if (getState(false).showAsPanel != showAsPanel) {
            if (showAsPanel) {
                setPrimaryStyleName("v-panel");
            } else {
                setPrimaryStyleName("c-groupbox");
            }

            getState().showAsPanel = showAsPanel;
        }
    }

    public boolean isShowAsPanel() {
        return getState(false).showAsPanel;
    }

    public MarginInfo getOuterMargin() {
        return new MarginInfo(getState(false).outerMarginsBitmask);
    }

    public void setOuterMargin(MarginInfo marginInfo) {
        getState().outerMarginsBitmask = marginInfo.getBitMask();
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return getState().requiredIndicatorVisible;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean visible) {
        getState().requiredIndicatorVisible = visible;
    }
}