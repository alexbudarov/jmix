/*
 * Copyright (c) 2008-2018 Haulmont.
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
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.ClientConfig;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.actions.Action;
import io.jmix.ui.actions.ActionType;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.data.meta.ContainerDataUnit;
import io.jmix.ui.icons.CubaIcon;
import io.jmix.ui.icons.Icons;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioDelegate;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.Nested;
import io.jmix.ui.screen.Install;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Standard action for removing an entity instance from the list and from the database.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 */
@StudioAction(category = "List Actions", description = "Removes an entity instance from the list and from the database")
@ActionType(RemoveAction.ID)
public class RemoveAction extends SecuredListAction implements Action.DisabledWhenScreenReadOnly {

    public static final String ID = "remove";

    @Inject
    protected RemoveOperation removeOperation;

    protected Boolean confirmation;
    protected String confirmationMessage;
    protected String confirmationTitle;
    protected Consumer<RemoveOperation.AfterActionPerformedEvent> afterActionPerformedHandler;
    protected Consumer<RemoveOperation.ActionCancelledEvent> actionCancelledHandler;

    public RemoveAction() {
        super(ID);
    }

    public RemoveAction(String id) {
        super(id);
    }

    /**
     * Returns true/false if the confirmation flag was set by {@link #setConfirmation(Boolean)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public Boolean getConfirmation() {
        return confirmation;
    }

    /**
     * Sets whether to ask confirmation from the user.
     */
    @StudioPropertiesItem
    public void setConfirmation(Boolean confirmation) {
        this.confirmation = confirmation;
    }

    /**
     * Returns confirmation dialog message if it was set by {@link #setConfirmationMessage(String)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    /**
     * Sets confirmation dialog message.
     */
    @StudioPropertiesItem
    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

    /**
     * Returns confirmation dialog title if it was set by {@link #setConfirmationTitle(String)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public String getConfirmationTitle() {
        return confirmationTitle;
    }

    /**
     * Sets confirmation dialog title.
     */
    @StudioPropertiesItem
    public void setConfirmationTitle(String confirmationTitle) {
        this.confirmationTitle = confirmationTitle;
    }

    /**
     * Sets the handler to be invoked after removing entities.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.remove", subject = "afterActionPerformedHandler")
     * protected void petsTableRemoveAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent event) {
     *     System.out.println("Removed " + event.getItems());
     * }
     * </pre>
     */
    @StudioDelegate
    public void setAfterActionPerformedHandler(Consumer<RemoveOperation.AfterActionPerformedEvent> afterActionPerformedHandler) {
        this.afterActionPerformedHandler = afterActionPerformedHandler;
    }

    /**
     * Sets the handler to be invoked if the action was cancelled by the user.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.remove", subject = "actionCancelledHandler")
     * protected void petsTableRemoveActionCancelledHandler(RemoveOperation.ActionCancelledEvent event) {
     *     System.out.println("Cancelled");
     * }
     * </pre>
     */
    @StudioDelegate
    public void setActionCancelledHandler(Consumer<RemoveOperation.ActionCancelledEvent> actionCancelledHandler) {
        this.actionCancelledHandler = actionCancelledHandler;
    }

    @Inject
    protected void setIcons(Icons icons) {
        this.icon = icons.get(CubaIcon.REMOVE_ACTION);
    }

    @Inject
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Remove");
    }

    @Inject
    protected void setConfiguration(ConfigInterfaces configuration) {
        ClientConfig clientConfig = configuration.getConfig(ClientConfig.class);
        setShortcut(clientConfig.getTableRemoveShortcut());
    }

    @Override
    protected boolean isPermitted() {
        if (target == null || !(target.getItems() instanceof ContainerDataUnit)) {
            return false;
        }

        if (!checkRemovePermission()) {
            return false;
        }

        return super.isPermitted();
    }

    protected boolean checkRemovePermission() {
        ContainerDataUnit containerDataUnit = (ContainerDataUnit) target.getItems();

        MetaClass metaClass = containerDataUnit.getEntityMetaClass();
        if (metaClass == null) {
            return false;
        }

        boolean entityOpPermitted = security.isEntityOpPermitted(metaClass, EntityOp.DELETE);
        if (!entityOpPermitted) {
            return false;
        }

        if (containerDataUnit.getContainer() instanceof Nested) {
            Nested nestedContainer = (Nested) containerDataUnit.getContainer();

            MetaClass masterMetaClass = nestedContainer.getMaster().getEntityMetaClass();
            MetaProperty metaProperty = masterMetaClass.getPropertyNN(nestedContainer.getProperty());

            boolean attrPermitted = security.isEntityAttrUpdatePermitted(masterMetaClass, metaProperty.getName());
            if (!attrPermitted) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void actionPerform(Component component) {
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
            throw new IllegalStateException("RemoveAction target is not set");
        }

        if (!(target.getItems() instanceof ContainerDataUnit)) {
            throw new IllegalStateException("RemoveAction target items is null or does not implement ContainerDataUnit");
        }

        ContainerDataUnit items = (ContainerDataUnit) target.getItems();
        CollectionContainer container = items.getContainer();
        if (container == null) {
            throw new IllegalStateException("RemoveAction target is not bound to CollectionContainer");
        }

        RemoveOperation.RemoveBuilder builder = removeOperation.builder(target);

        if (confirmation != null) {
            builder = builder.withConfirmation(confirmation);
        } else {
            builder = builder.withConfirmation(true);
        }

        if (confirmationMessage != null) {
            builder = builder.withConfirmationMessage(confirmationMessage);
        }

        if (confirmationTitle != null) {
            builder = builder.withConfirmationTitle(confirmationTitle);
        }

        if (afterActionPerformedHandler != null) {
            builder = builder.afterActionPerformed(afterActionPerformedHandler);
        }

        if (actionCancelledHandler != null) {
            builder = builder.onCancel(actionCancelledHandler);
        }

        builder.remove();
    }
}