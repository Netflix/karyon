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

package netflix.karyon.health;

/**
 * Default health check handler implementation which always returns healthy. There is always a single instance of this
 * class which can be obtained via {@link #INSTANCE}
 *
 * @author Nitesh Kant
 */
public class AlwaysHealthyHealthCheck implements HealthCheckHandler {

    public static final AlwaysHealthyHealthCheck INSTANCE = new AlwaysHealthyHealthCheck();

    public AlwaysHealthyHealthCheck() {
    }

    @Override
    public int getStatus() {
        return 200;
    }
}
