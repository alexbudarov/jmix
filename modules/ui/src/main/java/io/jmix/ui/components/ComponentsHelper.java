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

import io.jmix.core.AppBeans;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.Lookup;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.actions.Action;
import io.jmix.ui.components.data.value.ValueBinder;
import io.jmix.ui.components.impl.FrameImplementation;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.ValuePathHelper;
import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static io.jmix.core.entity.BaseEntityInternalAccess.getFilteredAttributes;

/**
 * Utility class working with GenericUI components.
 */
public abstract class ComponentsHelper {
    /**
     * Returns the collection of components within the specified container and all of its children.
     *
     * @param container container to start from
     * @return collection of components
     */
    public static Collection<Component> getComponents(ComponentContainer container) {
        // do not return LinkedHashSet, it uses much more memory than ArrayList
        Collection<Component> res = new ArrayList<>();

        fillChildComponents(container, res);

        if (res.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(res);
    }

    /**
     * Visit all components below the specified container.
     *
     * @param container container to start from
     * @param visitor   visitor instance
     */
    public static void traverseComponents(ComponentContainer container, Consumer<Component> visitor) {
        container.getOwnComponentsStream()
                .forEach(c -> {
                    visitor.accept(c);

                    if (c instanceof ComponentContainer) {
                        traverseComponents((ComponentContainer) c, visitor);
                    }
                });
    }

    /**
     * Visit all {@link Validatable} components below the specified container.
     *
     * @param container container to start from
     * @param visitor   visitor instance
     */
    public static void traverseValidatable(ComponentContainer container, Consumer<Validatable> visitor) {
        traverseComponents(container, c -> {
            if (c instanceof Validatable && ((Validatable) c).isValidateOnCommit()) {
                visitor.accept((Validatable) c);
            }
        });
    }

    @Nullable
    public static Component getWindowComponent(Window window, String id) {
        String[] elements = ValuePathHelper.parse(id);

        FrameImplementation frameImpl = (FrameImplementation) window;
        if (elements.length == 1) {
            return frameImpl.getRegisteredComponent(id);
                // todo timers should be find using getFacet()
//                return window.getTimer(id);
        } else {
            Component innerComponent = frameImpl.getRegisteredComponent(elements[0]);
            if (innerComponent instanceof ComponentContainer) {

                String subPath = ValuePathHelper.pathSuffix(elements);
                return ((ComponentContainer) innerComponent).getComponent(subPath);
            } else if (innerComponent instanceof HasNamedComponents) {

                String subPath = ValuePathHelper.pathSuffix(elements);
                return ((HasNamedComponents) innerComponent).getComponent(subPath);
            }

            return null;
        }
    }

    @Nullable
    public static Component getFrameComponent(Frame frame, String id) {
        FrameImplementation frameImpl = (FrameImplementation) frame;
        String[] elements = ValuePathHelper.parse(id);
        if (elements.length == 1) {
            Component component = frameImpl.getRegisteredComponent(id);
            if (component == null && frame.getFrame() != null && frame.getFrame() != frame) {
                component = frame.getFrame().getComponent(id);
            }
            return component;
        } else {
            Component innerComponent = frameImpl.getRegisteredComponent(elements[0]);
            if (innerComponent instanceof ComponentContainer) {

                String subPath = ValuePathHelper.pathSuffix(elements);
                return ((ComponentContainer) innerComponent).getComponent(subPath);
            } else if (innerComponent instanceof HasNamedComponents) {

                String subPath = ValuePathHelper.pathSuffix(elements);
                return ((HasNamedComponents) innerComponent).getComponent(subPath);
            }

            return null;
        }
    }

    @Nullable
    public static Component getComponent(ComponentContainer container, String id) {
        String[] elements = ValuePathHelper.parse(id);
        if (elements.length == 1) {
            Component component = container.getOwnComponent(id);

            if (component == null) {
                return getComponentByIteration(container, id);
            } else {
                return component;
            }

        } else {
            Component innerComponent = container.getOwnComponent(elements[0]);

            if (innerComponent == null) {
                return getComponentByIteration(container, id);
            } else {
                if (innerComponent instanceof ComponentContainer) {
                    String subPath = ValuePathHelper.pathSuffix(elements);
                    return ((ComponentContainer) innerComponent).getComponent(subPath);
                } else if (innerComponent instanceof HasNamedComponents) {

                    String subPath = ValuePathHelper.pathSuffix(elements);
                    return ((HasNamedComponents) innerComponent).getComponent(subPath);
                }

                return null;
            }
        }
    }

    @Nullable
    private static Component getComponentByIteration(ComponentContainer container, String id) {
        return getComponentByIterationInternal(container.getOwnComponents(), id);
    }

    private static Component getComponentByIterationInternal(Collection<Component> components, String id) {
        for (Component component : components) {
            if (id.equals(component.getId())) {
                return component;
            } else if (component instanceof ComponentContainer) {
                Collection<Component> ownComponents = ((ComponentContainer) component).getOwnComponents();
                Component innerComponent = getComponentByIterationInternal(ownComponents, id);
                if (innerComponent != null) {
                    return innerComponent;
                }
            } else if (component instanceof HasInnerComponents) {
                Collection<Component> innerComponents = ((HasInnerComponents) component).getInnerComponents();
                Component innerComponent = getComponentByIterationInternal(innerComponents, id);
                if (innerComponent != null) {
                    return innerComponent;
                }
            }
        }
        return null;
    }

    private static void fillChildComponents(ComponentContainer container, Collection<Component> components) {
        Collection<Component> ownComponents = container.getOwnComponents();
        components.addAll(ownComponents);

        for (Component component : ownComponents) {
            if (component instanceof ComponentContainer) {
                fillChildComponents((ComponentContainer) component, components);
            }
        }
    }

    /**
     * Searches for a component by identifier, down by the hierarchy of frames.
     *
     * @param frame frame to start from
     * @param id    component identifier
     * @return component instance or null if not found
     */
    @Nullable
    public static Component findComponent(Frame frame, String id) {
        Component find = frame.getComponent(id);
        if (find != null) {
            return find;
        } else {
            for (Component c : frame.getComponents()) {
                if (c instanceof Frame) {
                    Component nestedComponent = findComponent((Frame) c, id);
                    if (nestedComponent != null) {
                        return nestedComponent;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Visit all components below the specified container.
     *
     * @param container container to start from
     * @param visitor   visitor instance
     */
    public static void walkComponents(
            ComponentContainer container,
            ComponentVisitor visitor
    ) {
        __walkComponents(container, visitor, "");
    }

    private static void __walkComponents(
            ComponentContainer container,
            ComponentVisitor visitor,
            String path
    ) {
        for (Component component : container.getOwnComponents()) {
            String id = component.getId();
            if (id == null && component instanceof ActionOwner
                    && ((ActionOwner) component).getAction() != null) {
                id = ((ActionOwner) component).getAction().getId();
            }
            if (id == null) {
                id = component.getClass().getSimpleName();
            }
            visitor.visit(component, path + id);

            if (component instanceof ComponentContainer) {
                String p = component instanceof Frame ?
                        path + id + "." :
                        path;
                __walkComponents(((ComponentContainer) component), visitor, p);
            } else if (component instanceof AppWorkArea) {
                // todo support HasInnerComponents
                AppWorkArea workArea = (AppWorkArea) component;
                if (workArea.getState() == AppWorkArea.State.INITIAL_LAYOUT) {
                    VBoxLayout initialLayout = workArea.getInitialLayout();

                    __walkComponents(initialLayout, visitor, path);
                }
            }
        }
    }

    /**
     * Iterates over all components applying finder instance.
     * Stops when the component is found and returns {@code true}.
     * If no component is found returns {@code false}.
     *
     * @param container container to start from
     * @param finder    finder instance
     * @return {@code true} if component has been found, {@code false} otherwise
     */
    public static boolean walkComponents(ComponentContainer container,
                                         ComponentFinder finder) {
        return __walkComponents(container, finder);
    }

    private static boolean __walkComponents(ComponentContainer container,
                                            ComponentFinder finder) {
        for (Component component : container.getOwnComponents()) {
            if (finder.visit(component)) {
                return true;
            }

            if (component instanceof ComponentContainer) {
                if (__walkComponents(((ComponentContainer) component), finder)) {
                    return true;
                }
            }
        }
        return false;
    }

    // todo filter
    /*public static String getFilterComponentPath(Filter filter) {
        StringBuilder sb = new StringBuilder(filter.getId() != null ? filter.getId() : "filterWithoutId");
        Frame frame = filter.getFrame();
        while (frame != null) {
            sb.insert(0, ".");
            String s = frame.getId() != null ? frame.getId() : "frameWithoutId";
            if (s.contains(".")) {
                s = "[" + s + "]";
            }
            sb.insert(0, s);
            if (frame instanceof Window) {
                break;
            }
            frame = frame.getFrame();
        }
        return sb.toString();
    }*/

    /**
     * Get the topmost window for the specified component.
     *
     * @param component component instance
     * @return topmost window in the hierarchy of frames for this component.
     * <br>Can be null only if the component wasn't properly initialized.
     */
    @Nullable
    public static Window getWindow(Component.BelongToFrame component) {
        Frame frame = component.getFrame();
        while (frame != null) {
            if (frame instanceof Window && frame.getFrame() == frame) {
                return (Window) frame;
            }
            frame = frame.getFrame();
        }
        return null;
    }

    @Nonnull
    public static Window getWindowNN(Component.BelongToFrame component) {
        Window window = getWindow(component);

        if (window == null) {
            throw new IllegalStateException("Unable to find window for component " +
                    (component.getId() != null ? component.getId() : component.getClass()));
        }

        return window;
    }

    /**
     * Get screen context for UI component.
     *
     * @param component component
     * @return screen context
     * @throws IllegalStateException in case window cannot be inferred
     */
    public static ScreenContext getScreenContext(Component.BelongToFrame component) {
        Window window = getWindowNN(component);

        return UiControllerUtils.getScreenContext(window.getFrameOwner());
    }

    @Nullable
    public static Screen getScreen(ScreenFragment frameOwner) {
        Frame frame = frameOwner.getFragment();
        while (frame != null) {
            if (frame instanceof Window && frame.getFrame() == frame) {
                return ((Window) frame).getFrameOwner();
            }
            frame = frame.getFrame();
        }
        return null;
    }

    @Nullable
    public static Window getParentWindow(ScreenFragment frameOwner) {
        Frame frame = frameOwner.getFragment();
        while (frame != null) {
            if (frame instanceof Window && frame.getFrame() == frame) {
                return (Window) frame;
            }
            frame = frame.getFrame();
        }
        return null;
    }

    /**
     * Get the topmost window for the specified component.
     *
     * @param component component instance
     * @return topmost client specific window in the hierarchy of frames for this component.
     *
     * <br>Can be null only if the component wasn't properly initialized.
     */
    @Nullable
    public static Window getWindowImplementation(Component.BelongToFrame component) {
        Frame frame = component.getFrame();
        while (frame != null) {
            if (frame instanceof Window && frame.getFrame() == frame) {
                Window window = (Window) frame;
                return window instanceof Window.Wrapper ? ((Window.Wrapper) window).getWrappedWindow() : window;
            }
            frame = frame.getFrame();
        }
        return null;
    }

    /**
     * @deprecated Simply use {@link Frame#getFrameOwner()} call.
     */
    @Deprecated
    public static FrameOwner getFrameController(Frame frame) {
        return frame.getFrameOwner();
    }

    public static String getFullFrameId(Frame frame) {
        if (frame instanceof Window) {
            return frame.getId();
        }

        List<String> frameIds = new ArrayList<>(4);
        frameIds.add(frame.getId());
        while (frame != null && !(frame instanceof Window) && frame != frame.getFrame()) {
            frame = frame.getFrame();
            if (frame != null) {
                frameIds.add(frame.getId());
            }
        }

        return StringUtils.join(new ReverseListIterator<>(frameIds), '.');
    }

    /**
     * Searches for an action by name.
     *
     * @param actionName action name, can be a path to an action contained in some {@link ActionsHolder}
     * @param frame      current frame
     * @return action instance or null if there is no action with the specified name
     * @throws IllegalStateException if the component denoted by the path doesn't exist or is not an ActionsHolder
     */
    @Nullable
    public static Action findAction(String actionName, Frame frame) {
        String[] elements = ValuePathHelper.parse(actionName);
        if (elements.length > 1) {
            String id = elements[elements.length - 1];

            String prefix = ValuePathHelper.pathPrefix(elements);
            Component component = frame.getComponent(prefix);
            if (component != null) {
                if (component instanceof ActionsHolder) {
                    return ((ActionsHolder) component).getAction(id);
                } else {
                    throw new IllegalArgumentException(
                            String.format("Component '%s' can't contain actions", prefix));
                }
            } else {
                throw new IllegalArgumentException(
                        String.format("Can't find component '%s'", prefix));
            }
        } else if (elements.length == 1) {
            String id = elements[0];
            return frame.getAction(id);
        } else {
            throw new IllegalArgumentException("Invalid action name: " + actionName);
        }
    }

    public static String getComponentPath(Component c) {
        StringBuilder sb = new StringBuilder(c.getId() == null ? "" : c.getId());
        if (c instanceof Component.BelongToFrame) {
            Frame frame = ((Component.BelongToFrame) c).getFrame();
            while (frame != null) {
                sb.insert(0, ".");
                String s = frame.getId();
                if (s.contains(".")) {
                    s = "[" + s + "]";
                }
                sb.insert(0, s);
                if (frame instanceof Window) {
                    break;
                }
                frame = frame.getFrame();
            }
        }
        return sb.toString();
    }

    public static String getComponentWidth(Component c) {
        float width = c.getWidth();
        SizeUnit widthUnit = c.getWidthSizeUnit();
        return width + widthUnit.getSymbol();
    }

    public static String getComponentHeight(Component c) {
        float height = c.getHeight();
        SizeUnit heightUnit = c.getHeightSizeUnit();
        return height + heightUnit.getSymbol();
    }

    @Deprecated
    public static boolean hasFullWidth(Component c) {
        return (int) c.getWidth() == 100 && c.getWidthSizeUnit() == SizeUnit.PERCENTAGE;
    }

    @Deprecated
    public static boolean hasFullHeight(Component c) {
        return (int) c.getHeight() == 100 && c.getHeightSizeUnit() == SizeUnit.PERCENTAGE;
    }

    /**
     * Place component with error message to validation errors container.
     *
     * @param component validatable component
     * @param e         exception
     * @param errors    errors container
     */
    public static void fillErrorMessages(Validatable component, ValidationException e,
                                         ValidationErrors errors) {
        if (e instanceof ValidationException.HasRelatedComponent) {
            errors.add(((ValidationException.HasRelatedComponent) e).getComponent(), e.getMessage());
        } else if (e instanceof CompositeValidationException) {
            for (CompositeValidationException.ViolationCause cause : ((CompositeValidationException) e).getCauses()) {
                errors.add((Component) component, cause.getMessage());
            }
        } else {
            errors.add((Component) component, e.getMessage());
        }
    }

    public static int findActionById(List<Action> actionList, String actionId) {
        int oldIndex = -1;
        for (int i = 0; i < actionList.size(); i++) {
            Action a = actionList.get(i);
            if (Objects.equals(a.getId(), actionId)) {
                oldIndex = i;
                break;
            }
        }
        return oldIndex;
    }

    @Deprecated
    public static SizeUnit convertToSizeUnit(int unit) {
        switch (unit) {
            case Component.UNITS_PIXELS:
                return SizeUnit.PIXELS;
            case Component.UNITS_PERCENTAGE:
                return SizeUnit.PERCENTAGE;
            default:
                throw new IllegalArgumentException("Unsupported unit: " + unit);
        }
    }

    @Deprecated
    public static int convertFromSizeUnit(SizeUnit unit) {
        switch (unit) {
            case PIXELS:
                return Component.UNITS_PIXELS;
            case PERCENTAGE:
                return Component.UNITS_PERCENTAGE;
            default:
                throw new IllegalArgumentException("Unsupported unit: " + unit);
        }
    }

    /**
     * Focus component (or its nearest focusable parent) and activate all its parents,
     * for instance: select Tab, expand GroupBox.
     *
     * @param component component
     */
    public static void focusComponent(Component component) {
        Component parent = component;
        Component previousComponent = null;

        // activate all parent containers, select Tab in TabSheet, expand GroupBox
        while (parent != null) {
            if (parent instanceof Collapsable
                    && ((Collapsable) parent).isCollapsable()
                    && !((Collapsable) parent).isExpanded()) {
                ((Collapsable) parent).setExpanded(true);
            }

            if (parent instanceof SupportsChildrenSelection
                    && previousComponent != null) {
                ((SupportsChildrenSelection) parent).setChildSelected(previousComponent);
            }

            previousComponent = parent;
            parent = parent.getParent();
        }

        // focus first focusable parent
        parent = component;
        while (parent != null && !(parent instanceof Component.Focusable)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            ((Component.Focusable) parent).focus();
        }
    }

    @Nullable
    public static Component.Focusable focusChildComponent(ComponentContainer container) {
        if (!container.isEnabledRecursive()) {
            return null;
        }
        if (!container.isVisibleRecursive()) {
            return null;
        }

        for (Component component : container.getOwnComponents()) {
            if (component.isVisible()
                    && component.isEnabled()) {

                boolean reachable = true;
                if (component.getParent() instanceof SupportsChildrenSelection) {
                    reachable = ((SupportsChildrenSelection) component.getParent()).isChildSelected(component);
                } else if (component.getParent() instanceof Collapsable) {
                    reachable = ((Collapsable) component.getParent()).isExpanded();
                }

                if (reachable) {
                    if (component instanceof Component.Focusable) {
                        Component.Focusable focusable = (Component.Focusable) component;

                        focusable.focus();
                        return focusable;
                    }

                    if (component instanceof ComponentContainer) {
                        Component.Focusable focused = focusChildComponent((ComponentContainer) component);
                        if (focused != null) {
                            return focused;
                        }
                    }

                    // todo support HasInnerComponents
                }
            }
        }
        return null;
    }
}
