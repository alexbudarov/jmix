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

import io.jmix.core.commons.events.EventHub;
import io.jmix.core.commons.events.Subscription;
import io.jmix.core.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class EntityCalendarEventProvider implements CalendarEventProvider,
        io.jmix.ui.components.data.calendar.EntityCalendarEventProvider  {

    protected List<CalendarEvent> itemsCache;
    // protected CollectionDatasource datasource; TODO: legacy-ui

    protected String startDateProperty;
    protected String endDateProperty;
    protected String captionProperty;
    protected String descriptionProperty;
    protected String styleNameProperty;
    protected String allDayProperty;

    protected EventHub events = new EventHub();

    /*
    TODO: legacy-ui
    protected CollectionDatasource.CollectionChangeListener collectionChangeListener;
    protected Datasource.ItemPropertyChangeListener itemPropertyChangeListener;*/

    /*
    TODO: legacy-ui
    @SuppressWarnings("unchecked")
    public EntityCalendarEventProvider (CollectionDatasource datasource) {
        this.datasource = datasource;

        collectionChangeListener = createCollectionChangeListener();
        itemPropertyChangeListener = createItemPropertyChangeListener();

        datasource.addCollectionChangeListener(collectionChangeListener);
        datasource.addItemPropertyChangeListener(itemPropertyChangeListener);
    }

    protected CollectionDatasource.CollectionChangeListener createCollectionChangeListener() {
        return e -> {
            itemsCache = null;
            events.publish(EventSetChangeEvent.class, new EventSetChangeEvent(this));
        };
    }

    protected Datasource.ItemPropertyChangeListener createItemPropertyChangeListener() {
        return e -> {
            if (e.getProperty() != null) {
                if (e.getProperty().equals(startDateProperty)
                        || e.getProperty().equals(endDateProperty)
                        || e.getProperty().equals(captionProperty)
                        || e.getProperty().equals(descriptionProperty)
                        || e.getProperty().equals(styleNameProperty)
                        || e.getProperty().equals(allDayProperty)) {
                    itemsCache = null;
                    events.publish(EventSetChangeEvent.class, new EventSetChangeEvent(this));
                }
            }
        };
    }

    public CollectionDatasource getDatasource() {
        return datasource;
    }*/

    @Override
    public void addEvent(CalendarEvent event) {
        throw new UnsupportedOperationException("Use datasource for changing data items of EntityCalendarEventProvider");
    }

    @Override
    public void removeEvent(CalendarEvent event) {
        throw new UnsupportedOperationException("Use datasource for changing data items of EntityCalendarEventProvider");
    }

    @Override
    public void removeAllEvents() {
        throw new UnsupportedOperationException("Use datasource for changing data items of EntityCalendarEventProvider");
    }

    @Override
    public Subscription addEventSetChangeListener(Consumer<EventSetChangeEvent> listener) {
        return events.subscribe(EventSetChangeEvent.class, listener);
    }

    @Override
    public void removeEventSetChangeListener(Consumer<EventSetChangeEvent> listener) {
        events.unsubscribe(EventSetChangeEvent.class, listener);
    }

    @Override
    public List<CalendarEvent> getEvents() {
        return Collections.emptyList();
        /*
        TODO: legacy-ui
        if (startDateProperty == null || endDateProperty == null || captionProperty == null) {
            return new ArrayList<>();
        }

        if (itemsCache == null) {
            itemsCache = new ArrayList<>();
            for (Entity entity : (Collection<Entity>) datasource.getItems()) {
                itemsCache.add(new EntityCalendarEvent<>(entity, this));
            }
            return itemsCache;
        } else {
            return itemsCache;
        }*/
    }

    @Override
    public void setStartDateProperty(String startDateProperty) {
        this.startDateProperty = startDateProperty;
    }

    @Override
    public String getStartDateProperty() {
        return startDateProperty;
    }

    @Override
    public void setEndDateProperty(String endDateProperty) {
        this.endDateProperty = endDateProperty;
    }

    @Override
    public String getEndDateProperty() {
        return endDateProperty;
    }

    @Override
    public void setCaptionProperty(String captionProperty) {
        this.captionProperty = captionProperty;
    }

    @Override
    public String getCaptionProperty() {
        return captionProperty;
    }

    @Override
    public void setDescriptionProperty(String descriptionProperty) {
        this.descriptionProperty = descriptionProperty;
    }

    @Override
    public String getDescriptionProperty() {
        return descriptionProperty;
    }

    @Override
    public void setStyleNameProperty(String styleNameProperty) {
        this.styleNameProperty = styleNameProperty;
    }

    @Override
    public String getStyleNameProperty() {
        return styleNameProperty;
    }

    @Override
    public void setAllDayProperty(String allDayProperty) {
        this.allDayProperty = allDayProperty;
    }

    @Override
    public String getIsAllDayProperty() {
        return allDayProperty;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unbind() {
        /*
        TODO: legacy-ui
        datasource.removeCollectionChangeListener(collectionChangeListener);
        datasource.removeItemPropertyChangeListener(itemPropertyChangeListener);*/
    }
}
