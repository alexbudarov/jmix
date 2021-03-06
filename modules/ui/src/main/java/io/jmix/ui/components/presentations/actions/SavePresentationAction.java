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

package io.jmix.ui.components.presentations.actions;

import io.jmix.core.entity.Presentation;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.Table;
import io.jmix.ui.presentations.Presentations;
import org.dom4j.Element;

public class SavePresentationAction extends AbstractPresentationAction {

    public SavePresentationAction(Table table) {
        super(table, "PresentationsPopup.save");
    }

    @Override
    public void actionPerform(Component component) {
        tableImpl.hidePresentationsPopup();

        Presentations presentations = table.getPresentations();
        Presentation current = presentations.getCurrent();
        Element e = presentations.getSettings(current);
        table.saveSettings(e);
        presentations.setSettings(current, e);
        presentations.commit();
    }
}
