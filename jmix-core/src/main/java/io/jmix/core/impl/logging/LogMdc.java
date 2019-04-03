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

package io.jmix.core.impl.logging;

import io.jmix.core.compatibility.AppContext;
import io.jmix.core.security.SecurityContext;
import io.jmix.core.security.UserSession;
import io.jmix.core.security.UserSessions;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;

public class LogMdc {

    public static final String USER = "cubaUser";
    public static final String APPLICATION = "cubaApp";

    public static void setup(SecurityContext securityContext) {
        String userProp = AppContext.getProperty("cuba.logUserName");
        if (userProp == null || Boolean.valueOf(userProp)) {
            if (securityContext != null) {
                String user = securityContext.getUser();
                if (user == null) {
                    UserSession session = securityContext.getSession();
                    if (session != null)
                        user = session.getUser().getLogin();
                    else if (securityContext.getSessionId() != null) {
                        ApplicationContext applicationContext = AppContext.getApplicationContext();
                        if (applicationContext.containsBean("cuba_UserSessions")) {
                            UserSessions sessionFinder = (UserSessions) applicationContext.getBean("cuba_UserSessions");
                            session = sessionFinder.get(securityContext.getSessionId());
                            if (session != null) {
                                user = session.getUser().getLogin();
                            }
                        }
                    }
                }
                if (user != null) {
                    MDC.put(USER, "/" + user);
                }
            } else {
                MDC.remove(USER);
            }
        }

        String applicationProp = AppContext.getProperty("cuba.logAppName");
        if (applicationProp == null || Boolean.valueOf(applicationProp)) {
            if (securityContext != null) {
                MDC.put(APPLICATION, "/" + AppContext.getProperty("cuba.webContextName"));
            } else {
                MDC.remove(APPLICATION);
            }
        }
    }
}