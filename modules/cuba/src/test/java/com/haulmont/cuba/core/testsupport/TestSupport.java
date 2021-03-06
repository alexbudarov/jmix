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

package com.haulmont.cuba.core.testsupport;

import io.jmix.core.AppBeans;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.Entity;
import io.jmix.core.impl.StandardSerialization;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.Persistence;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serializable;

public class TestSupport {

    public static <T> T reserialize(Serializable object) throws Exception {
        if (object == null)
            return null;

        return (T) StandardSerialization.deserialize(StandardSerialization.serialize(object));
    }

    public static void deleteRecord(String table, Object... ids) {
        deleteRecord(table, "ID", ids);
    }

    public static void deleteRecord(String table, String primaryKeyCol, Object... ids) {
        Persistence persistence = AppBeans.get(Persistence.class);
        for (Object id : ids) {
            String sql = "delete from " + table + " where " + primaryKeyCol + " = '" + id.toString() + "'";
            JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
            try {
                jdbcTemplate.update(sql);
            } catch (DataAccessException e) {
                throw new RuntimeException(
                        String.format("Unable to delete record %s from %s", id.toString(), table), e);
            }
        }
    }

    public static void deleteRecord(Entity... entities) {
        if (entities == null) {
            return;
        }

        for (Entity entity : entities) {
            if (entity == null) {
                continue;
            }

            Metadata metadata = AppBeans.get(Metadata.class);
            MetadataTools metadataTools = metadata.getTools();
            MetaClass metaClass = metadata.getClassNN(entity.getClass());

            String table = metadataTools.getDatabaseTable(metaClass);
            String primaryKey = metadataTools.getPrimaryKeyName(metaClass);
            if (table == null || primaryKey == null) {
                throw new RuntimeException("Unable to determine table or primary key name for " + entity);
            }

            deleteRecord(table, primaryKey, entity.getId());
        }
    }
}