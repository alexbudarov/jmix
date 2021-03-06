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

import io.jmix.core.commons.events.Subscription;
import io.jmix.ui.components.Button;
import io.jmix.ui.components.ClipboardTrigger;
import io.jmix.ui.components.TextInputField;
import io.jmix.ui.widgets.CubaCopyButtonExtension;
import io.jmix.ui.widgets.CubaCopyButtonExtension;

import java.util.function.Consumer;

import static io.jmix.ui.widgets.CubaCopyButtonExtension.browserSupportCopy;
import static io.jmix.ui.widgets.CubaCopyButtonExtension.copyWith;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class WebClipboardTrigger extends WebAbstractFacet implements ClipboardTrigger {

    protected TextInputField<?> input;
    protected Button button;

    @Override
    public void setInput(TextInputField<?> input) {
        this.input = input;

        checkInitialized();
    }

    @Override
    public TextInputField<?> getInput() {
        return input;
    }

    @Override
    public void setButton(Button button) {
        if (this.button != null) {
            disableExtension(this.button);
        }

        this.button = button;

        checkInitialized();
    }

    @Override
    public Button getButton() {
        return button;
    }

    @Override
    public boolean isSupportedByWebBrowser() {
        return browserSupportCopy();
    }

    @Override
    public Subscription addCopyListener(Consumer<CopyEvent> listener) {
        return getEventHub().subscribe(CopyEvent.class, listener);
    }

    protected void disableExtension(Button button) {
        button.withUnwrapped(com.vaadin.ui.Button.class, vButton ->
                vButton.getExtensions().stream()
                        .filter(e -> e instanceof CubaCopyButtonExtension)
                        .findFirst()
                        .ifPresent(vButton::removeExtension));
    }

    protected void checkInitialized() {
        if (this.button != null &&
            this.input != null) {
            // setup field CSS class for selector
            String generatedClassName = "copy-text-" + randomAlphanumeric(6);

            this.input.addStyleName(generatedClassName);

            button.withUnwrapped(com.vaadin.ui.Button.class, vButton -> {
                disableExtension(this.button);

                CubaCopyButtonExtension extension = copyWith(vButton, "." + generatedClassName);
                extension.addCopyListener(event ->
                        publish(CopyEvent.class, new CopyEvent(this, event.isSuccess()))
                );
            });
        }
    }
}
