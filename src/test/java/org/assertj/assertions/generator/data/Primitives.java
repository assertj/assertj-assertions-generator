/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2020 the original author or authors.
 */
package org.assertj.assertions.generator.data;

/**
 * This is a class to test generation of primitive types and their wrapper
 */
public class Primitives {

  public boolean isBoolean() {
    return true;
  }

  public Boolean isBooleanWrapper() {
    return null;
  }

  // test #52
  private Boolean enabled;

  public Boolean isEnabled() {
    return enabled;
  }

  public byte getByte() {
    return 0;
  }

  public Byte getByteWrapper() {
    return null;
  }

  public char getChar() {
    return 'h';
  }

  public Character getCharacter() {
    return 'h';
  }

  public double getDouble() {
    return 0.0;
  }

  public Double getDoubleWrapper() {
    return 0.0;
  }

  public float getFloat() {
    return 1.0f;
  }

  public Float getFloatWrapper() {
    return 1.0f;
  }

  public long getLong() {
    return 1l;
  }

  public Long getLongWrapper() {
    return 1L;
  }

  public short getShort() {
    return 1;
  }

  public Short getShortWrapper() {
    return 1;
  }

  public int getInt() {
    return 1;
  }

  public Integer getInteger() {
    return 1;
  }
}
