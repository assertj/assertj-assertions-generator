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
