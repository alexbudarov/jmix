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

package io.jmix.ui.components.calendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalendarEventWrapper implements com.vaadin.v7.ui.components.calendar.event.CalendarEvent,
        com.vaadin.v7.ui.components.calendar.event.CalendarEvent.EventChangeNotifier {

    protected CalendarEvent calendarEvent;
    protected List<EventChangeListener> eventChangeListeners;

    public CalendarEventWrapper(CalendarEvent calendarEvent) {
        this.calendarEvent = calendarEvent;

        calendarEvent.addEventChangeListener(eventChangeEvent -> fireItemChanged());
    }

    protected void fireItemChanged() {
        if (eventChangeListeners != null) {
            EventChangeEvent event = new EventChangeEvent(this);

            for (EventChangeListener listener : eventChangeListeners) {
                listener.eventChange(event);
            }
        }
    }

    @Override
    public Date getStart() {
        return calendarEvent.getStart();
    }

    @Override
    public Date getEnd() {
        return calendarEvent.getEnd();
    }

    @Override
    public String getCaption() {
        return calendarEvent.getCaption();
    }

    @Override
    public String getDescription() {
        return calendarEvent.getDescription();
    }

    @Override
    public String getStyleName() {
        return calendarEvent.getStyleName();
    }

    @Override
    public boolean isAllDay() {
        return calendarEvent.isAllDay();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CalendarEventWrapper that = (CalendarEventWrapper) o;

        return calendarEvent.equals(that.calendarEvent);
    }

    @Override
    public int hashCode() {
        return calendarEvent.hashCode();
    }

    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    @Override
    public void addEventChangeListener(EventChangeListener listener) {
        if (eventChangeListeners == null) {
            eventChangeListeners = new ArrayList<>();
        }

        if (!eventChangeListeners.contains(listener)) {
            eventChangeListeners.add(listener);
        }
    }

    @Override
    public void removeEventChangeListener(EventChangeListener listener) {
        if (eventChangeListeners != null) {
            eventChangeListeners.remove(listener);
        }
    }
}
