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
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.Deque;
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
 *     authenticator.begin();
 *     try {
 *         // valid current thread's user session presents here
 *     } finally {
 *         authenticator.end();
 *     }
 * </pre>
 */
@Component(Authenticator.NAME)
public class Authenticator {

    public static final String NAME = "jmix_Authenticator";

    private static final Logger log = LoggerFactory.getLogger(Authenticator.class);

    private static final SecurityContext NULL_CONTEXT = new SecurityContext(new UUID(0L, 0L));

    @Inject
    protected AuthenticationManager authenticationManager;

    @Inject
    protected UserSessionFactory userSessionFactory;

    @Inject
    protected ServerConfig serverConfig;

    protected ThreadLocal<Deque<SecurityContext>> threadLocalStack = new ThreadLocal<>();

    protected Map<String, UserSession> sessions = new ConcurrentHashMap<>();

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
        if (StringUtils.isBlank(login)) {
            login = getSystemLogin();
        }

        UserSession session;
        log.trace("Authenticating as {}", login);
        session = sessions.get(login);
        if (session == null) {
            // saved session doesn't exist
            synchronized (this) {
                // double check to prevent the same log in by subsequent threads
                session = sessions.get(login);
                if (session == null) {
                    try {
                        Authentication authToken = new SystemAuthenticationToken(login);
                        Authentication authentication = authenticationManager.authenticate(authToken);
                        session = userSessionFactory.create(authentication);
                        session.setClientDetails(ClientDetails.builder().info("System authentication").build());
                    } catch (LoginException e) {
                        throw new RuntimeException("Unable to perform system login", e);
                    }
                    sessions.put(login, session);
                }
            }
        }

        pushSecurityContext(AppContext.getSecurityContext());

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
        log.trace("Set previous SecurityContext");
        SecurityContext previous = popSecurityContext();
        AppContext.setSecurityContext(previous);
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

    private void pushSecurityContext(SecurityContext securityContext) {
        Deque<SecurityContext> stack = threadLocalStack.get();
        if (stack == null) {
            stack = new ArrayDeque<>();
            threadLocalStack.set(stack);
        } else {
            if (stack.size() > 10) {
                log.warn("Stack is too big: {}. Check correctness of begin/end invocations.", stack.size());
            }
        }
        if (securityContext == null) {
            securityContext = NULL_CONTEXT;
        }
        stack.push(securityContext);
    }

    private SecurityContext popSecurityContext() {
        Deque<SecurityContext> stack = threadLocalStack.get();
        if (stack != null) {
            SecurityContext securityContext = stack.poll();
            if (securityContext != null) {
                if (securityContext == NULL_CONTEXT) {
                    return null;
                } else {
                    return securityContext;
                }
            } else {
                log.warn("Stack is empty. Check correctness of begin/end invocations.");
            }
        } else {
            log.warn("Stack does not exist. Check correctness of begin/end invocations.");
        }
        return null;
    }

    public interface AuthenticatedOperation<T> {
        T call();
    }
}