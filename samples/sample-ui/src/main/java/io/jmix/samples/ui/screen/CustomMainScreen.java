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

package io.jmix.samples.ui.screen;

import io.jmix.core.AppBeans;
import io.jmix.core.ConfigInterfaces;
import io.jmix.core.Metadata;
import io.jmix.core.impl.MetadataImpl;
import io.jmix.samples.ui.screen.component.ComponentSamples;
import io.jmix.samples.ui.screen.user.UserBrowse;
import io.jmix.ui.*;
import io.jmix.ui.builders.ScreenBuilder;
import io.jmix.ui.components.*;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Base class for a controller of application Main screen.
 */
@UiDescriptor("custom-main-screen.xml")
@UiController("custom-main")
public class CustomMainScreen extends Screen implements Window.HasWorkArea {
    @Inject
    protected Notifications notifications;
    @Inject
    private Metadata metadata;
    @Inject
    private UiComponents uiComponents;
    @Inject
    private ScreenBuilders screenBuilders;


    @Subscribe
    protected void afterShow(AfterShowEvent event) {
        notifications.create(Notifications.NotificationType.TRAY)
                .withCaption("Welcome to Custom main screen")
                .show();

        Button button = uiComponents.create(Button.class);
        button.setCaption("Users");
        button.addClickListener(clickEvent -> {
            screenBuilders
                    .screen(this)
                    .withScreenClass(UserBrowse.class)
                    .withLaunchMode(OpenMode.NEW_TAB)
                    .show();
        });

        VBoxLayout sideMenuPanel = (VBoxLayout) getWindow().getComponentNN("sideMenuPanel");
        sideMenuPanel.add(button);

        Button componentSamplesBtn = uiComponents.create(Button.class);
        componentSamplesBtn.setCaption("Component samples");
        componentSamplesBtn.addClickListener(clickEvent -> {
            screenBuilders.screen(this)
                    .withScreenClass(ComponentSamples.class)
                    .withLaunchMode(OpenMode.NEW_TAB)
                    .show();
        });
        sideMenuPanel.add(componentSamplesBtn);

        ClientConfig clientConfig = AppBeans.get(ConfigInterfaces.class).getConfig(ClientConfig.class);
        clientConfig.getCloseShortcut();
    }

    @Nullable
    @Override
    public AppWorkArea getWorkArea() {
        return (AppWorkArea) getWindow().getComponent("workArea");
    }
}
