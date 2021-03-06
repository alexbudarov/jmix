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

package io.jmix.ui.sys.delegates;

import io.jmix.ui.screen.FrameOwner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class InstalledFunction implements Function {
    private final FrameOwner frameOwner;
    private final Method method;

    public InstalledFunction(FrameOwner frameOwner, Method method) {
        this.frameOwner = frameOwner;
        this.method = method;
    }

    @Override
    public Object apply(Object o) {
        try {
            return method.invoke(frameOwner, o);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Exception on @Install invocation", e);
        }
    }

    @Override
    public String toString() {
        return "InstalledFunction{" +
                "frameOwner=" + frameOwner.getClass() +
                ", method=" + method +
                '}';
    }
}