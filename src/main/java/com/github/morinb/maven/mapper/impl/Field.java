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

public class Field {
    private final String type;
    private final String name;
    private final String value;
    private final String key;

    public Field(String key, String type, String name, String value) {
        this.key = key;
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public static String guessTypeFromValue(String value) {
        if (value == null) {
            return "String";
        }
        try {
            Integer.parseInt(value);
            return "Integer";
        } catch (NumberFormatException e) {
            try {
                Long.parseLong(value);
                return "Long";
            } catch (NumberFormatException e1) {
                try {
                    Double.parseDouble(value);
                    return "Double";
                } catch (NumberFormatException e2) {
                    //next
                }
            }
        }
        // Default value
        return "String";
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
