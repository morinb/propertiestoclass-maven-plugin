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

package com.github.morinb.maven.mapper.impl;

import com.github.morinb.maven.mapper.ToClassMapper;
import com.github.morinb.maven.processor.PropertiesFilenameProcessor;
import com.github.morinb.maven.processor.impl.GuavaPropertiesFilenameProcessor;
import com.google.common.base.CaseFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class PropertiesToClassMapper implements ToClassMapper {
    private final PropertiesFilenameProcessor processor = new GuavaPropertiesFilenameProcessor();

    public PropertiesToClassMapper() {
        // empty constructor
    }


    @Override
    public void mapToClass(File propertiesFile, String outputPackage, boolean generateValueValues, boolean displayGeneratedAnnotation, String constantPrefix, MavenProject project, Log log) throws IOException {
        Properties prop = new Properties();
        final VelocityContext vContext = new VelocityContext();
        vContext.put("packageName", outputPackage);
        vContext.put("dateString", LocalDateTime.now().toString());
        vContext.put("displayGenerated", displayGeneratedAnnotation);
        vContext.put("generateValueValues", generateValueValues);
        vContext.put("propertiesFile", propertiesFile.getAbsolutePath());
        vContext.put("prefix", constantPrefix);
        String javaVersion = System.getProperty("java.version");
        boolean lowerJdk11 = javaVersion.startsWith("1.")
            || javaVersion.startsWith("8.")
            || javaVersion.startsWith("9.")
            || javaVersion.startsWith("10.");
        vContext.put("lowerJdk11", lowerJdk11);
        final String className = processor.process(propertiesFile.getName());
        vContext.put("className", className);
        final File generatedSourcesDirectory = new File(project.getBuild().getDirectory() + "/generated-sources/java");
        log.info("Writing class " + className + " to " + generatedSourcesDirectory.getAbsolutePath());
        if (!generatedSourcesDirectory.exists() && !generatedSourcesDirectory.mkdirs()) {
            throw new IOException("Creation of " + generatedSourcesDirectory.getAbsolutePath() + " impossible.");
        }

        final File generatedPackageDirectory = new File(generatedSourcesDirectory, formatPackageToFolder(outputPackage));
        if (!generatedPackageDirectory.exists() && !generatedPackageDirectory.mkdirs()) {
            throw new IOException("Creation of " + generatedPackageDirectory.getAbsolutePath() + " impossible.");
        }

        prop.load(new FileInputStream(propertiesFile));
        List<Field> fields = new ArrayList<>();
        for (String key : prop.stringPropertyNames()) {
            if (generateValueValues) {

                log.info("Processing key : " + key);
                String value = prop.getProperty(key);
                String type = Field.guessTypeFromValue(value);

                String name = transformKeyName(key);
                fields.add(new Field(key, type, name, "String".equals(type) ? '"' + value + '"' : value));
            } else {
                fields.add(new Field(key, "String", transformKeyName(key) + "_KEY", '"' + key + '"'));

            }
        }
        vContext.put("fields", fields);

        log.info("Generating java class " + className);
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        Template t = velocityEngine.getTemplate("/velocity/JavaClass.vm");

        File classFile = new File(generatedPackageDirectory, className + ".java");

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(classFile)))) {
            t.merge(vContext, pw);
        }
    }

    private String transformKeyName(String key) {
        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE,
            key.replaceAll("\\W", "-").replaceAll("-+", "-").toLowerCase());
    }

    private String formatPackageToFolder(String outputPackage) {
        return outputPackage.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
    }

}
