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

import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.*;
import io.jmix.ui.components.ComponentContainer;
import io.jmix.ui.components.*;
import io.jmix.ui.widgets.CubaGroupBox;
import io.jmix.ui.widgets.CubaHorizontalActionsLayout;
import io.jmix.ui.widgets.CubaScrollBoxLayout;
import io.jmix.ui.widgets.JmixVerticalActionsLayout;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.jmix.ui.components.Component.AUTO_SIZE;

public class WebComponentsHelper {

    @SuppressWarnings("unchecked")
    public static <T extends Component> Collection<T> getComponents(HasComponents container, Class<T> aClass) {
        List<T> res = new ArrayList<>();
        for (Object aContainer : container) {
            Component component = (Component) aContainer;
            if (aClass.isAssignableFrom(component.getClass())) {
                res.add((T) component);
            } else if (HasComponents.class.isAssignableFrom(component.getClass())) {
                res.addAll(getComponents((HasComponents) component, aClass));
            }
        }

        return res;
    }

    /**
     * Returns underlying Vaadin component implementation.
     *
     * @param component GUI component
     * @return          Vaadin component
     * @see #getComposition(io.jmix.ui.components.Component)
     */
    public static Component unwrap(io.jmix.ui.components.Component component) {
        Object comp = component;
        while (comp instanceof io.jmix.ui.components.Component.Wrapper) {
            comp = ((io.jmix.ui.components.Component.Wrapper) comp).getComponent();
        }

        return comp instanceof io.jmix.ui.components.Component
                ? ((io.jmix.ui.components.Component) comp).unwrapComposition(Component.class)
                : (Component) comp;
    }

    /**
     * Returns underlying Vaadin component, which serves as the outermost container for the supplied GUI component.
     * For simple components like {@link io.jmix.ui.components.Button} this method returns the same
     * result as {@link #unwrap(io.jmix.ui.components.Component)}.
     *
     * @param component GUI component
     * @return          Vaadin component
     * @see #unwrap(io.jmix.ui.components.Component)
     */
    public static Component getComposition(io.jmix.ui.components.Component component) {
        Object comp = component;
        while (comp instanceof io.jmix.ui.components.Component.Wrapper) {
            comp = ((io.jmix.ui.components.Component.Wrapper) comp).getComposition();
        }

        return comp instanceof io.jmix.ui.components.Component
                ? ((io.jmix.ui.components.Component) comp).unwrapComposition(Component.class)
                : (Component) comp;
    }

    public static void expand(AbstractOrderedLayout layout, Component component, String height, String width) {
        if (!isHorizontalLayout(layout)
                && (StringUtils.isEmpty(height) || AUTO_SIZE.equals(height) || height.endsWith("%"))) {
            component.setHeight(100, Sizeable.Unit.PERCENTAGE);
        }

        if (!isVerticalLayout(layout)
                && (StringUtils.isEmpty(width) || AUTO_SIZE.equals(width) || width.endsWith("%"))) {
            component.setWidth(100, Sizeable.Unit.PERCENTAGE);
        }

        layout.setExpandRatio(component, 1);
    }


    public static boolean isVerticalLayout(AbstractOrderedLayout layout) {
        return (layout instanceof VerticalLayout)
                || (layout instanceof JmixVerticalActionsLayout);
    }

    public static boolean isHorizontalLayout(AbstractOrderedLayout layout) {
        return (layout instanceof HorizontalLayout)
                || (layout instanceof CubaHorizontalActionsLayout);
    }

    /**
     * Checks if the component should be visible to the client. Returns false if
     * the child should not be sent to the client, true otherwise.
     *
     * @param child The child to check
     * @return true if the child is visible to the client, false otherwise
     */
    public static boolean isComponentVisibleToClient(Component child) {
        if (!child.isVisible()) {
            return false;
        }
        HasComponents parent = child.getParent();

        if (parent instanceof SelectiveRenderer) {
            if (!((SelectiveRenderer) parent).isRendered(child)) {
                return false;
            }
        }

        if (parent != null) {
            return isComponentVisibleToClient(parent);
        } else {
            if (child instanceof UI) {
                // UI has no parent and visibility was checked above
                return true;
            } else {
                // Component which is not attached to any UI
                return false;
            }
        }
    }

    /**
     * Tests if component visible and its container visible.
     *
     * @param child component
     * @return component visibility
     */
    public static boolean isComponentVisible(Component child) {
        if (child.getParent() instanceof TabSheet) {
            TabSheet tabSheet = (TabSheet) child.getParent();
            TabSheet.Tab tab = tabSheet.getTab(child);
            if (!tab.isVisible()) {
                return false;
            }
        }


        if (child.getParent() instanceof CubaGroupBox) {
            // ignore groupbox content container visibility
            return isComponentVisible(child.getParent());
        }
        return child.isVisible() && (child.getParent() == null || isComponentVisible(child.getParent()));
    }

