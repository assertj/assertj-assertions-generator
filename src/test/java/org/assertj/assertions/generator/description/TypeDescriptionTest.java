package org.assertj.assertions.generator.description;

import static org.junit.rules.ExpectedException.none;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class TypeDescriptionTest {
  
  @Rule
  public ExpectedException thrown = none();
  
  @Test
  public void should_throw_exception_in_constructor_call_if_typename_parameter_is_null() throws Exception {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("typeName must not be null.");
    new TypeDescription(null);
  }

}
