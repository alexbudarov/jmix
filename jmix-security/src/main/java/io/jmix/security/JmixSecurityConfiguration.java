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

package io.jmix.security;

import io.jmix.core.JmixCoreConfiguration;
import io.jmix.core.annotation.JmixComponent;
import io.jmix.core.annotation.JmixProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@JmixComponent(dependsOn = JmixCoreConfiguration.class, properties = {
        @JmixProperty(name = "jmix.viewsConfig", value = "io/jmix/security/views.xml", append = true),
        @JmixProperty(name = "jmix.defaultPermissionValuesConfig", value = "io/jmix/security/default-permission-values.xml")
})
@Import(StandardSecurityConfiguration.class)
public class JmixSecurityConfiguration {
}
