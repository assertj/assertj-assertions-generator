package org.assertj.assertions.generator.data;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This is a bean whose getters throws one exception.
 */
public class BeanWithOneException {
    public String getStringPropertyThrowsException() throws IOException {
        return null;
    }
    public boolean isBooleanPropertyThrowsException() throws IOException {
        return false;
    }
    public String[] getArrayPropertyThrowsException() throws IOException {
        return null;
    }
    public List<String> getIterablePropertyThrowsException() throws IOException {
        return null;
    }
}
