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

package io.jmix.remoting

import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.impl.ConfigStorage
import io.jmix.data.JmixDataConfiguration
import io.jmix.remoting.test.JmixRemotingTestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

import javax.inject.Inject
import java.lang.reflect.Proxy

@ContextConfiguration(classes = [JmixCoreConfiguration, JmixDataConfiguration, JmixRemotingConfiguration,
        JmixRemotingTestConfiguration])
@TestPropertySource("classpath:/io/jmix/remoting/client.properties")
class ClientTest extends Specification {

    @Inject
    ConfigStorage configStorage
    @Inject
    ApplicationContext applicationContext

    def "context has correct beans"() {
        expect:

        configStorage instanceof ConfigStorageClient

        applicationContext.containsBean(ConfigStorageService.NAME)
        applicationContext.getBean(ConfigStorageService.NAME) instanceof Proxy

        !applicationContext.containsBean('/remoting/' + ConfigStorageService.NAME)
    }
}
