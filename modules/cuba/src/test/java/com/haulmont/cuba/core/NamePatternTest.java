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
package com.haulmont.cuba.core;

import com.haulmont.cuba.core.model.common.Role;
import com.haulmont.cuba.core.model.common.Server;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.data.Persistence;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CoreTest
public class NamePatternTest {
    @Inject
    private Metadata metadata;

    @Inject
    protected MetadataTools metadataTools;

    @Test
    public void test() {
        Server server = new Server();
        server.setName("orion");
        server.setRunning(false);

        assertEquals("orion", metadataTools.getInstanceName(server));
    }

    @Test
    public void roleNamePattern() {
        Role role = new Role();
        role.setLocName("System Role");
        role.setName("system_role");

        String instanceName = metadata.getTools().getInstanceName(role);
        assertEquals("System Role [system_role]", instanceName);
        assertEquals("System Role [system_role]", metadataTools.getInstanceName(role));
    }

    @Test
    public void userNamePattern() {
        User user = new User();
        user.setName("System Administrator");
        user.setLogin("systemAdmin");

        String instanceName = metadata.getTools().getInstanceName(user);
        assertEquals("System Administrator [systemAdmin]", instanceName);
        assertEquals("System Administrator [systemAdmin]", metadataTools.getInstanceName(user));
    }
}