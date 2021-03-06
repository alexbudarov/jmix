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

/*
 * Licensed under the Apache License,Version2.0(the"License");you may not
 * use this file except in compliance with the License.You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,software
 * distributed under the License is distributed on an"AS IS"BASIS,WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.ui.widgets.client.renderers.componentrenderer.focuspreserve;

import com.vaadin.shared.communication.ClientRpc;

/**
 * Provides rpc-methods to save and restore the current focus of the grid. This
 * is needed to preserve the current focus when issuing a full rerendering of
 * the grid.
 *
 * <ul>
 *     <li>save the current focus using {@link #saveFocus()}</li>
 *     <li>rerender the grid</li>
 *     <li>restore the current focus using {@link #restoreFocus()}</li>
 * </ul>
 *
 * @author Jonas Hahn (jonas.hahn@datenhahn.de)
 */
public interface FocusPreservingRefreshClientRpc extends ClientRpc {

    /**
     * Saves the grid's current focus in this extension's internal state.
     */
    void saveFocus();

    /**
     * Restores the grid's focus from  extension's internal state.
     */
    void restoreFocus();
}
