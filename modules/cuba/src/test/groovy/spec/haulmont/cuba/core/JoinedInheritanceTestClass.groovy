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

import com.haulmont.cuba.core.model.selfinherited.ChildEntity
import com.haulmont.cuba.core.model.selfinherited.ChildEntityDetail
import com.haulmont.cuba.core.model.selfinherited.ChildEntityReferrer
import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.core.commons.db.QueryRunner
import io.jmix.data.Persistence

import javax.inject.Inject

class JoinedInheritanceTestClass extends CoreTestSpecification {
    @Inject
    private Persistence persistence
    @Inject
    private Metadata metadata
    @Inject
    private DataManager dataManager


    void cleanup() {
        def runner = new QueryRunner(persistence.dataSource)
        runner.update('delete from TEST_CHILD_ENTITY_DETAIL')
        runner.update('delete from TEST_ROOT_ENTITY_DETAIL')
        runner.update('delete from TEST_CHILD_ENTITY_REFERRER')
        runner.update('delete from TEST_CHILD_ENTITY')
        runner.update('delete from TEST_ROOT_ENTITY')
    }

    def "store master-detail"() {
        when:
        persistence.runInTransaction({ em ->
            ChildEntity childEntity = metadata.create(ChildEntity)
            childEntity.name = 'name'
            childEntity.description = 'description'
            em.persist(childEntity)

            ChildEntityDetail childEntityDetail = metadata.create(ChildEntityDetail)
            childEntityDetail.childEntity = childEntity
            childEntityDetail.info = 'info'
            em.persist(childEntityDetail)
        })

        then:
        noExceptionThrown()
    }

    def "store root-joined-inheritance-and-referer"() {
        when:
        persistence.runInTransaction({ em ->
            ChildEntity childEntity = metadata.create(ChildEntity)
            childEntity.name = 'name'
            childEntity.description = 'description'
            em.persist(childEntity)

            ChildEntityReferrer childEntityReferrer = metadata.create(ChildEntityReferrer)
            childEntityReferrer.childEntity = childEntity
            childEntityReferrer.info = 'info'
            em.persist(childEntityReferrer)
        })

        then:
        noExceptionThrown()
    }
}
