/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.ui.actions.list;

import io.jmix.core.ConfigInterfaces;
import io.jmix.core.Messages;
import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.ClientConfig;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.actions.ActionType;
import io.jmix.ui.builders.EditorBuilder;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.data.meta.EntityDataUnit;
import io.jmix.ui.icons.CubaIcon;
import io.jmix.ui.icons.Icons;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioDelegate;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.ActionScreenInitializer;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Standard action for opening an editor screen in the read-only mode.
 * The editor screen must implement the {@link ReadOnlyAwareScreen} interface.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 */
@StudioAction(category = "List Actions", description = "Opens an editor screen for an entity instance in read-only mode")
@ActionType(ViewAction.ID)
public class ViewAction extends SecuredListAction {

    public static final String ID = "view";

    protected ScreenBuilders screenBuilders;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();

    public ViewAction() {
        this(ID);
    }

    public ViewAction(String id) {
        super(id);
    }

    /**
     * Returns the editor screen open mode if it was set by {@link #setOpenMode(OpenMode)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public OpenMode getOpenMode() {
        return screenInitializer.getOpenMode();
    }

    /**
     * Sets the editor screen open mode.
     */
    @StudioPropertiesItem
    public void setOpenMode(OpenMode openMode) {
        screenInitializer.setOpenMode(openMode);
    }

    /**
     * Returns the editor screen id if it was set by {@link #setScreenId(String)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public String getScreenId() {
        return screenInitializer.getScreenId();
    }

    /**
     * Sets the editor screen id.
     */
    @StudioPropertiesItem
    public void setScreenId(String screenId) {
        screenInitializer.setScreenId(screenId);
    }

    /**
     * Returns the editor screen class if it was set by {@link #setScreenClass(Class)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public Class getScreenClass() {
        return screenInitializer.getScreenClass();
    }

    /**
     * Sets the editor screen id.
     */
    @StudioPropertiesItem
    public void setScreenClass(Class screenClass) {
        screenInitializer.setScreenClass(screenClass);
    }

    /**
     * Sets the editor screen options supplier. The supplier provides {@code ScreenOptions} to the
     * opened screen.
     * <p>
     * The preferred way to set the supplier is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.view", subject = "screenOptionsSupplier")
     * protected ScreenOptions petsTableViewScreenOptionsSupplier() {
     *     return new MapScreenOptions(ParamsMap.of("someParameter", 10));
     * }
     * </pre>
     */
    @StudioDelegate
    public void setScreenOptionsSupplier(Supplier<ScreenOptions> screenOptionsSupplier) {
        screenInitializer.setScreenOptionsSupplier(screenOptionsSupplier);
    }

    /**
     * Sets the editor screen configurer. Use the configurer if you need to provide parameters to the
     * opened screen through setters.
     * <p>
     * The preferred way to set the configurer is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.view", subject = "screenConfigurer")
     * protected void petsTableViewScreenConfigurer(Screen editorScreen) {
     *     ((PetEdit) editorScreen).setSomeParameter(someValue);
     * }
     * </pre>
     */
    @StudioDelegate
    public void setScreenConfigurer(Consumer<Screen> screenConfigurer) {
        screenInitializer.setScreenConfigurer(screenConfigurer);
    }

    /**
     * Sets the handler to be invoked when the editor screen closes.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.view", subject = "afterCloseHandler")
     * protected void petsTableViewAfterCloseHandler(AfterCloseEvent event) {
     *     CloseAction closeAction = event.getCloseAction();
     *     System.out.println("Closed with " + closeAction);
     * }
     * </pre>
     */
    public void setAfterCloseHandler(Consumer<Screen.AfterCloseEvent> afterCloseHandler) {
        screenInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    @Inject
    protected void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Inject
    protected void setIcons(Icons icons) {
        this.icon = icons.get(CubaIcon.VIEW_ACTION);
    }

    @Inject
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.View");
    }

    @Inject
    protected void setConfiguration(ConfigInterfaces configuration) {
        ClientConfig clientConfig = configuration.getConfig(ClientConfig.class);
        setShortcut(clientConfig.getTableViewShortcut());
    }

    @Override
    protected boolean isPermitted() {
        if (target == null || !(target.getItems() instanceof EntityDataUnit)) {
            return false;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            return true;
        }

        boolean entityOpPermitted = security.isEntityOpPermitted(metaClass, EntityOp.READ);
        if (!entityOpPermitted) {
            return false;
        }

        return super.isPermitted();
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    /**
     * Executes the action.
     */
    @SuppressWarnings("unchecked")
    public void execute() {
        if (target == null) {
            throw new IllegalStateException("ViewAction target is not set");
        }

        if (!(target.getItems() instanceof EntityDataUnit)) {
            throw new IllegalStateException("ViewAction target dataSource is null or does not implement EntityDataUnit");
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            throw new IllegalStateException("Target is not bound to entity");
        }

        Entity editedEntity = target.getSingleSelected();
        if (editedEntity == null) {
            throw new IllegalStateException("There is not selected item in ViewAction target");
        }

        EditorBuilder builder = screenBuilders.editor(target)
                .editEntity(editedEntity);

        builder = screenInitializer.initBuilder(builder);

        Screen editor = builder.build();

        screenInitializer.initScreen(editor);

        if (editor instanceof ReadOnlyAwareScreen) {
            ((ReadOnlyAwareScreen) editor).setReadOnly(true);
        } else {
            throw new IllegalStateException(String.format("Screen '%s' does not implement ReadOnlyAwareScreen: %s",
                    editor.getId(), editor.getClass()));
        }

        editor.show();
    }
}
