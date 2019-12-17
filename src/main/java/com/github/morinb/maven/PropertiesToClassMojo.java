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

package com.github.morinb.maven;

import com.github.morinb.maven.mapper.ToClassMapper;
import com.github.morinb.maven.mapper.impl.PropertiesToClassMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "p2c", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class PropertiesToClassMojo extends AbstractMojo {

    /**
     * List of .properties files relative to project basedir to transform.
     */
    @Parameter(property = "propertiesFiles", required = true)
    protected List<File> propertiesFiles;

    /**
     * Whether the plugin should add a @Generated annotation to generated files.
     */
    @Parameter(property = "displayGeneratedAnnotation", defaultValue = "true")
    protected boolean displayGeneratedAnnotation;

    /**
     * Whether the plugin should create constant with the value, or the key as value.
     */
    @Parameter(property = "generateValueValues", defaultValue = "false")
    protected boolean generateValueValues;

    /**
     * The prefix to prepend to each generated constant.
     */
    @Parameter(property = "constantPrefix")
    protected String constantPrefix;


    /**
     * Package name used in generated classes
     */
    @Parameter(property = "outputPackage", required = true)
    protected String outputPackage;

    /**
     * Maven Project bean
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;


    private final ToClassMapper mapper = new PropertiesToClassMapper();

    @Override
    public void execute() throws MojoExecutionException {
        if (propertiesFiles == null || propertiesFiles.isEmpty()) {
            getLog().warn("No 'propertiesFiles' defined in plugin configuration");
            return;
        }
        for (File propertiesFile : propertiesFiles) {
            try {
                mapper.mapToClass(propertiesFile,
                    outputPackage,
                    generateValueValues,
                    displayGeneratedAnnotation,
                    constantPrefix,
                    project,
                    getLog());
            } catch (IOException e) {
                throw new MojoExecutionException("Error while mapping '" + propertiesFile.getPath() + "' file", e);
            }
        }
    }
}
