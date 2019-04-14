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

package io.jmix.remoting.impl;

import io.jmix.remoting.annotation.ConditionalOnRemotingRole;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

public class OnRemotingRoleCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean enabled = Boolean.parseBoolean(context.getEnvironment().getProperty("jmix.remoting.enabled"));
        if (!enabled) {
            return false;
        }

        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnRemotingRole.class.getName());
        if (attributes != null) {
            String value = (String) attributes.get("value");
            String role = context.getEnvironment().getProperty("jmix.remoting.role");
            return value.equals(role);
        }
        throw new IllegalStateException("Cannot get @ConditionalOnRemotingRole attributes from " + metadata);
    }
}
