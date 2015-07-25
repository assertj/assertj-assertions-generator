/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2015 the original author or authors.
 */
package org.assertj.assertions.generator.data;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This is a bean whose getters throws two exceptions.
 */
public class BeanWithTwoExceptions {
    public String getStringPropertyThrowsException() throws IOException, SQLException {
        return null;
    }
    public boolean isBooleanPropertyThrowsException() throws IOException, SQLException {
        return false;
    }
    public String[] getArrayPropertyThrowsException() throws IOException, SQLException {
        return null;
    }
    public List<String> getIterablePropertyThrowsException() throws IOException, SQLException {
        return null;
    }
}
