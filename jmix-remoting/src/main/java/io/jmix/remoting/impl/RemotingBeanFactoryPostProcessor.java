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

import com.google.common.base.Strings;
import io.jmix.remoting.annotation.Remote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("jmix_RemotingBeanFactoryPostProcessor")
public class RemotingBeanFactoryPostProcessor implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final Logger log = LoggerFactory.getLogger(RemotingBeanFactoryPostProcessor.class);

    protected boolean enabled;

    protected String thisRole;

    private static final String SERVER_URL = "http://localhost:8080"; // todo make configurable

    @Override
    public void setEnvironment(Environment environment) {
        enabled = Boolean.parseBoolean(environment.getProperty("jmix.remoting.enabled"));
        thisRole = environment.getProperty("jmix.remoting.role");
        if (enabled && Strings.isNullOrEmpty(thisRole)) {
            throw new IllegalStateException("Property " + "jmix.remoting.role" + " is not set");
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        processRemoteBeans(registry);
    }

    private void processRemoteBeans(BeanDefinitionRegistry registry) {
        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (beanDefinition instanceof AnnotatedBeanDefinition) {
                AnnotationMetadata metadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();
                if (metadata.hasAnnotation(Remote.class.getName())) {
                    String[] interfaceNames = metadata.getInterfaceNames();
                    if (interfaceNames.length != 1) {
                        throw new UnsupportedOperationException("@Remote bean " + metadata.getClassName() + " must implement exactly one interface");
                    }
                    Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(Remote.class.getName());
                    if (annotationAttributes != null) {
                        String role = (String) annotationAttributes.get("role");
                        if (role.equals(thisRole)) {
                            createServerEndpoint(registry, beanName, interfaceNames[0]);
                        } else {
                            createClientProxy(registry, beanName, interfaceNames[0]);
                        }
                    } else {
                        throw new IllegalStateException("annotationAttributes map is null in " + metadata.getClassName());
                    }
                }
            }
        }
    }

    private void createServerEndpoint(BeanDefinitionRegistry registry, String beanName, String interfaceName) {
        BeanDefinition endpointBeanDefinition = new RootBeanDefinition(ServerEndpointExporter.class);
        MutablePropertyValues propertyValues = endpointBeanDefinition.getPropertyValues();
        propertyValues.add("serviceBeanName", beanName);
        propertyValues.add("serviceInterface", interfaceName);
        registry.registerBeanDefinition("/remoting/" + beanName, endpointBeanDefinition);
        log.debug("Configured bean " + beanName + " for export via HTTP");
    }

    private void createClientProxy(BeanDefinitionRegistry registry, String beanName, String interfaceName) {
        BeanDefinition definition = new RootBeanDefinition(ClientProxyFactoryBean.class);
//        definition.getConstructorArgumentValues().addIndexedArgumentValue(0, serverSelector);
        MutablePropertyValues propertyValues = definition.getPropertyValues();
        String servicePath = "/remoting/" + beanName;
        propertyValues.add("serviceUrl", SERVER_URL + servicePath);
        propertyValues.add("serviceInterface", interfaceName);
        registry.registerBeanDefinition(beanName, definition);

        log.debug("Configured remote proxy bean " + beanName + " of type " + interfaceName + ", bound to " + servicePath);

    }
}
