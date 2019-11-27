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

package io.jmix.ui.components.filter.edit;

import com.haulmont.bali.datastruct.Tree;
import com.haulmont.cuba.gui.components.filter.ConditionsTree;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import com.haulmont.cuba.gui.data.impl.AbstractTreeDatasource;

import java.util.Map;
import java.util.UUID;

/**
 * Datasource for conditions tree in generic filter editor
 *
 */
public class ConditionsDs extends AbstractTreeDatasource<AbstractCondition, UUID> {

    protected ConditionsTree conditionsTree;

    @Override
    protected Tree loadTree(Map params) {
        conditionsTree = (ConditionsTree) params.get("conditions");
        return conditionsTree;
    }

}