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

package io.jmix.ui.components.validation;

import com.haulmont.cuba.core.global.BeanLocator;
import com.haulmont.cuba.core.global.DateTimeTransformations;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.ValidationException;
import com.haulmont.cuba.gui.components.validation.time.TimeValidator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.*;
import java.util.Date;

/**
 * Validates that date or time in the past or present.
 * <p>
 * Note, types that support TimeZones can be found in {@link DateTimeTransformations#isDateTypeSupportsTimeZones(Class)}
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 *     &lt;bean id="cuba_PastOrPresentValidator" class="com.haulmont.cuba.gui.components.validation.PastOrPresentValidator" scope="prototype"/&gt;
 *     </pre>
 * Use {@link BeanLocator} when creating the validator programmatically.
 *
 * @param <T> {@link Date}, {@link LocalDate}, {@link LocalDateTime}, {@link LocalTime}, {@link OffsetDateTime},
 *            {@link OffsetTime}
 */
@Component(PastOrPresentValidator.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PastOrPresentValidator<T> extends AbstractValidator<T> {

    public static final String NAME = "cuba_PastOrPresentValidator";

    protected boolean checkSeconds = false;

    public PastOrPresentValidator() {
    }

    /**
     * Constructor for custom error message.
     *
     * @param message error message
     */
    public PastOrPresentValidator(String message) {
        this.message = message;
    }

    @Inject
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    /**
     * Set true if validator should also check seconds and nanos (if supported) in value. Default value is false.
     *
     * @param checkSeconds check seconds
     */
    public void setCheckSeconds(boolean checkSeconds) {
        this.checkSeconds = checkSeconds;
    }

    /**
     * @return true if seconds and nanos are checked
     */
    public boolean isCheckSeconds() {
        return checkSeconds;
    }

    @Override
    public void accept(T value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }

        TimeValidator timeConstraint = ValidatorHelper.getTimeConstraint(value);
        if (timeConstraint == null) {
            throw new IllegalArgumentException("PastOrPresentValidator doesn't support following type: '" + value.getClass() + "'");
        }

        timeConstraint.setCheckSeconds(checkSeconds);
        if (!timeConstraint.isPastOrPresent()) {
            String message = getMessage();
            if (message == null) {
                message = messages.getMainMessage("validation.constraints.pastOrPresent");
            }

            throw new ValidationException(message);
        }
    }
}