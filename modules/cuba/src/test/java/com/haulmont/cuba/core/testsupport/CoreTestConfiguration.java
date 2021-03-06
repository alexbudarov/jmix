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

package com.haulmont.cuba.core.testsupport;

import com.haulmont.cuba.JmixCubaConfiguration;
import com.haulmont.cuba.core.model.common.UserEntityListener;
import io.jmix.core.JmixCoreConfiguration;
import io.jmix.core.security.UserSessionSource;
import io.jmix.data.JmixDataConfiguration;
import io.jmix.data.persistence.JpqlSortExpressionProvider;
import io.jmix.ui.JmixUiConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
@Import({JmixCoreConfiguration.class, JmixCubaConfiguration.class, JmixDataConfiguration.class, JmixUiConfiguration.class})
@PropertySource("classpath:/com/haulmont/cuba/core/test-app.properties")
public class CoreTestConfiguration {
    @Bean
    protected DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean(name = "test_UserEntityListener")
    UserEntityListener userEntityListener() {
        return new UserEntityListener();
    }

    @Bean(name = "cuba_UserSessionSource")
    UserSessionSource userSessionSource() {
        return new TestUserSessionSource();
    }

    @Bean(name = "cuba_JpqlSortExpressionProvider")
    JpqlSortExpressionProvider jpqlSortExpressionProvider() {
        return new TestJpqlSortExpressionProvider();
    }

    @Bean
    TestEventsListener testEventsListener() {
        return new TestEventsListener();
    }
}
