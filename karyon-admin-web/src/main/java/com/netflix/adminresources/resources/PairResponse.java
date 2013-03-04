/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.netflix.adminresources.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Nitesh Kant
 */
public class PairResponse {

    List<Pair> data;

    public PairResponse() {
        data = new ArrayList<Pair>();
    }

    public PairResponse(Map<String, ?> input) {
        this();
        for (Map.Entry<String, ?> entry : input.entrySet()) {
            data.add(new Pair(String.valueOf(entry.getKey()), entry.getValue()));
        }
    }

    public void addEntry(String name, String value) {
        data.add(new Pair(name, value));
    }

    private static class Pair {

        private String name;
        private Object value;

        private Pair(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }
    }
}
