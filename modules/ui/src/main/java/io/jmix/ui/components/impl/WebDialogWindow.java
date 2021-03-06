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

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import io.jmix.core.BeanLocator;
import io.jmix.core.ConfigInterfaces;
import io.jmix.core.Messages;
import io.jmix.ui.AppUI;
import io.jmix.ui.ClientConfig;
import io.jmix.ui.components.*;
import io.jmix.ui.icons.IconResolver;
import io.jmix.ui.screen.StandardCloseAction;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import io.jmix.ui.widgets.JmixWindow;
import io.jmix.ui.widgets.ShortcutListenerDelegate;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static io.jmix.ui.components.impl.WebWrapperUtils.toSizeUnit;

public class WebDialogWindow extends WebWindow implements DialogWindow, InitializingBean {
    protected JmixWindow dialogWindow;

    protected BeanLocator beanLocator;

    public WebDialogWindow() {
        this.dialogWindow = new GuiDialogWindow(this);
        this.dialogWindow.setContent(component);
        this.dialogWindow.addPreCloseListener(this::onCloseButtonClick);
    }

    @Override
    public void afterPropertiesSet() {
        setupDialogShortcuts();
        setupContextMenu();
        setupDefaultSize();
    }

    protected void setupDefaultSize() {
        ThemeConstantsManager themeConstantsManager = beanLocator.get(ThemeConstantsManager.NAME);
        ThemeConstants theme = themeConstantsManager.getConstants();

        dialogWindow.setWidth(theme.get("cuba.web.WebWindowManager.dialog.width"));
        dialogWindow.setHeightUndefined();

        component.setWidth(100, Sizeable.Unit.PERCENTAGE);
        component.setHeightUndefined();
    }

    protected void setupContextMenu() {
        dialogWindow.addContextActionHandler(new DialogWindowActionHandler());
    }

    protected void setupDialogShortcuts() {
        ClientConfig clientConfig = getClientConfig();

        String closeShortcut = clientConfig.getCloseShortcut();
        KeyCombination closeCombination = KeyCombination.create(closeShortcut);

        ShortcutListenerDelegate exitAction = new ShortcutListenerDelegate(
                "closeShortcutAction",
                closeCombination.getKey().getCode(),
                KeyCombination.Modifier.codes(closeCombination.getModifiers())
        );

        exitAction.withHandler(this::onCloseShortcutTriggered);

        dialogWindow.addActionHandler(new Action.Handler() {
            @Override
            public Action[] getActions(Object target, Object sender) {
                return new ShortcutAction[]{exitAction};
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                if (action == exitAction) {
                    exitAction.handleAction(sender, target);
                }
            }
        });
    }

    protected ClientConfig getClientConfig() {
        ConfigInterfaces configuration = beanLocator.get(ConfigInterfaces.NAME);
        return configuration.getConfig(ClientConfig.class);
    }

    protected void onCloseButtonClick(JmixWindow.PreCloseEvent preCloseEvent) {
        preCloseEvent.setPreventClose(true);

        Component component = getComponent();
        AppUI ui = (AppUI) component.getUI();
        if (!ui.isAccessibleForUser(component)) {
            LoggerFactory.getLogger(WebWindow.class)
                    .debug("Ignore close button click because Window is inaccessible for user");
            return;
        }

        BeforeCloseEvent event = new BeforeCloseEvent(this, CloseOriginType.CLOSE_BUTTON);
        fireBeforeClose(event);

        if (!event.isClosePrevented()) {
            // user has clicked on X
            getFrameOwner().close(new StandardCloseAction(Window.CLOSE_ACTION_ID));
        }
    }

    protected void onCloseShortcutTriggered(@SuppressWarnings("unused") Object sender,
                                            @SuppressWarnings("unused") Object target) {
        if (this.isCloseable()) {
            Component component = getComponent();
            AppUI ui = (AppUI) component.getUI();
            if (!ui.isAccessibleForUser(component)) {
                LoggerFactory.getLogger(WebWindow.class)
                        .debug("Ignore shortcut action because Window is inaccessible for user");
                return;
            }

            BeforeCloseEvent event = new BeforeCloseEvent(this, CloseOriginType.SHORTCUT);
            fireBeforeClose(event);

            if (!event.isClosePrevented()) {
                getFrameOwner().close(new StandardCloseAction(Window.CLOSE_ACTION_ID));
            }
        }
    }

    @Inject
    public void setBeanLocator(BeanLocator beanLocator) {
        this.beanLocator = beanLocator;
    }

    @Override
    public void setIcon(String icon) {
        super.setIcon(icon);

        if (icon == null) {
            dialogWindow.setIcon(null);
        } else {
            IconResolver iconResolver = beanLocator.get(IconResolver.NAME);
            dialogWindow.setIcon(iconResolver.getIconResource(icon));
        }
    }

    @Override
    public void setCaption(String caption) {
        super.setCaption(caption);

        this.dialogWindow.setCaption(caption);
    }

    @Override
    public void setDescription(String description) {
        super.setDescription(description);

        this.dialogWindow.setDescription(description);
    }

    @Override
    public Component getComposition() {
        return dialogWindow;
    }

    @Override
    public void setDialogWidth(String dialogWidth) {
        dialogWindow.setWidth(dialogWidth);

        if (dialogWindow.getWidth() < 0) {
            component.setWidthUndefined();
        } else {
            component.setWidth(100, Sizeable.Unit.PERCENTAGE);
        }
    }

