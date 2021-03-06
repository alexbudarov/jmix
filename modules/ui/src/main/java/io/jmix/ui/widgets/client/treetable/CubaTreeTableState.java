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

package io.jmix.ui.widgets.client.treetable;

import com.vaadin.shared.Connector;
import com.vaadin.shared.annotations.NoLayout;
import com.vaadin.v7.shared.ui.treetable.TreeTableState;

import java.util.Map;

public class CubaTreeTableState extends TreeTableState {

    @NoLayout
    public boolean multiLineCells = false;

    @NoLayout
    public boolean textSelectionEnabled = false;

    @NoLayout
    public boolean contextMenuEnabled = true;

    @NoLayout
    public Connector presentations;

    @NoLayout
    public Connector contextMenu;

    @NoLayout
    public Connector customPopup;

    @NoLayout
    public boolean customPopupAutoClose = false;

    public String[] clickableColumnKeys;

    @NoLayout
    public Map<String, String> columnDescriptions;

    @NoLayout
    public Map<String, String> aggregationDescriptions;
    
    @NoLayout
    public String tableSortResetLabel;

    @NoLayout
    public String tableSortAscendingLabel;

    @NoLayout
    public String tableSortDescendingLabel;

    @NoLayout
    public String[] htmlCaptionColumns;

    @NoLayout
    public boolean showEmptyState;

    @NoLayout
    public String emptyStateMessage;

    @NoLayout
    public String emptyStateLinkMessage;

    @NoLayout
    public String selectAllLabel;

    @NoLayout
    public String deselectAllLabel;
}