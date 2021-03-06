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
package com.haulmont.cuba.gui.components.actions;

import io.jmix.core.ConfigInterfaces;
import io.jmix.core.AppBeans;
import io.jmix.core.Messages;
import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.EntityAttrAccess;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.ClientConfig;
import io.jmix.ui.Dialogs;
import io.jmix.ui.actions.Action;
import io.jmix.ui.actions.DialogAction;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.ComponentsHelper;
import io.jmix.ui.components.ListComponent;
import io.jmix.ui.icons.CubaIcon;
import io.jmix.ui.icons.Icons;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.PropertyDatasource;
import org.springframework.context.annotation.Scope;

import java.util.HashSet;
import java.util.Set;

/**
 * Standard list action to remove an entity instance.
 * <p>
 * Action's behaviour can be customized by providing arguments to constructor, setting properties, or overriding
 * method {@link #afterRemove(Set)} )}
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 * &lt;bean id="cuba_RemoveAction" class="com.company.sample.gui.MyRemoveAction" scope="prototype"/&gt;
 * </pre>
 * Also, use {@code create()} static methods instead of constructors when creating the action programmatically.
 */
@org.springframework.stereotype.Component("cuba_RemoveAction")
@Scope("prototype")
public class LegacyRemoveAction extends ItemTrackingAction
        implements Action.HasBeforeActionPerformedHandler, Action.DisabledWhenScreenReadOnly {

    public static final String ACTION_ID = ListActionType.REMOVE.getId();

    protected boolean autocommit;

    protected boolean confirm = true;
    protected String confirmationMessage;
    protected String confirmationTitle;

    protected AfterRemoveHandler afterRemoveHandler;

    protected BeforeActionPerformedHandler beforeActionPerformedHandler;

    public interface AfterRemoveHandler {
        /**
         * @param removedItems  set of removed instances
         */
        void handle(Set removedItems);
    }

    /**
     * Creates an action with default id. Autocommit is set to true.
     * @param target    component containing this action
     */
    public static LegacyRemoveAction create(ListComponent target) {
        return AppBeans.getPrototype("cuba_RemoveAction", target);
    }

    /**
     * Creates an action with default id.
     * @param target    component containing this action
     * @param autocommit    whether to commit datasource immediately
     */
    public static LegacyRemoveAction create(ListComponent target, boolean autocommit) {
        return AppBeans.getPrototype("cuba_RemoveAction", target, autocommit);
    }

    /**
     * Creates an action with the given id.
     * @param target    component containing this action
     * @param autocommit    whether to commit datasource immediately
     * @param id            action's identifier
     */
    public static LegacyRemoveAction create(ListComponent target, boolean autocommit, String id) {
        return AppBeans.getPrototype("cuba_RemoveAction", target, autocommit, id);
    }

    /**
     * The simplest constructor. The action has default name and autocommit=true.
     * @param target    component containing this action
     */
    public LegacyRemoveAction(ListComponent target) {
        this(target, true, ACTION_ID);
    }

    /**
     * Constructor that allows to specify autocommit value. The action has default name.
     * @param target        component containing this action
     * @param autocommit    whether to commit datasource immediately
     */
    public LegacyRemoveAction(ListComponent target, boolean autocommit) {
        this(target, autocommit, ACTION_ID);
    }

    /**
     * Constructor that allows to specify action's identifier and autocommit value.
     * @param target        component containing this action
     * @param autocommit    whether to commit datasource immediately
     * @param id            action's identifier
     */
    public LegacyRemoveAction(ListComponent target, boolean autocommit, String id) {
        super(id);

        this.target = target;
        this.autocommit = autocommit;

        Messages messages = AppBeans.get(Messages.NAME);
        this.caption = messages.getMessage("actions.Remove");

        this.icon = AppBeans.get(Icons.class).get(CubaIcon.REMOVE_ACTION);

        ConfigInterfaces configuration = AppBeans.get(ConfigInterfaces.NAME);
        ClientConfig config = configuration.getConfig(ClientConfig.class);
        setShortcut(config.getTableRemoveShortcut());
    }

    /**
     * Check permissions for Action
     */
    @Override
    protected boolean isPermitted() {
        if (target == null/* || target.getDatasource() == null TODO: legacy-ui*/) {
            return false;
        }

        if (!checkRemovePermission()) {
            return false;
        }

        return super.isPermitted();
    }

    protected boolean checkRemovePermission() {
        CollectionDatasource ds = null/*target.getDatasource() TODO: legacy-ui*/;
        if (ds instanceof PropertyDatasource) {
            PropertyDatasource propertyDatasource = (PropertyDatasource) ds;

            MetaClass parentMetaClass = propertyDatasource.getMaster().getMetaClass();
            MetaProperty metaProperty = propertyDatasource.getProperty();

            boolean modifyPermitted = security.isEntityAttrPermitted(parentMetaClass, metaProperty.getName(),
                    EntityAttrAccess.MODIFY);
            if (!modifyPermitted) {
                return false;
            }

            if (metaProperty.getRange().getCardinality() != Range.Cardinality.MANY_TO_MANY) {
                boolean deletePermitted = security.isEntityOpPermitted(ds.getMetaClass(), EntityOp.DELETE);
                if (!deletePermitted) {
                    return false;
                }
            }
        } else {
            boolean entityOpPermitted = security.isEntityOpPermitted(ds.getMetaClass(), EntityOp.DELETE);
            if (!entityOpPermitted) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is invoked by the action owner component.
     *
     * @param component component invoking the action
     */
    @Override
    public void actionPerform(Component component) {
        if (!isEnabled()) {
            return;
        }

        if (beforeActionPerformedHandler != null) {
            if (!beforeActionPerformedHandler.beforeActionPerformed())
                return;
        }

        @SuppressWarnings("unchecked")
        Set<Entity> selected = target.getSelected();
        if (!selected.isEmpty()) {
            if (confirm) {
                confirmAndRemove(selected);
            } else {
                remove(selected);
            }
        }
    }

    protected void confirmAndRemove(Set<Entity> selected) {
        Dialogs dialogs = ComponentsHelper.getScreenContext(target.getFrame()).getDialogs();

        dialogs.createOptionDialog()
                .withCaption(getConfirmationTitle())
                .withMessage(getConfirmationMessage())
                .withActions(
                        new DialogAction(DialogAction.Type.OK, Status.PRIMARY).withHandler(event -> {
                            try {
                                remove(selected);
                            } finally {
                                if (target instanceof Component.Focusable) {
                                    ((Component.Focusable) target).focus();
                                }

                                Set<Entity> filtered = new HashSet<>(selected);
                                // TODO: legacy-ui
                                // filtered.retainAll(target.getDatasource().getItems());
                                //noinspection unchecked
                                target.setSelected(filtered);
                            }
                        }),
                        new DialogAction(DialogAction.Type.CANCEL).withHandler(event -> {
                            // move focus to owner
                            if (target instanceof Component.Focusable) {
                                ((Component.Focusable) target).focus();
                            }
                        }))
                .show();
    }

    protected void remove(Set<Entity> selected) {
        doRemove(selected, autocommit);

        // move focus to owner
        if (target instanceof Component.Focusable) {
            ((Component.Focusable) target).focus();
        }

        afterRemove(selected);
        if (afterRemoveHandler != null) {
            afterRemoveHandler.handle(selected);
        }
    }

    /**
     * @return  whether to commit datasource immediately after deletion
     */
    public boolean isAutocommit() {
        return autocommit;
    }

    /**
     * @param autocommit    whether to commit datasource immediately after deletion
     */
    public void setAutocommit(boolean autocommit) {
        this.autocommit = autocommit;
    }

    /**
     * @return  whether to show the confirmation dialog to user
     */
    public boolean isConfirm() {
        return confirm;
    }

    /**
     * @param confirm   whether to show the confirmation dialog to user
     */
    public void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }

    /**
     * Provides confirmation dialog message.
     * @return  localized message
     */
    public String getConfirmationMessage() {
        if (confirmationMessage != null)
            return confirmationMessage;
        else {
            Messages messages = AppBeans.get(Messages.NAME);
            return messages.getMessage("dialogs.Confirmation.Remove");
        }
    }

    /**
     * @param confirmationMessage   confirmation dialog message
     */
    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

    /**
     * Provides confirmation dialog title.
     * @return  localized title
     */
    public String getConfirmationTitle() {
        if (confirmationTitle != null)
            return confirmationTitle;
        else {
            Messages messages = AppBeans.get(Messages.NAME);
            return messages.getMessage("dialogs.Confirmation");
        }
    }

    /**
     * @param confirmationTitle confirmation dialog title.
     */
    public void setConfirmationTitle(String confirmationTitle) {
        this.confirmationTitle = confirmationTitle;
    }

    protected void doRemove(Set<Entity> selected, boolean autocommit) {
        CollectionDatasource datasource = null/*target.getDatasource() TODO: legac-ui*/;
        for (Entity item : selected) {
            datasource.removeItem(item);
        }

        if (autocommit && (datasource.getCommitMode() != Datasource.CommitMode.PARENT)) {
            try {
                datasource.commit();
            } catch (RuntimeException e) {
                datasource.refresh();
                throw e;
            }
        }
    }

    /**
     * Hook invoked after remove.
     * @param selected  set of removed instances
     */
    protected void afterRemove(Set selected) {
    }

    /**
     * @param afterRemoveHandler handler that is invoked after remove
     */
    public void setAfterRemoveHandler(AfterRemoveHandler afterRemoveHandler) {
        this.afterRemoveHandler = afterRemoveHandler;
    }

    @Override
    public BeforeActionPerformedHandler getBeforeActionPerformedHandler() {
        return beforeActionPerformedHandler;
    }

    @Override
    public void setBeforeActionPerformedHandler(BeforeActionPerformedHandler handler) {
        beforeActionPerformedHandler = handler;
    }
}
