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

package io.jmix.ui.components.factories;

import io.jmix.core.AppBeans;
import io.jmix.core.Messages;
import io.jmix.core.entity.FileDescriptor;
import io.jmix.core.entity.annotation.CurrencyValue;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.ui.UiComponents;
import io.jmix.ui.components.*;
import io.jmix.ui.components.data.Options;
import io.jmix.ui.components.data.ValueSource;
import io.jmix.ui.components.data.value.ContainerValueSource;
import io.jmix.ui.dynamicattributes.CategoryAttribute;
import io.jmix.ui.dynamicattributes.DynamicAttributesGuiTools;
import io.jmix.ui.dynamicattributes.DynamicAttributesTools;
import io.jmix.ui.dynamicattributes.DynamicAttributesUtils;
import io.jmix.ui.gui.OpenType;
import io.jmix.ui.screen.FrameOwner;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.sql.Time;
import java.time.*;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;

import static io.jmix.ui.components.DateField.Resolution;

public abstract class AbstractComponentGenerationStrategy implements ComponentGenerationStrategy {

    protected Messages messages;
    protected UiComponents uiComponents;
    protected DynamicAttributesTools dynamicAttributesTools;

    public AbstractComponentGenerationStrategy(Messages messages, DynamicAttributesTools dynamicAttributesTools) {
        this.messages = messages;
        this.dynamicAttributesTools = dynamicAttributesTools;
    }

    protected Component createComponentInternal(ComponentGenerationContext context) {
        MetaClass metaClass = context.getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, context.getProperty());

        if (mpp == null) {
            return null;
        }

        Range mppRange = mpp.getRange();
        Component resultComponent = null;

        if (mppRange.isDatatype()) {
            resultComponent = createDatatypeField(context, mpp);
        } else if (mppRange.isClass()) {
            resultComponent = createClassField(context, mpp);
        } else if (mppRange.isEnum()) {
            resultComponent = createEnumField(context);
        }

        if (resultComponent instanceof HasValue) {
            setValueChangedListeners((HasValue) resultComponent, context);
        }

        if (resultComponent instanceof Component.Editable) {
            setEditable((Component.Editable) resultComponent, context);
        }

