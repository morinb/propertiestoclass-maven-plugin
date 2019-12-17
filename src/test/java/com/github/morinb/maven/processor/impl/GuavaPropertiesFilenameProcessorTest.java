/*
 * Copyright 2019 Baptiste MORIN
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

package com.github.morinb.maven.processor.impl;

import com.github.morinb.maven.processor.PropertiesFilenameProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GuavaPropertiesFilenameProcessorTest {

    private PropertiesFilenameProcessor processor;

    @BeforeEach
    public void beforeEach() {
        processor = new GuavaPropertiesFilenameProcessor();
    }

    @Test
    @DisplayName("Dotted Properties File name")
    public void dottedPropertiesFileName() {
        // Given
        final String dottedPropertiesName = "application.dev.properties";
        // When
        final String transformedName = processor.process(dottedPropertiesName);
        // Then
        assertNotNull(transformedName);
        assertEquals("ApplicationDevProperties", transformedName);
    }

    @Test
    @DisplayName("Null Properties File name")
    public void nullPropertiesFileName() {
        // Given
        // When
        final String transformedName = processor.process(null);
        // Then
        assertNull(transformedName);
    }

    @Test
    @DisplayName("Dashed Properties File name")
    public void dashedPropertiesFileName() {
        // Given
        final String name = "application-dev.properties";
        // When
        final String actual = processor.process(name);
        // Then
        assertNotNull(actual);
        assertEquals("ApplicationDevProperties", actual);
    }

}
