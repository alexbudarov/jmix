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

package io.jmix.core.security;

import io.jmix.core.ServerConfig;
import io.jmix.core.compatibility.AppContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean that provides authentication to an arbitrary code on the Middleware.
 * <br>
 * Authentication is required if the code doesn't belong to a normal user request handling, which is the case for
 * invocation by schedulers or JMX tools, other than Web Client's JMX-console.
 * <br>
 * Example usage:
 * <pre>
 *     authentication.begin();
 *     try {
 *         // valid current thread's user session presents here
 *     } finally {
 *         authentication.end();
 *     }
 * </pre>
 */
@Component(Authenticator.NAME)
public class Authenticator {

    public static final String NAME = "jmix_Authenticator";

    private static final Logger log = LoggerFactory.getLogger(Authenticator.class);

    @Inject
    protected AuthenticationManager authenticationManager;

    @Inject
    protected UserSessions userSessions;

    @Inject
    protected ServerConfig serverConfig;

    protected ThreadLocal<Integer> cleanupCounter = new ThreadLocal<>();

    protected Map<String, UUID> sessions = new ConcurrentHashMap<>();

    // todo rework: always authenticate as the given user (impersonate)
    /**
     * Begin an authenticated code block.
     * <br>
     * If a valid current thread session exists, does nothing.
     * Otherwise sets the current thread session, logging in if necessary.
     * <br>
     * Subsequent {@link #end()} method must be called in "finally" section.
     *
     * @param login user login. If null, a value of {@code cuba.jmxUserLogin} app property is used.
     * @return new or cached instance of system user session
     */
    public UserSession begin(@Nullable String login) {
        if (cleanupCounter.get() == null) {
            cleanupCounter.set(0);
        }

        // check if a current thread session exists, that is we got here from authenticated code
        SecurityContext securityContext = AppContext.getSecurityContext();
        if (securityContext != null) {
            UserSession userSession = userSessions.getAndRefresh(securityContext.getSessionId());
            if (userSession != null) {
                log.trace("Already authenticated, do nothing");
                cleanupCounter.set(cleanupCounter.get() + 1);
                if (log.isTraceEnabled()) {
                    log.trace("New cleanup counter value: {}", cleanupCounter.get());
                }
                return userSession;
            }
        }

        // no current thread session or it is expired - need to authenticate
        if (StringUtils.isBlank(login)) {
            login = getSystemLogin();
        }

        UserSession session = null;
        log.trace("Authenticating as {}", login);
        UUID sessionId = sessions.get(login);
        if (sessionId != null) {
            session = userSessions.getAndRefresh(sessionId);
        }
        if (session == null) {
            // saved session doesn't exist or is expired
            synchronized (this) {
                // double check to prevent the same log in by subsequent threads
                sessionId = sessions.get(login);
                if (sessionId != null) {
                    session = userSessions.get(sessionId);
                }
                if (session == null) {
                    try {
                        Authentication authToken = new SystemAuthenticationToken(login);
                        Authentication authentication = authenticationManager.authenticate(authToken);
                        session = new UserSession(authentication);
                        session.setClientInfo("System authentication");
                    } catch (AuthenticationException e) {
                        throw new RuntimeException("Unable to perform system login", e);
                    }
                    sessions.put(login, session.getId());
                }
            }
        }

        AppContext.setSecurityContext(new SecurityContext(session));

        return session;
    }

    /**
     * Authenticate with login set in {@code cuba.jmxUserLogin} app property.
     * <br>
     * Same as {@link #begin(String)} with null parameter
     */
    public UserSession begin() {
        return begin(null);
    }

    /**
     * End of an authenticated code block.
     * <br>
     * Performs cleanup for SecurityContext if there was previous loginOnce in this thread.
     * Must be called in "finally" section of a try/finally block.
     */
    public void end() {
        if (cleanupCounter.get() == null || cleanupCounter.get() < 0) {
            log.warn("Cleanup counter is null or invalid");
        } else if (cleanupCounter.get() == 0) {
            log.trace("Cleanup SecurityContext");
            AppContext.setSecurityContext(null);
            cleanupCounter.remove();
        } else {
            log.trace("Do not own authentication, cleanup not required");
            cleanupCounter.set(cleanupCounter.get() - 1);
            log.trace("New cleanup counter value: {}", cleanupCounter.get());
        }
    }

    /**
     * Execute code on behalf of the specified user.
     *
     * @param login     user login. If null, a value of {@code cuba.jmxUserLogin} app property is used.
     * @param operation code to execute
     * @return result of the execution
     */
    public <T> T withUser(@Nullable String login, AuthenticatedOperation<T> operation) {
        SecurityContext previousSecurityContext = AppContext.getSecurityContext();
        AppContext.setSecurityContext(null);
        try {
            begin(login);
            return operation.call();
        } finally {
            AppContext.setSecurityContext(previousSecurityContext);
        }
    }

    /**
     * Execute code on behalf of the user with login set in {@code cuba.jmxUserLogin} app property.
     *
     * @param operation code to execute
     * @return result of the execution
     */
    public <T> T withSystemUser(AuthenticatedOperation<T> operation) {
        SecurityContext previousSecurityContext = AppContext.getSecurityContext();
        AppContext.setSecurityContext(null);
        try {
            begin(null);
            return operation.call();
        } finally {
            AppContext.setSecurityContext(previousSecurityContext);
        }
    }

    protected String getSystemLogin() {
        return serverConfig.getSystemUserLogin();
    }

    public interface AuthenticatedOperation<T> {
        T call();
    }
}