    /**
     * Tests if component enabled and visible and its container enabled.
     *
     * @param child component
     * @return component enabled state
     */
    public static boolean isComponentEnabled(Component child) {
        if (child.getParent() instanceof TabSheet) {
            TabSheet tabSheet = (TabSheet) child.getParent();
            TabSheet.Tab tab = tabSheet.getTab(child);
            if (!tab.isEnabled()) {
                return false;
            }
        }

        return child.isEnabled() && (child.getParent() == null || isComponentEnabled(child.getParent())) &&
                isComponentVisible(child);
    }

    public static boolean isComponentExpanded(io.jmix.ui.components.Component component) {
        Component vComponent = component.unwrapComposition(Component.class);
        if (vComponent.getParent() instanceof AbstractOrderedLayout) {
            AbstractOrderedLayout layout = (AbstractOrderedLayout) vComponent.getParent();
            return (int)layout.getExpandRatio(vComponent) == 1;
        }

        return false;
    }

    public static boolean convertFieldGroupCaptionAlignment(/*FieldGroup.FieldCaptionAlignment captionAlignment*/) {
        return false;
        /*
        TODO: legacy-ui
        if (captionAlignment == FieldGroup.FieldCaptionAlignment.LEFT)
            return true;
        else
            return false;*/
    }

    public static ShortcutTriggeredEvent getShortcutEvent(io.jmix.ui.components.Component source,
                                                          Component target) {
        Component vaadinSource = getVaadinSource(source);

        if (vaadinSource == target) {
            return new ShortcutTriggeredEvent(source, source);
        }

        if (source instanceof ComponentContainer) {
            ComponentContainer container = (ComponentContainer) source;
            io.jmix.ui.components.Component childComponent =
                    findChildComponent(container, target);
            return new ShortcutTriggeredEvent(source, childComponent);
        }

        return new ShortcutTriggeredEvent(source, null);
    }

    protected static Component getVaadinSource(io.jmix.ui.components.Component source) {
        Component component = source.unwrapComposition(Component.class);
        if (component instanceof AbstractSingleComponentContainer) {
            return ((AbstractSingleComponentContainer) component).getContent();
        }


        if (component instanceof CubaScrollBoxLayout) {
            return ((CubaScrollBoxLayout) component).getComponent(0);
        }

        return component;
    }

    @Nullable
    protected static io.jmix.ui.components.Component findChildComponent(ComponentContainer container,
                                                                                   Component target) {
        Component vaadinSource = getVaadinSource(container);
        Collection<io.jmix.ui.components.Component> components = container.getOwnComponents();

        return findChildComponent(components, vaadinSource, target);
    }

    // todo implement
    /*@Nullable
    protected static io.jmix.ui.components.Component findChildComponent(FieldGroup fieldGroup,
                                                                                   Component target) {
        Component vaadinSource = fieldGroup.unwrap(CubaFieldGroupLayout.class);
        Collection<io.jmix.ui.components.Component> components = fieldGroup.getFields().stream()
                .map(FieldGroup.FieldConfig::getComponentNN)
                .collect(Collectors.toList());

        return findChildComponent(components, vaadinSource, target);
    }*/

    protected static io.jmix.ui.components.Component findChildComponent(
            Collection<io.jmix.ui.components.Component> components,
            Component vaadinSource, Component target) {
        Component targetComponent = getDirectChildComponent(target, vaadinSource);

        for (io.jmix.ui.components.Component component : components) {
            Component unwrapped = component.unwrapComposition(Component.class);
            if (unwrapped == targetComponent) {
                io.jmix.ui.components.Component child = null;

                if (component instanceof ComponentContainer) {
                    child = findChildComponent((ComponentContainer) component, target);
                }


                if (component instanceof HasButtonsPanel) {
                    ButtonsPanel buttonsPanel = ((HasButtonsPanel) component).getButtonsPanel();
                    if (getVaadinSource(buttonsPanel) == target) {
                        return buttonsPanel;
                    } else {
                        child = findChildComponent(buttonsPanel, target);
                    }
                }

                /*
                TODO: legacy-ui
                if (component instanceof FieldGroup) {
                    FieldGroup fieldGroup = (FieldGroup) component;
                    child = findChildComponent(fieldGroup, target);
                }*/

                return child != null ? child : component;
            }
        }
        return null;
    }

    /**
     * @return the direct child component of the layout which contains the component involved to event
     */
    protected static Component getDirectChildComponent(Component targetComponent, Component vaadinSource) {
        while (targetComponent != null
                && targetComponent.getParent() != vaadinSource) {
            targetComponent = targetComponent.getParent();
        }

        return targetComponent;
    }

    public static void setClickShortcut(Button button, String shortcut) {
        KeyCombination closeCombination = KeyCombination.create(shortcut);
        int[] closeModifiers = KeyCombination.Modifier.codes(closeCombination.getModifiers());
        int closeCode = closeCombination.getKey().getCode();

        button.setClickShortcut(closeCode, closeModifiers);
    }
}
