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

import io.jmix.core.*;
import io.jmix.core.security.UserSessionSource;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import static io.jmix.core.commons.util.Preconditions.checkNotNullArgument;

@Component(BeanValidation.NAME)
public class BeanValidationImpl implements BeanValidation {

    public static final ValidationOptions NO_VALIDATION_OPTIONS = new ValidationOptions();

    @Inject
    protected Messages messages;
    @Inject
    protected Metadata metadata;
    @Inject
    protected TimeSource timeSource;
    @Inject
    protected UserSessionSource userSessionSource;
    @Inject
    protected EntityStates entityStates;

    protected ConcurrentHashMap<Locale, ValidatorFactory> validatorFactoriesCache = new ConcurrentHashMap<>();

    @Override
    public Validator getValidator() {
        Locale locale = getCurrentLocale();

        return getValidatorWithDefaultFactory(locale);
    }

    @Override
    public Validator getValidator(ConstraintMapping constraintMapping) {
        checkNotNullArgument(constraintMapping);

        return getValidator(constraintMapping, NO_VALIDATION_OPTIONS);
    }

    @Override
    public Validator getValidator(@Nullable ConstraintMapping constraintMapping, ValidationOptions options) {
        checkNotNullArgument(options);

        if (constraintMapping == null
                && options.getFailFast() == null
                && options.getLocale() != null) {
            return getValidatorWithDefaultFactory(options.getLocale());
        }

        Locale locale;
        if (options.getLocale() != null) {
            locale = options.getLocale();
        } else {
            locale = getCurrentLocale();
        }

        HibernateValidatorConfiguration configuration = getValidatorFactoryConfiguration(locale);
        if (options.getFailFast() != null) {
            configuration.failFast(options.getFailFast());
        }
        if (constraintMapping != null) {
            configuration.addMapping(constraintMapping);
        }

        ValidatorFactory factory = configuration.buildValidatorFactory();
        return factory.getValidator();
    }

    protected Validator getValidatorWithDefaultFactory(Locale locale) {
        ValidatorFactory validatorFactoryFromCache = validatorFactoriesCache.get(locale);
        if (validatorFactoryFromCache != null) {
            return validatorFactoryFromCache.getValidator();
        }

        HibernateValidatorConfiguration configuration = getValidatorFactoryConfiguration(locale);
        ValidatorFactory factory = configuration.buildValidatorFactory();

        validatorFactoriesCache.put(locale, factory);

        return factory.getValidator();
    }

    protected HibernateValidatorConfiguration getValidatorFactoryConfiguration(Locale locale) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        HibernateValidatorConfiguration configuration = Validation.byProvider(HibernateValidator.class)
                .configure()

                // todo custom options
                /*.clockProvider(new CubaValidationTimeProvider(timeSource))
                .traversableResolver(new CubaValidationTraversableResolver(metadata, entityStates))
                .messageInterpolator(new CubaValidationMessagesInterpolator(messages, locale))*/;

        return configuration;
    }

    protected Locale getCurrentLocale() {
        Locale locale;
        if (userSessionSource.checkCurrentUserSession()) {
            locale = userSessionSource.getLocale();
        } else {
            locale = Locale.ENGLISH; //  messages.getTools().getDefaultLocale(); todo default locale
        }
        return locale;
    }
}