    @Override
    public float getDialogWidth() {
        return dialogWindow.getWidth();
    }

    @Override
    public SizeUnit getDialogWidthUnit() {
        return toSizeUnit(dialogWindow.getWidthUnits());
    }

    @Override
    public void setDialogHeight(String dialogHeight) {
        dialogWindow.setHeight(dialogHeight);

        if (dialogWindow.getHeight() < 0) {
            component.setHeightUndefined();
        } else {
            component.setHeight(100, Sizeable.Unit.PERCENTAGE);
        }
    }

    @Override
    public float getDialogHeight() {
        return dialogWindow.getHeight();
    }

    @Override
    public SizeUnit getDialogHeightUnit() {
        return toSizeUnit(dialogWindow.getHeightUnits());
    }

    @Override
    public void setDialogStylename(String stylename) {
        dialogWindow.setStyleName(stylename);
    }

    @Override
    public String getDialogStylename() {
        return dialogWindow.getStyleName();
    }

    @Override
    public void setResizable(boolean resizable) {
        dialogWindow.setResizable(resizable);
    }

    @Override
    public boolean isResizable() {
        return dialogWindow.isResizable();
    }

    @Override
    public void setDraggable(boolean draggable) {
        dialogWindow.setDraggable(draggable);
    }

    @Override
    public boolean isDraggable() {
        return dialogWindow.isDraggable();
    }

    @Override
    public void setCloseable(boolean closeable) {
        super.setCloseable(closeable);

        dialogWindow.setClosable(closeable);
    }

    @Override
    public void setModal(boolean modal) {
        dialogWindow.setModal(modal);
    }

    @Override
    public boolean isModal() {
        return dialogWindow.isModal();
    }

    @Override
    public void setCloseOnClickOutside(boolean closeOnClickOutside) {
        dialogWindow.setCloseOnClickOutside(closeOnClickOutside);
    }

    @Override
    public boolean isCloseOnClickOutside() {
        return dialogWindow.getCloseOnClickOutside();
    }

    @Override
    public void setWindowMode(WindowMode mode) {
        dialogWindow.setWindowMode(com.vaadin.shared.ui.window.WindowMode.valueOf(mode.name()));
    }

    @Override
    public WindowMode getWindowMode() {
        return WindowMode.valueOf(dialogWindow.getWindowMode().name());
    }

    @Override
    public void center() {
        dialogWindow.center();
    }

    @Override
    public void setPositionX(int positionX) {
        dialogWindow.setPositionX(positionX);
    }

    @Override
    public int getPositionX() {
        return dialogWindow.getPositionX();
    }

    @Override
    public void setPositionY(int positionY) {
        dialogWindow.setPositionY(positionY);
    }

    @Override
    public int getPositionY() {
        return dialogWindow.getPositionY();
    }

    protected class DialogWindowActionHandler implements Action.Handler {

        protected Action saveSettingsAction;
        protected Action restoreToDefaultsAction;

        protected Action analyzeAction;

        protected boolean initialized = false;

        public DialogWindowActionHandler() {
        }

        @Override
        public Action[] getActions(Object target, Object sender) {
            if (!initialized) {
                Messages messages = beanLocator.get(Messages.NAME);

                saveSettingsAction = new Action(messages.getMessage("actions.saveSettings"));
                restoreToDefaultsAction = new Action(messages.getMessage("actions.restoreToDefaults"));
                analyzeAction = new Action(messages.getMessage("actions.analyzeLayout"));

                initialized = true;
            }

            List<Action> actions = new ArrayList<>(3);

            ClientConfig clientConfig = getClientConfig();
            if (clientConfig.getManualScreenSettingsSaving()) {
                actions.add(saveSettingsAction);
                actions.add(restoreToDefaultsAction);
            }
            if (clientConfig.getLayoutAnalyzerEnabled()) {
                actions.add(analyzeAction);
            }

            return actions.toArray(new Action[0]);
        }

        @Override
        public void handleAction(Action action, Object sender, Object target) {
            // todo actions

            /*if (initialized) {
                if (saveSettingsAction == action) {
                    Screen screen = getFrameOwner();
                    UiControllerUtils.saveSettings(screen);
                } else if (restoreToDefaultsAction == action) {
                    Screen screen = getFrameOwner();
                    UiControllerUtils.deleteSettings(screen);
                } else if (analyzeAction == action) {
                    LayoutAnalyzer analyzer = new LayoutAnalyzer();
                    List<LayoutTip> tipsList = analyzer.analyze(WebDialogWindow.this);

                    if (tipsList.isEmpty()) {
                        getWindowManager().showNotification("No layout problems found", Frame.NotificationType.HUMANIZED);
                    } else {
                        WindowConfig windowConfig = beanLocator.get(WindowConfig.NAME);
                        WindowInfo windowInfo = windowConfig.getWindowInfo("layoutAnalyzer");
                        getWindowManager().openWindow(windowInfo, OpenType.DIALOG, ParamsMap.of("tipsList", tipsList));
                    }
                }
            }*/
        }
    }

    public static class GuiDialogWindow extends JmixWindow {
        protected DialogWindow dialogWindow;

        public GuiDialogWindow(DialogWindow dialogWindow) {
            this.dialogWindow = dialogWindow;

            setStyleName("c-app-dialog-window");
            setModal(true);
            setResizable(false);
            center();
        }

        public DialogWindow getDialogWindow() {
            return dialogWindow;
        }
    }
}