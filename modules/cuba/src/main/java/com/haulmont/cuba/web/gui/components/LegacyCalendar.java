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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.impl.CollectionDsHelper;
import io.jmix.ui.components.calendar.CalendarEventProvider;
import com.haulmont.cuba.web.gui.components.calendar.EntityCalendarEventProvider;
import io.jmix.ui.components.impl.WebCalendar;

import javax.annotation.Nullable;

public class LegacyCalendar extends WebCalendar {

    /**
     * Set collection datasource for the calendar component with a collection of events.
     *
     * @param datasource a datasource to set
     * @deprecated @deprecated Use {@link #setEventProvider(CalendarEventProvider)}
     * with {@link EntityCalendarEventProvider} instead
     */
    @Deprecated
    void setDatasource(CollectionDatasource datasource) {
        if (datasource == null) {
            setEventProvider(null);
        } else {
            CollectionDsHelper.autoRefreshInvalid(datasource, true);
            setEventProvider(new EntityCalendarEventProvider(datasource));
        }
    }

    /**
     * @return a datasource
     * @deprecated Use {@link #getEventProvider()} instead
     */

    @Nullable
    @Deprecated
    CollectionDatasource getDatasource() {
        return (calendarEventProvider instanceof EntityCalendarEventProvider)
                ? ((EntityCalendarEventProvider) calendarEventProvider)
                .getDatasource()
                : null;
    }
}
