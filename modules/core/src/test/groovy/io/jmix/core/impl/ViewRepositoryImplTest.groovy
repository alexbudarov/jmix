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

package io.jmix.core.impl

import com.sample.addon1.TestAddon1Configuration
import com.sample.addon1.entity.TestAddon1Entity
import com.sample.app.AppContextTestExecutionListener
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.ViewRepository
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import spock.lang.Specification

import javax.inject.Inject

@ContextConfiguration(classes = [JmixCoreConfiguration, TestAddon1Configuration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class ViewRepositoryImplTest extends Specification {

    @Inject
    ViewRepository viewRepository

    def "view repository is initialized"() {

        when:

        def view = viewRepository.getView(TestAddon1Entity, 'test-view-1')

        then:

        noExceptionThrown()
        view.getProperty('name') != null
    }
}
