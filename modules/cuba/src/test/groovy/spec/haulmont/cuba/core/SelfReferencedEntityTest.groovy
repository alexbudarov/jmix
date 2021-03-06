/*
 * Copyright (c) 2008-2019 Haulmont.
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

package spec.haulmont.cuba.core

import com.haulmont.cuba.core.model.self_reference.SelfReferencedEntity
import io.jmix.core.DataManager
import io.jmix.core.Metadata

import javax.inject.Inject

class SelfReferencedEntityTest extends CoreTestSpecification {
    @Inject
    private Metadata metadata
    @Inject
    private DataManager dataManager

    private parent
    private childe

    def "load test"() {
        setup:

        parent = metadata.create(SelfReferencedEntity.class)
        parent.setCode("1")
        dataManager.commit(parent)

        childe = metadata.create(SelfReferencedEntity.class)
        childe.setCode("2")
        childe.setParent(parent)
        dataManager.commit(childe)

        when:

        def testHierarchy = dataManager
                .load(SelfReferencedEntity.class)
                .id("1")
                .view("entityWithChildren")
                .one()

        then:

        testHierarchy.getChildren().size() == 1

        cleanup:

        dataManager.remove(childe)
        dataManager.remove(parent)
    }
}
