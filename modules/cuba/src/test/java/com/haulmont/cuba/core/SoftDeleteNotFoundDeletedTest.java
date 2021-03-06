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
package com.haulmont.cuba.core;

import com.haulmont.cuba.core.model.SoftDelete_Project;
import com.haulmont.cuba.core.model.SoftDelete_Service;
import com.haulmont.cuba.core.model.SoftDelete_Task;
import com.haulmont.cuba.core.model.SoftDelete_TaskValue;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.View;
import io.jmix.data.EntityManager;
import io.jmix.data.Persistence;
import io.jmix.data.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

@CoreTest
public class SoftDeleteNotFoundDeletedTest {
    @Inject
    private Persistence persistence;
    @Inject
    private DataManager dataManager;

    private UUID taskId;
    private UUID serviceId;
    private UUID projectId;
    private UUID taskValueId;

    @BeforeEach
    public void setUp() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            SoftDelete_Service service = new SoftDelete_Service();
            serviceId = service.getId();
            service.setName("service");
            service.setCode("serviceCode");
            em.persist(service);

            SoftDelete_Task task = new SoftDelete_Task();
            taskId = task.getId();
            task.setMessage("message");
            task.setService(service);
            em.persist(task);

            SoftDelete_TaskValue taskValue = new SoftDelete_TaskValue();
            taskValueId = taskValue.getId();
            taskValue.setTask(task);
            em.persist(taskValue);

            SoftDelete_Project project = new SoftDelete_Project();
            projectId = project.getId();
            project.setName("project");
            project.setAValue(taskValue);
            project.setTask(task);
            em.persist(project);

            tx.commitRetaining();

            em = persistence.getEntityManager();

            task = em.find(SoftDelete_Task.class, taskId);
            em.remove(task);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestSupport.deleteRecord("TEST_SOFT_DELETE_PROJECT", projectId);
        TestSupport.deleteRecord("TEST_SOFT_DELETE_TASK_VALUE", taskValueId);
        TestSupport.deleteRecord("TEST_SOFT_DELETE_TASK", taskId);
        TestSupport.deleteRecord("TEST_SOFT_DELETE_SERVICE", serviceId);
    }

    @Test
    public void testLoadDeletedObject() {
        View taskView_Message = new View(SoftDelete_Task.class).addProperty("message");
        View taskView_Service = new View(SoftDelete_Task.class)
                .addProperty("service", new View(SoftDelete_Service.class).addProperty("code"));
        View taskValueView = new View(SoftDelete_TaskValue.class)
                .addProperty("task", taskView_Message);

        View projectView = new View(SoftDelete_Project.class)
                .addProperty("name")
                .addProperty("aValue", taskValueView)
                .addProperty("task", taskView_Service);

        LoadContext<SoftDelete_Project> loadContext = new LoadContext<>(SoftDelete_Project.class)
                .setView(projectView).setId(projectId);
        SoftDelete_Project project = dataManager.load(loadContext);

        Assertions.assertNotNull(project);
        Assertions.assertNotNull(project.getTask());
        Assertions.assertTrue(project.getTask().isDeleted());
    }
}
