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

package io.jmix.ui.xml.layout.loaders;

import io.jmix.core.Metadata;
import io.jmix.ui.Actions;
import io.jmix.ui.actions.Action;
import io.jmix.ui.actions.picker.LookupAction;
import io.jmix.ui.actions.picker.OpenAction;
import io.jmix.ui.components.ActionsHolder;
import io.jmix.ui.components.CaptionMode;
import io.jmix.ui.components.SuggestionPickerField;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class SuggestionPickerFieldLoader extends SuggestionFieldQueryLoader<SuggestionPickerField> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(SuggestionPickerField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadTabIndex(resultComponent, element);

        loadMetaClass(resultComponent, element);

        loadCaptionProperty(resultComponent, element);

        loadActions(resultComponent);
        loadValidators(resultComponent, element);

        loadAsyncSearchDelayMs(resultComponent, element);
        loadMinSearchStringLength(resultComponent, element);
        loadSuggestionsLimit(resultComponent, element);

        loadInputPrompt(resultComponent, element);

        loadPopupWidth(resultComponent, element);

        loadQuery(resultComponent, element);
    }

    protected Metadata getMetadata() {
        return beanLocator.get(Metadata.NAME);
    }

    protected void loadPopupWidth(SuggestionPickerField suggestionField, Element element) {
        String popupWidth = element.attributeValue("popupWidth");
        if (StringUtils.isNotEmpty(popupWidth)) {
            suggestionField.setPopupWidth(popupWidth);
        }
    }

    protected void loadCaptionProperty(SuggestionPickerField suggestionField, Element element) {
        String captionProperty = element.attributeValue("captionProperty");
        if (!StringUtils.isEmpty(captionProperty)) {
            suggestionField.setCaptionMode(CaptionMode.PROPERTY);
            suggestionField.setCaptionProperty(captionProperty);
        }
    }

    protected void loadActions(SuggestionPickerField suggestionField) {
        loadActions(suggestionField, element);
        if (suggestionField.getActions().isEmpty()) {

            if (isLegacyFrame()) {
                suggestionField.addLookupAction();
                suggestionField.addOpenAction();
            } else {
                Actions actions = getActions();

                suggestionField.addAction(actions.create(LookupAction.ID));
                suggestionField.addAction(actions.create(OpenAction.ID));
            }
        }
    }

    protected Actions getActions() {
        return beanLocator.get(Actions.NAME);
    }

    protected void loadMetaClass(SuggestionPickerField suggestionField, Element element) {
        String metaClass = element.attributeValue("metaClass");
        if (!StringUtils.isEmpty(metaClass)) {
            suggestionField.setMetaClass(getMetadata().getClass(metaClass));
        }
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        return loadPickerDeclarativeAction(actionsHolder, element);
    }

    protected void loadSuggestionsLimit(SuggestionPickerField suggestionField, Element element) {
        String suggestionsLimit = element.attributeValue("suggestionsLimit");
        if (StringUtils.isNotEmpty(suggestionsLimit)) {
            suggestionField.setSuggestionsLimit(Integer.parseInt(suggestionsLimit));
        }
    }

    protected void loadMinSearchStringLength(SuggestionPickerField suggestionField, Element element) {
        String minSearchStringLength = element.attributeValue("minSearchStringLength");
        if (StringUtils.isNotEmpty(minSearchStringLength)) {
            suggestionField.setMinSearchStringLength(Integer.parseInt(minSearchStringLength));
        }
    }

    protected void loadAsyncSearchDelayMs(SuggestionPickerField suggestionField, Element element) {
        String asyncSearchDelayMs = element.attributeValue("asyncSearchDelayMs");
        if (StringUtils.isNotEmpty(asyncSearchDelayMs)) {
            suggestionField.setAsyncSearchDelayMs(Integer.parseInt(asyncSearchDelayMs));
        }
    }
}
