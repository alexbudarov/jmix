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

package io.jmix.ui.sys;

import io.jmix.core.BeanLocator;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Resources;
import io.jmix.ui.logging.ScreenLifeCycle;
import io.jmix.ui.logging.UIPerformanceLogger;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.perf4j.StopWatch;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Loads screen XML descriptors.
 */
@Component(ScreenXmlLoader.NAME)
public class ScreenXmlLoader {

    public static final String NAME = "jmix_ScreenXmlLoader";

    @Inject
    protected Resources resources;
    @Inject
    protected ScreenXmlDocumentCache screenXmlCache;
    @Inject
    protected ScreenXmlParser screenXmlParser;
    @Inject
    protected BeanLocator beanLocator;

    /**
     * Loads a descriptor.
     *
     * @param resourcePath path to the resource containing the XML
     * @param id           screen ID
     * @param params       screen parameters
     * @return root XML element
     */
    public Element load(String resourcePath, String id, Map<String, Object> params) {
        StopWatch xmlLoadWatch = UIPerformanceLogger.createStopWatch(ScreenLifeCycle.XML, id);

        String template = loadTemplate(resourcePath);
        Document document = getDocument(template, params);

        xmlLoadWatch.stop();
        return document.getRootElement();
    }

    protected String loadTemplate(String resourcePath) {
        try (InputStream stream = resources.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new DevelopmentException("Template is not found " + resourcePath, "Path", resourcePath);
            }

            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read screen template");
        }
    }

    protected Document getDocument(String template, Map<String, Object> params) {
        Document document = screenXmlCache.get(template);
        if (document == null) {
            document = createDocument(template, params);
            screenXmlCache.put(template, document);
        }
        return document;
    }

    protected Document createDocument(String template, Map<String, Object> params) {
        Document originalDocument = screenXmlParser.parseDescriptor(template);

        XmlInheritanceProcessor processor = beanLocator.getPrototype(XmlInheritanceProcessor.NAME,
                originalDocument, params);
        Element resultRoot = processor.getResultRoot();

        return resultRoot.getDocument();
    }
}
