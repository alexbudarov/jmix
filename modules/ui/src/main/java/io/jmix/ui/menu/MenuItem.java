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
package io.jmix.ui.menu;

import io.jmix.core.security.PermissionType;
import io.jmix.core.security.Security;
import io.jmix.ui.components.KeyCombination;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Main menu item descriptor
 */
public class MenuItem {

    private MenuItem parent;
    private List<MenuItem> children = new ArrayList<>();

    private String id;
    private String screen;
    private String runnableClass;
    private String bean;
    private String beanMethod;
    private String stylename;
    private String icon;
    private String caption;
    private String description;
    private Element descriptor;
    private boolean separator = false;
    private boolean expanded = false;

    private KeyCombination shortcut;
    private boolean isMenu = false;

    public MenuItem(MenuItem parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    public MenuItem(String id) {
        this(null, id);
    }

    public boolean isMenu() {
        return isMenu;
    }

    public void setMenu(boolean isMenu) {
        this.isMenu = isMenu;
    }

    /** Parent item. Null if this is root item. */
    public MenuItem getParent() {
        return parent;
    }

    /** Children items */
    public List<MenuItem> getChildren() {
        return children;
    }

    /**
     * Menu item ID as defined in <code>menu-config.xml</code>
     */
    public String getId() {
        return id;
    }

    public Element getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(Element descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * @return a raw string from menu XML config, can be a reference to localization message, e.g. {@code mainMsg://menuitem.caption}
     *
     * @see MenuConfig#getItemCaption(MenuItem)
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Sets the given {@code caption} to menu item.
     *
     * @param caption menu item caption
     *
     * @see MenuConfig#getItemCaption(MenuItem)
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * @return a raw string from menu XML config, can be a reference to localization message, e.g. {@code mainMsg://menuitem.description}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the given {@code description} to menu item.
     *
     * @param description menu item description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getStylename() {
        return stylename;
    }

    public void setStylename(String stylename) {
        this.stylename = stylename;
    }

    public boolean isPermitted(Security security) {
        if (StringUtils.isEmpty(id) || isSeparator()) {
             return true;
        } else {
            boolean screenPermitted = security.isScreenPermitted(id);
            if (screenPermitted) {
                Element permissionsElem = descriptor.element("permissions");
                if (permissionsElem != null) {
                    for (Element element : permissionsElem.elements("permission")) {
                        PermissionType type = PermissionType.valueOf(element.attributeValue("type"));
                        String target = element.attributeValue("target");
                        // todo security
                        // screenPermitted = security.isPermitted(type, target);
                        if (!screenPermitted)
                            break;
                    }
                }
            }
            return screenPermitted;
        }
    }

    public KeyCombination getShortcut() {
        return shortcut;
    }

    public void setShortcut(KeyCombination shortcut) {
        this.shortcut = shortcut;
    }

    public boolean isSeparator() {
        return separator || "-".equals(id);
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getRunnableClass() {
        return runnableClass;
    }

    public void setRunnableClass(String runnableClass) {
        this.runnableClass = runnableClass;
    }

    public String getBean() {
        return bean;
    }

    public void setBean(String bean) {
        this.bean = bean;
    }

    public String getBeanMethod() {
        return beanMethod;
    }

    public void setBeanMethod(String beanMethod) {
        this.beanMethod = beanMethod;
    }
}
