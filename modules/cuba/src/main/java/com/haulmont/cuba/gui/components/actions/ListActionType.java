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

import io.jmix.ui.Actions;
import io.jmix.ui.actions.Action;
import io.jmix.ui.components.ListComponent;

/**
 * Enumerates standard list action types. Can create a corresponding action instance with default parameters.
 *
 * @deprecated Use {@link Actions} instead.
 */
@Deprecated
public enum ListActionType {

    CREATE("create") {
        @Override
        public Action createAction(ListComponent holder) {
            return LegacyCreateAction.create(holder);
        }
    },

    EDIT("edit") {
        @Override
        public Action createAction(ListComponent holder) {
            return LegacyEditAction.create(holder);
        }
    },

    REMOVE("remove") {
        @Override
        public Action createAction(ListComponent holder) {
            return LegacyRemoveAction.create(holder);
        }
    },

    REFRESH("refresh") {
        @Override
        public Action createAction(ListComponent holder) {
            return LegacyRefreshAction.create(holder);
        }
    },

    ADD("add") {
        @Override
        public Action createAction(ListComponent holder) {
            return LegacyAddAction.create(holder);
        }
    },

    EXCLUDE("exclude") {
        @Override
        public Action createAction(ListComponent holder) {
            return LegacyExcludeAction.create(holder);
        }
    };

    // todo excel action
//    EXCEL("excel") {
//        @Override
//        public Action createAction(ListComponent holder) {
//            if (holder instanceof Table || holder instanceof DataGrid)
//                return LegacyExcelAction.create(holder);
//            else
//                throw new IllegalArgumentException("Only Table and DataGrid can contain EXCEL action");
//        }
//    };

    private String id;

    ListActionType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract Action createAction(ListComponent holder);
}
