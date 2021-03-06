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

package io.jmix.ui.components;

import io.jmix.ui.UiComponents;
import io.jmix.ui.screen.ScreenFragment;

/**
 * Reusable part of {@link Window} with separate UI controller.
 *
 * @see ScreenFragment
 */
public interface Fragment extends Frame {
    /**
     * Name that is used to register a client type specific screen implementation in
     * {@link UiComponents}
     */
    String NAME = "fragment";

    @Override
    ScreenFragment getFrameOwner();
}