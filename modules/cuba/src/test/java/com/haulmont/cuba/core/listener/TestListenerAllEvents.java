/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.core.listener;

import com.haulmont.cuba.core.model.common.Server;
import io.jmix.core.AppBeans;
import io.jmix.core.commons.db.ArrayHandler;
import io.jmix.core.commons.db.QueryRunner;
import io.jmix.data.EntityManager;
import io.jmix.data.Persistence;
import io.jmix.data.listener.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestListenerAllEvents implements
        BeforeInsertEntityListener<Server>,
        BeforeUpdateEntityListener<Server>,
        BeforeDeleteEntityListener<Server>,
        AfterInsertEntityListener<Server>,
        AfterUpdateEntityListener<Server>,
        AfterDeleteEntityListener<Server> {
    protected Persistence persistence = AppBeans.get(Persistence.class);

    public static final List<String> events = new ArrayList<>();

    private Object[] selectObjectFromDb(String sql, Server entity) {
        QueryRunner queryRunner = new QueryRunner();
        try {
            return queryRunner.query(persistence.getEntityManager().getConnection(),
                    sql,
                    new Object[]{entity.getId().toString()},
                    new ArrayHandler()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onBeforeInsert(Server entity, EntityManager entityManager) {
        Object[] objects = selectObjectFromDb("select id from test_server where id = ?", entity);
        assertNull(objects);

        events.add("onBeforeInsert: " + entity.getId());
    }

    @Override
    public void onAfterInsert(Server entity, Connection connection) {
        Object[] objects = selectObjectFromDb("select id from test_server where id = ?", entity);
        assertEquals(entity.getId().toString(), objects[0]);

        events.add("onAfterInsert: " + entity.getId());
    }

    @Override
    public void onBeforeUpdate(Server entity, EntityManager entityManager) {
        Object[] objects = selectObjectFromDb("select name from test_server where id = ?", entity);
        assertEquals("localhost", objects[0]);

        events.add("onBeforeUpdate: " + entity.getId());
    }

    @Override
    public void onAfterUpdate(Server entity, Connection connection) {
        Object[] objects = selectObjectFromDb("select name from test_server where id = ?", entity);
        assertEquals("changed", objects[0]);

        events.add("onAfterUpdate: " + entity.getId());
    }

    @Override
    public void onBeforeDelete(Server entity, EntityManager entityManager) {
        Object[] objects = selectObjectFromDb("select id from test_server where id = ?", entity);
        assertEquals(entity.getId().toString(), objects[0]);

        events.add("onBeforeDelete: " + entity.getId());
    }

    @Override
    public void onAfterDelete(Server entity, Connection connection) {
        Object[] objects = selectObjectFromDb("select id from test_server where id = ?", entity);
        assertNull(objects);

        events.add("onAfterDelete: " + entity.getId());
    }
}
