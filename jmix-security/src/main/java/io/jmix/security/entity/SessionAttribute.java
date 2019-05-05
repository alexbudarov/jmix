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
package io.jmix.security.entity;

import io.jmix.core.AppBeans;
import io.jmix.core.Messages;
import io.jmix.core.entity.StandardEntity;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotations.MetaProperty;

import javax.persistence.*;

@Entity(name = "sec_SessionAttribute")
@Table(name = "SEC_SESSION_ATTR")
@SystemLevel
public class SessionAttribute extends StandardEntity {

    private static final long serialVersionUID = 4886168889020578592L;

    @Column(name = "NAME", length = 50)
    private String name;

    @Column(name = "STR_VALUE", length = 1000)
    private String stringValue;

    @Column(name = "DATATYPE", length = 20)
    private String datatype;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID")
    private Group group;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @MetaProperty
    public String getDatatypeCaption() {
        Messages messages = AppBeans.get(Messages.NAME);
        return messages.getMessage("Datatype." + datatype);
    }
}