        return resultComponent;
    }

    protected Component createClassField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        MetaProperty metaProperty = mpp.getMetaProperty();
        Class<?> javaType = metaProperty.getJavaType();

        if (FileDescriptor.class.isAssignableFrom(javaType)) {
            return createFileUploadField(context);
        }

        if (!Collection.class.isAssignableFrom(javaType)) {
            return createEntityField(context, mpp);
        }

        return null;
    }

    protected Component createDatatypeField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Range mppRange = mpp.getRange();
        Element xmlDescriptor = context.getXmlDescriptor();

        Class type = mppRange.asDatatype().getJavaClass();

        MetaProperty metaProperty = mpp.getMetaProperty();
        if (DynamicAttributesUtils.isDynamicAttribute(metaProperty)) {
            CategoryAttribute categoryAttribute = DynamicAttributesUtils.getCategoryAttribute(metaProperty);
            if (categoryAttribute != null) {
                // todo dynamic attributes
//                CategoryAttributeConfiguration configuration = categoryAttribute.getConfiguration();
//                if (categoryAttribute.getDataType() == PropertyType.ENUMERATION && BooleanUtils.isNotTrue(categoryAttribute.getIsCollection())) {
//                    return createEnumField(context);
//                }
//                if (Boolean.TRUE.equals(categoryAttribute.getLookup()) && configuration.hasOptionsLoader()) {
//                    return createLookupField(context, categoryAttribute);
//                }
            }
        }

        if (xmlDescriptor != null
                && "true".equalsIgnoreCase(xmlDescriptor.attributeValue("link"))) {
            return createDatatypeLinkField(context);
        }

        boolean hasMaskAttribute = xmlDescriptor != null
                && xmlDescriptor.attribute("mask") != null;

        if (type.equals(String.class)) {
            return hasMaskAttribute
                    ? createMaskedField(context)
                    : createStringField(context, mpp);
        } else if (type.equals(UUID.class)) {
            return createUuidField(context);
        } else if (type.equals(Boolean.class)) {
            return createBooleanField(context);
        } else if (type.equals(java.sql.Date.class)
                || type.equals(Date.class)
                || type.equals(LocalDate.class)
                || type.equals(LocalDateTime.class)
                || type.equals(OffsetDateTime.class)) {
            return createDateField(context);
        } else if (type.equals(Time.class)
                || type.equals(LocalTime.class)
                || type.equals(OffsetTime.class)) {
            return createTimeField(context);
        } else if (Number.class.isAssignableFrom(type)) {
            if (hasMaskAttribute) {
                return createMaskedField(context);
            }

            Field currencyField = createCurrencyField(context, mpp);
            if (currencyField != null) {
                return currencyField;
            }

            return createNumberField(context);
        }
        return null;
    }

    protected Component createDatatypeLinkField(ComponentGenerationContext context) {
        EntityLinkField linkField = uiComponents.create(EntityLinkField.class);

        setValidators(linkField, context);
        setValueSource(linkField, context);
        setLinkFieldAttributes(linkField, context);

        return linkField;
    }

    protected Field createEnumField(ComponentGenerationContext context) {
        LookupField component = uiComponents.create(LookupField.class);
        setValidators(component, context);
        setValueSource(component, context);
        return component;
    }

    protected Component createMaskedField(ComponentGenerationContext context) {
        MaskedField maskedField = uiComponents.create(MaskedField.class);
        setValidators(maskedField, context);
        setValueSource(maskedField, context);

        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            maskedField.setMask(xmlDescriptor.attributeValue("mask"));

            String valueModeStr = xmlDescriptor.attributeValue("valueMode");
            if (StringUtils.isNotEmpty(valueModeStr)) {
                maskedField.setValueMode(MaskedField.ValueMode.valueOf(valueModeStr.toUpperCase()));
            }
        }
        maskedField.setValueMode(MaskedField.ValueMode.MASKED);
        maskedField.setSendNullRepresentation(false);

        return maskedField;
    }

    protected Component createStringField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        TextInputField textField = null;

        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            final String rows = xmlDescriptor.attributeValue("rows");
            if (!StringUtils.isEmpty(rows)) {
                TextArea textArea = uiComponents.create(TextArea.class);
                textArea.setRows(Integer.parseInt(rows));
                textField = textArea;
            }
        }

        if (DynamicAttributesUtils.isDynamicAttribute(context.getProperty()) && mpp != null) {
            CategoryAttribute categoryAttribute = DynamicAttributesUtils.getCategoryAttribute(mpp.getMetaProperty());
            // todo dynamic attributes
//            if (categoryAttribute != null && categoryAttribute.getDataType() == PropertyType.STRING
//                    && categoryAttribute.getRowsCount() != null && categoryAttribute.getRowsCount() > 1) {
//                TextArea textArea = uiComponents.create(TextArea.class);
//                textArea.setRows(categoryAttribute.getRowsCount());
//                textField = textArea;
//            }
        }

        if (textField == null) {
            textField = uiComponents.create(TextField.class);
        }

        setValidators(textField, context);
        setValueSource(textField, context);

        String maxLength = xmlDescriptor != null ? xmlDescriptor.attributeValue("maxLength") : null;
        if (StringUtils.isNotEmpty(maxLength)) {
            ((TextInputField.MaxLengthLimited) textField).setMaxLength(Integer.parseInt(maxLength));
        }

        return textField;
    }

    protected Component createLookupField(ComponentGenerationContext context, CategoryAttribute categoryAttribute) {
        LookupField lookupField = uiComponents.create(LookupField.class);

        ValueSource valueSource = context.getValueSource();
        if (valueSource instanceof ContainerValueSource) {
            setOptionsLoader(categoryAttribute, lookupField, (ContainerValueSource) valueSource);
        }

        setValueSource(lookupField, context);

        setValidators(lookupField, context);

        return lookupField;
    }

    protected Field createUuidField(ComponentGenerationContext context) {
        MaskedField maskedField = uiComponents.create(MaskedField.class);
        setValidators(maskedField, context);
        setValueSource(maskedField, context);
        maskedField.setMask("hhhhhhhh-hhhh-hhhh-hhhh-hhhhhhhhhhhh");
        maskedField.setSendNullRepresentation(false);
        return maskedField;
    }

    protected Field createBooleanField(ComponentGenerationContext context) {
        CheckBox component = uiComponents.create(CheckBox.class);
        setValidators(component, context);
        setValueSource(component, context);
        return component;
    }

    protected Component createDateField(ComponentGenerationContext context) {
        DateField dateField = uiComponents.create(DateField.class);
        setValidators(dateField, context);
        setValueSource(dateField, context);

        Element xmlDescriptor = context.getXmlDescriptor();
        String resolution = xmlDescriptor == null ? null : xmlDescriptor.attributeValue("resolution");
        String dateFormat = xmlDescriptor == null ? null : xmlDescriptor.attributeValue("dateFormat");

        if (StringUtils.isNotEmpty(resolution)) {
            Resolution dateResolution = Resolution.valueOf(resolution);
            dateField.setResolution(dateResolution);

            if (dateFormat == null) {
                if (dateResolution == Resolution.DAY) {
                    dateFormat = "msg://dateFormat";
                } else if (dateResolution == Resolution.MIN) {
                    dateFormat = "msg://dateTimeFormat";
                }
            }
        }

        if (StringUtils.isNotEmpty(dateFormat)) {
            if (dateFormat.startsWith("msg://")) {
                dateFormat = messages.getMessage(dateFormat.substring(6));
            }
            dateField.setDateFormat(dateFormat);
        }

        return dateField;
    }

    protected Component createTimeField(ComponentGenerationContext context) {
        TimeField timeField = uiComponents.create(TimeField.class);
        setValidators(timeField, context);
        setValueSource(timeField, context);

        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            String showSeconds = xmlDescriptor.attributeValue("showSeconds");
            if (Boolean.parseBoolean(showSeconds)) {
                timeField.setResolution(TimeField.Resolution.SEC);
            }
        }

        return timeField;
    }

    protected Field createNumberField(ComponentGenerationContext context) {
        TextField component = uiComponents.create(TextField.class);
        setValidators(component, context);
        setCustomDataType(component, context);
        setValueSource(component, context);

        return component;
    }

    @Nullable
    protected Field createCurrencyField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        if (DynamicAttributesUtils.isDynamicAttribute(mpp.getMetaProperty()))
            return null;

        Object currencyAnnotation = mpp.getMetaProperty().getAnnotations().get(CurrencyValue.class.getName());
        if (currencyAnnotation == null) {
            return null;
        }

        CurrencyField component = uiComponents.create(CurrencyField.class);
        setValueSource(component, context);
        return component;
    }

    protected Field createFileUploadField(ComponentGenerationContext context) {
        FileUploadField fileUploadField = uiComponents.create(FileUploadField.NAME);
        fileUploadField.setMode(FileUploadField.FileStoragePutMode.IMMEDIATE);

        fileUploadField.setUploadButtonCaption(null);
        fileUploadField.setUploadButtonDescription(messages.getMessage("upload.submit"));
        fileUploadField.setUploadButtonIcon("icons/upload.png");

        fileUploadField.setClearButtonCaption(null);
        fileUploadField.setClearButtonDescription(messages.getMessage("upload.clear"));
        fileUploadField.setClearButtonIcon("icons/remove.png");

        fileUploadField.setShowFileName(true);
        fileUploadField.setShowClearButton(true);

        setValueSource(fileUploadField, context);

        return fileUploadField;
    }

    @SuppressWarnings("unchecked")
    protected Component createEntityField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        String linkAttribute = null;
        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            linkAttribute = xmlDescriptor.attributeValue("link");
        }

        if (!Boolean.parseBoolean(linkAttribute)) {
            Options options = context.getOptions();
            boolean useOptionsLoader = false;
            if (DynamicAttributesUtils.isDynamicAttribute(mpp.getMetaProperty())) {
                // todo dynamic attributes
//                DynamicAttributesMetaProperty metaProperty = (DynamicAttributesMetaProperty) mpp.getMetaProperty();
//                CategoryAttribute attribute = metaProperty.getAttribute();
//                CategoryAttributeConfiguration configuration = attribute.getConfiguration();
//                if (Boolean.TRUE.equals(attribute.getLookup()) && configuration.hasOptionsLoader()) {
//                    useOptionsLoader = true;
//                }
            }

            PickerField pickerField;
            if (options == null && !useOptionsLoader) {
                pickerField = uiComponents.create(PickerField.class);
                setValueSource(pickerField, context);

                if (mpp.getMetaProperty().getType() == MetaProperty.Type.ASSOCIATION) {
                    pickerField.addLookupAction();
                    if (DynamicAttributesUtils.isDynamicAttribute(mpp.getMetaProperty())) {
                        // todo dynamic attributes
//                        DynamicAttributesMetaProperty dynamicAttributesMetaProperty =
//                                (DynamicAttributesMetaProperty) mpp.getMetaProperty();
//                        getDynamicAttributesGuiTools().initEntityPickerField(pickerField,
//                                dynamicAttributesMetaProperty.getAttribute());
                    }
                    boolean actionsByMetaAnnotations = ComponentsHelper.createActionsByMetaAnnotations(pickerField);
                    if (!actionsByMetaAnnotations) {
                        pickerField.addClearAction();
                    }
                } else {
                    pickerField.addOpenAction();
                    pickerField.addClearAction();
                }
            } else {
                LookupPickerField lookupPickerField = uiComponents.create(LookupPickerField.class);

                setValueSource(lookupPickerField, context);

                if (useOptionsLoader) {
                    // todo dynamic attributes
//                    DynamicAttributesMetaProperty metaProperty = (DynamicAttributesMetaProperty) mpp.getMetaProperty();
//                    CategoryAttribute attribute = metaProperty.getAttribute();
//                    ValueSource valueSource = context.getValueSource();
//                    if (valueSource instanceof ContainerValueSource) {
//                        setOptionsLoader(attribute, lookupPickerField, (ContainerValueSource) valueSource);
//                    }
                } else {
                    lookupPickerField.setOptions(options);
                }

                pickerField = lookupPickerField;

                ComponentsHelper.createActionsByMetaAnnotations(pickerField);
            }

            if (xmlDescriptor != null) {
                String captionProperty = xmlDescriptor.attributeValue("captionProperty");
                if (StringUtils.isNotEmpty(captionProperty)) {
                    pickerField.setCaptionMode(CaptionMode.PROPERTY);
                    pickerField.setCaptionProperty(captionProperty);
                }
            }
            setValidators(pickerField, context);

            return pickerField;
        } else {
            EntityLinkField linkField = uiComponents.create(EntityLinkField.class);

            setValueSource(linkField, context);
            setLinkFieldAttributes(linkField, context);

            return linkField;
        }
    }

    protected void setLinkFieldAttributes(EntityLinkField linkField, ComponentGenerationContext context) {
        Element xmlDescriptor = context.getXmlDescriptor();
        if (xmlDescriptor != null) {
            String linkScreen = xmlDescriptor.attributeValue("linkScreen");
            if (StringUtils.isNotEmpty(linkScreen)) {
                linkField.setScreen(linkScreen);
            }

            String invokeMethodName = xmlDescriptor.attributeValue("linkInvoke");
            if (StringUtils.isNotEmpty(invokeMethodName)) {
                linkField.setCustomClickHandler(new InvokeEntityLinkClickHandler(invokeMethodName));
            }

            String openTypeAttribute = xmlDescriptor.attributeValue("linkScreenOpenType");
            if (StringUtils.isNotEmpty(openTypeAttribute)) {
                OpenType openType = OpenType.valueOf(openTypeAttribute);
                linkField.setScreenOpenType(openType);
            }
        }
    }

    protected MetaPropertyPath resolveMetaPropertyPath(MetaClass metaClass, String property) {
        MetaPropertyPath mpp = metaClass.getPropertyPath(property);

        if (mpp == null && DynamicAttributesUtils.isDynamicAttribute(property)) {
            mpp = dynamicAttributesTools.getMetaPropertyPath(metaClass, property);
        }

        return mpp;
    }

    @SuppressWarnings("unchecked")
    protected void setValueSource(Field field, ComponentGenerationContext context) {
        field.setValueSource(context.getValueSource());
    }

    protected void setValidators(Field field, ComponentGenerationContext context) {
        CategoryAttribute categoryAttribute = getCategoryAttribute(context);
    // todo dynamic attributes

//        if (categoryAttribute != null && BooleanUtils.isNotTrue(categoryAttribute.getIsCollection())) {
//
//            Collection<Consumer<?>> validators = getDynamicAttributesGuiTools().createValidators(categoryAttribute);
//            if (validators != null && !validators.isEmpty()) {
//                for (Consumer<?> validator : validators) {
//                    //noinspection unchecked
//                    field.addValidator(validator);
//                }
//            }
//        }
    }

    protected void setCustomDataType(TextField field, ComponentGenerationContext context) {
        CategoryAttribute categoryAttribute = getCategoryAttribute(context);
        Datatype datatype = getDynamicAttributesGuiTools().getCustomNumberDatatype(categoryAttribute);

        if (datatype != null) {
            //noinspection unchecked
            field.setDatatype(datatype);
        }
    }

    protected void setValueChangedListeners(HasValue component, ComponentGenerationContext context) {

        CategoryAttribute attribute = getCategoryAttribute(context);
        if (attribute == null) {
            return;
        }
        Consumer<HasValue.ValueChangeEvent> valueChangedListener = getDynamicAttributesGuiTools()
                .getValueChangeEventListener(attribute);

        if (valueChangedListener != null) {
            //noinspection unchecked
            component.addValueChangeListener(valueChangedListener);
        }
    }

    protected void setEditable(Component.Editable component, ComponentGenerationContext context) {
        CategoryAttribute attribute = getCategoryAttribute(context);

        // todo dynamic attributes
//        if (attribute != null
//                && Boolean.TRUE.equals(attribute.getConfiguration().isReadOnly())) {
//            component.setEditable(false);
//        }
    }

    protected CategoryAttribute getCategoryAttribute(ComponentGenerationContext context) {
        MetaClass metaClass = context.getMetaClass();
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, context.getProperty());
        MetaProperty metaProperty = mpp.getMetaProperty();

        if (DynamicAttributesUtils.isDynamicAttribute(metaProperty)) {
            return DynamicAttributesUtils.getCategoryAttribute(metaProperty);
        }

        return null;
    }

    protected DynamicAttributesGuiTools getDynamicAttributesGuiTools() {
        return AppBeans.get(DynamicAttributesGuiTools.class);
    }

    protected void setOptionsLoader(CategoryAttribute categoryAttribute, LookupField lookupField, ContainerValueSource valueSource) {
        // todo dynamic attributes
//        InstanceContainer<?> container = valueSource.getContainer();
//        Entity entity = container.getItemOrNull();
//        if (entity != null) {
//            List options = dynamicAttributesTools.loadOptions((BaseGenericIdEntity) entity, categoryAttribute);
//            //noinspection unchecked
//            lookupField.setOptions(new ListOptions(options));
//        }
//        container.addItemChangeListener(e -> {
//            List options = dynamicAttributesTools.loadOptions((BaseGenericIdEntity) e.getItem(), categoryAttribute);
//            //noinspection unchecked
//            lookupField.setOptions(new ListOptions(options));
//        });
//
//        List<CategoryAttribute> dependsOnAttributes = categoryAttribute.getConfiguration().getDependsOnAttributes();
//        if (dependsOnAttributes != null && !dependsOnAttributes.isEmpty()) {
//            List<String> dependsOnAttributesCodes = dependsOnAttributes.stream()
//                    .map(a -> DynamicAttributesUtils.encodeAttributeCode(a.getCode()))
//                    .collect(Collectors.toList());
//
//            container.addItemPropertyChangeListener(e -> {
//                if (dependsOnAttributesCodes.contains(e.getProperty())) {
//                    List options = dynamicAttributesTools.loadOptions((BaseGenericIdEntity) e.getItem(), categoryAttribute);
//                    //noinspection unchecked
//                    lookupField.setOptions(new ListOptions(options));
//
//                    if (!options.contains(lookupField.getValue())) {
//                        //noinspection unchecked
//                        lookupField.setValue(null);
//                    }
//                }
//            });
//        }
    }

    protected static class InvokeEntityLinkClickHandler implements EntityLinkField.EntityLinkClickHandler {
        protected final String invokeMethodName;

        public InvokeEntityLinkClickHandler(String invokeMethodName) {
            this.invokeMethodName = invokeMethodName;
        }

        @Override
        public void onClick(EntityLinkField field) {
            Window frame = ComponentsHelper.getWindow(field);
            if (frame == null) {
                throw new IllegalStateException("Please specify Frame for EntityLinkField");
            }

            FrameOwner controller = frame.getFrameOwner();
            Method method;
            try {
                method = controller.getClass().getMethod(invokeMethodName, EntityLinkField.class);
                try {
                    method.invoke(controller, field);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Can't invoke method with name '%s'",
                            invokeMethodName), e);
                }
            } catch (NoSuchMethodException e) {
                try {
                    method = controller.getClass().getMethod(invokeMethodName);
                    try {
                        method.invoke(controller);
                    } catch (Exception ex) {
                        throw new RuntimeException(String.format("Can't invoke method with name '%s'",
                                invokeMethodName), ex);
                    }
                } catch (NoSuchMethodException e1) {
                    throw new IllegalStateException(String.format("No suitable methods named '%s' for invoke",
                            invokeMethodName));
                }
            }
        }
    }
}
