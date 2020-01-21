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

import com.haulmont.cuba.gui.components.LegacyListComponent;
import io.jmix.ui.actions.ListAction;
import io.jmix.ui.components.ListComponent;

import javax.annotation.Nullable;

public class LegacyListAction extends ListAction {

    protected LegacyListComponent target;

    public LegacyListAction(String id) {
        super(id);
    }

    public LegacyListAction(String id, @Nullable String shortcut) {
        super(id, shortcut);
    }

    @Override
    public LegacyListComponent getTarget() {
        return (LegacyListComponent) super.getTarget();
    }

    @Override
    public void setTarget(ListComponent target) {
        super.setTarget(target);
    }
}
