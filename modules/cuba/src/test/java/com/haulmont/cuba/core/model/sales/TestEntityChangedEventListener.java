/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.cuba.core.model.sales;

import io.jmix.core.Id;
import io.jmix.core.commons.db.ArrayHandler;
import io.jmix.core.commons.db.QueryRunner;
import io.jmix.core.entity.Entity;
import io.jmix.data.EntityManager;
import io.jmix.data.Persistence;
import io.jmix.data.event.EntityChangedEvent;
import io.jmix.data.event.EntityPersistingEvent;
import io.jmix.data.listener.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

@Component("test_EntityChangedEventListener")
public class TestEntityChangedEventListener implements
        BeforeCommitTransactionListener,
        AfterCompleteTransactionListener,
        BeforeInsertEntityListener<Order>,
        BeforeUpdateEntityListener<Order>,
        BeforeDeleteEntityListener<Order>,
        AfterInsertEntityListener<Order>,
        AfterUpdateEntityListener<Order>,
        AfterDeleteEntityListener<Order>,
        BeforeDetachEntityListener<Order>,
        BeforeAttachEntityListener<Order> {

    public static class EventInfo {
        public final String message;
        public final Object[] payload;

        public EventInfo(String message, Object payload) {
            this.message = message;
            this.payload = new Object[]{payload};
        }

        public EventInfo(String message, Object[] payload) {
            this.message = message;
            this.payload = payload;
        }

        @Override
        public String toString() {
            return message + " { " + (payload == null ? null : Arrays.asList(payload)) + " }";
        }
    }

    public static class Info {
        public final EntityChangedEvent event;
        public final boolean committedToDb;

        public Info(EntityChangedEvent event, boolean committedToDb) {
            this.event = event;
            this.committedToDb = committedToDb;
        }
    }

    public List<Info> entityChangedEvents = new ArrayList<>();

    public List<EventInfo> allEvents = new ArrayList<>();

    @Inject
    private Persistence persistence;

    public void clear() {
        allEvents.clear();
        entityChangedEvents.clear();
    }

    @EventListener
    void beforePersist(EntityPersistingEvent<Order> event) {
        allEvents.add(new EventInfo("EntityPersistingEvent", event));
    }

    @EventListener
    void beforeCommit(EntityChangedEvent<Order, UUID> event) {
        allEvents.add(new EventInfo("EntityChangedEvent: beforeCommit, " + event.getType(), event));
        entityChangedEvents.add(new Info(event, isCommitted(event.getEntityId())));
    }

    @TransactionalEventListener
    void afterCommit(EntityChangedEvent<Order, UUID> event) {
        allEvents.add(new EventInfo("EntityChangedEvent: afterCommit, " + event.getType(), event));
        entityChangedEvents.add(new Info(event, isCommitted(event.getEntityId())));
    }

    @Override
    public void beforeCommit(EntityManager entityManager, Collection<Entity> managedEntities) {
        allEvents.add(new EventInfo("BeforeCommitTransactionListener", managedEntities));
    }

    @Override
    public void afterComplete(boolean committed, Collection<Entity> detachedEntities) {
        allEvents.add(new EventInfo("AfterCompleteTransactionListener", new Object[]{committed, detachedEntities}));
    }

    @Override
    public void onAfterDelete(Order entity, Connection connection) {
        allEvents.add(new EventInfo("AfterDeleteEntityListener", entity));
    }

    @Override
    public void onAfterInsert(Order entity, Connection connection) {
        allEvents.add(new EventInfo("AfterInsertEntityListener", entity));
    }

    @Override
    public void onAfterUpdate(Order entity, Connection connection) {
        allEvents.add(new EventInfo("AfterUpdateEntityListener", entity));
    }

    @Override
    public void onBeforeAttach(Order entity) {
        allEvents.add(new EventInfo("BeforeAttachEntityListener", entity));
    }

    @Override
    public void onBeforeDelete(Order entity, EntityManager entityManager) {
        allEvents.add(new EventInfo("BeforeDeleteEntityListener", entity));
    }

    @Override
    public void onBeforeDetach(Order entity, EntityManager entityManager) {
        allEvents.add(new EventInfo("BeforeDetachEntityListener", entity));
    }

    @Override
    public void onBeforeInsert(Order entity, EntityManager entityManager) {
        allEvents.add(new EventInfo("BeforeInsertEntityListener", entity));
    }

    @Override
    public void onBeforeUpdate(Order entity, EntityManager entityManager) {
        allEvents.add(new EventInfo("BeforeUpdateEntityListener", entity));
    }

    private boolean isCommitted(Id<Order, UUID> entityId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> {
            QueryRunner runner = new QueryRunner(persistence.getDataSource());
            try {
                Object[] row = runner.query("select id from TEST_ORDER where id = ?",
                        entityId.getValue().toString(),
                        new ArrayHandler());
                return row != null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            return future.get(200L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            return false;
        }
    }

}
