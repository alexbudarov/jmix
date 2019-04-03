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

package io.jmix.core.security

import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.compatibility.AppContext
import io.jmix.core.security.Authenticator
import io.jmix.core.security.UserSession
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Inject

@ContextConfiguration(classes = [JmixCoreConfiguration])
class AuthenticatorTest extends Specification {

    @Inject
    Authenticator authenticator

    def "authenticate as system"() {
        when:

        authenticator.begin()

        then:

        UserSession session = AppContext.getSecurityContextNN().getSession()
        session != null
        session.getUser().loginLowerCase == 'system'

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
        authentication != null

        when:

        authenticator.end()

        then:

        AppContext.getSecurityContext() == null
        SecurityContextHolder.getContext().getAuthentication() == null
    }

    def "authenticate as admin"() {
        when:

        authenticator.begin('admin')

        then:

        UserSession session = AppContext.getSecurityContextNN().getSession()
        session != null
        session.getUser().loginLowerCase == 'admin'

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
        authentication != null

        when:

        authenticator.end()

        then:

        AppContext.getSecurityContext() == null
        SecurityContextHolder.getContext().getAuthentication() == null
    }
}
