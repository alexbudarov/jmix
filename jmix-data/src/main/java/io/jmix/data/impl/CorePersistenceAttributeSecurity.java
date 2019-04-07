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

package io.jmix.data.impl;

import io.jmix.core.security.ConditionalOnSecurityImplementation;
import io.jmix.core.View;
import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.PersistenceAttributeSecurity;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component(PersistenceAttributeSecurity.NAME)
@ConditionalOnSecurityImplementation("core")
public class CorePersistenceAttributeSecurity implements PersistenceAttributeSecurity {

    @Override
    public View createRestrictedView(View view) {
        return view;
    }

    @Override
    public void afterLoad(Entity entity) {

    }

    @Override
    public void afterLoad(Collection<? extends Entity> entities) {

    }

    @Override
    public void beforePersist(Entity entity) {

    }

    @Override
    public void afterPersist(Entity entity, View view) {

    }

    @Override
    public void beforeMerge(Entity entity) {

    }

    @Override
    public void afterMerge(Entity entity) {

    }

    @Override
    public void afterCommit(Entity entity) {

    }

    @Override
    public void onLoad(Collection<? extends Entity> entities, View view) {

    }

    @Override
    public void onLoad(Entity entity, View view) {

    }

    @Override
    public <T extends Entity> void setupAttributeAccess(T entity) {

    }

    @Override
    public boolean isAttributeAccessEnabled(MetaClass metaClass) {
        return false;
    }

    @Override
    public boolean isAttributeAccessEnabled() {
        return false;
    }
}
