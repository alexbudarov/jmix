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

import com.google.common.reflect.TypeToken;
import io.jmix.core.entity.Entity;

/**
 * LookupPickerField adds to PickerField the ability to select an entity from drop-down list.
 */
public interface LookupPickerField<V extends Entity> extends LookupField<V>, PickerField<V> {

    String NAME = "lookupPickerField";

    static <T extends Entity> TypeToken<LookupPickerField<T>> of(Class<T> valueClass) {
        return new TypeToken<LookupPickerField<T>>() {};
    }

    /**
     * Use this method to enable items refreshing in component after closing lookup window.
     *
     * @deprecated Override LookupAction if needed instead of using this option.
     */
    @Deprecated
    void setRefreshOptionsOnLookupClose(boolean refresh);
    boolean isRefreshOptionsOnLookupClose();
}