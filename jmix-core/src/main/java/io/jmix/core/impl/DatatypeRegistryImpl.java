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

package io.jmix.core.impl;

import io.jmix.core.Events;
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.DatatypeRegistry;
import io.jmix.core.metamodel.datatypes.impl.DatatypeDefUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component(DatatypeRegistry.NAME)
public class DatatypeRegistryImpl implements DatatypeRegistry {

    private static final Logger log = LoggerFactory.getLogger(DatatypeRegistryImpl.class);

    protected Map<Class<?>, Datatype> datatypeByClass = new HashMap<>();
    protected Map<String, Datatype> datatypeById = new HashMap<>();

    @Override
    public Datatype get(String id) {
        Datatype datatype = datatypeById.get(id);
        if (datatype == null)
            throw new IllegalArgumentException("Datatype '" + id + "' is not found");
        return datatype;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> Datatype<T> get(Class<T> javaClass) {
        Datatype<T> datatype = datatypeByClass.get(javaClass);
        if (datatype == null) {
            // if no exact type found, try to find matching super-type
            for (Map.Entry<Class<?>, Datatype> entry : datatypeByClass.entrySet()) {
                if (entry.getKey().isAssignableFrom(javaClass)) {
                    datatype = entry.getValue();
                    break;
                }
            }
        }
        return datatype;
    }

    /**
     * Get Datatype instance by the corresponding Java class. This method tries to find matching supertype too.
     * @return Datatype instance
     * @throws IllegalArgumentException if no datatype suitable for the given type found
     */
    @Override
    public <T> Datatype<T> getNN(Class<T> javaClass) {
        Datatype<T> datatype = get(javaClass);
        if (datatype == null)
            throw new IllegalArgumentException("A datatype for " + javaClass + " is not found");
        return datatype;
    }

    @Override
    public String getId(Datatype datatype) {
        for (Map.Entry<String, Datatype> entry : datatypeById.entrySet()) {
            if (entry.getValue().equals(datatype))
                return entry.getKey();
        }
        throw new IllegalArgumentException("Datatype not registered: " + datatype);
    }

    @Override
    public String getIdByJavaClass(Class<?> javaClass) {
        for (Map.Entry<String, Datatype> entry : datatypeById.entrySet()) {
            if (entry.getValue().getJavaClass().equals(javaClass))
                return entry.getKey();
        }
        throw new IllegalArgumentException("No datatype registered for " + javaClass);
    }

    @Override
    public Set<String> getIds() {
        return Collections.unmodifiableSet(datatypeById.keySet());
    }

    @Override
    public void register(Datatype datatype, String id, boolean defaultForJavaClass) {
        Preconditions.checkNotNullArgument(datatype, "datatype is null");
        Preconditions.checkNotNullArgument(id, "id is null");
        log.trace("Register datatype: {}, id: {}, defaultForJavaClass: {}", datatype.getClass(), id, defaultForJavaClass);

        if (defaultForJavaClass) {
            datatypeByClass.put(datatype.getJavaClass(), datatype);
        }
        datatypeById.put(id, datatype);
    }

    @EventListener
    @Order(Events.HIGHEST_CORE_PRECEDENCE + 20)
    private void onContextRefresh(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        Map<String, Datatype> beansMap = context.getBeansOfType(Datatype.class);
        for (Datatype datatype : beansMap.values()) {
            register(datatype, datatype.getId(), DatatypeDefUtils.isDefaultForClass(datatype));
        }
    }
}
