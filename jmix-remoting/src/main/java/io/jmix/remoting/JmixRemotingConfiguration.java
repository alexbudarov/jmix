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

package io.jmix.remoting;

import io.jmix.core.DataManager;
import io.jmix.core.annotation.JmixComponent;
import io.jmix.core.impl.ConfigStorage;
import io.jmix.data.JmixDataConfiguration;
import io.jmix.remoting.gateway.ConfigStorageClient;
import io.jmix.remoting.gateway.DataManagerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan
@JmixComponent(dependsOn = JmixDataConfiguration.class)
public class JmixRemotingConfiguration {

    @Bean(name = DataManager.NAME)
    @Profile("client")
    public DataManager dataManager() {
        return new DataManagerClient();
    }

    @Bean(name = ConfigStorage.NAME)
    @Profile("client")
    public ConfigStorage configStorage() {
        return new ConfigStorageClient();
    }
}
