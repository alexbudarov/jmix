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

package io.jmix.ui.components.impl;

import com.haulmont.bali.util.Preconditions;
import com.haulmont.cuba.gui.components.RelativePathResource;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.vaadin.server.ExternalResource;

import java.net.MalformedURLException;
import java.net.URL;

public class WebRelativePathResource extends WebAbstractResource implements WebResource, RelativePathResource {

    protected String path;

    protected String mimeType;

    @Override
    public RelativePathResource setPath(String path) {
        Preconditions.checkNotNullArgument(path);

        this.path = path;
        hasSource = true;

        fireResourceUpdateEvent();

        return this;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    protected void createResource() {
        try {
            URL context = new URL(ControllerUtils.getLocationWithoutParams());
            resource = new ExternalResource(new URL(context, path));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Can't create RelativePathResource", e);
        }
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;

        if (resource != null) {
            ((ExternalResource) resource).setMIMEType(mimeType);
        }
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }
}