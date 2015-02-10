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
 * Copyright 2012-2014 the original author or authors.
 */
package org.assertj.assertions.generator.data;

import java.io.IOException;
import java.util.List;

/**
 * This is a class whose properties clash with Java keywords.
 */
public class Keywords {
  public String getAbstract() throws java.io.IOException {
	return null;
  }
  public Object getAssert() {
	return null;
  }
  public Boolean getBoolean() {
	return null;
  }
  public List<Object> getBreak() {
	return null;
  }
  public Byte getByte() {
	return null;
  }
  public Object[] getCase() {
	return null;
  }
  public Object getCatch() {
	return null;
  }
  public char getChar() {
	return 'h';
  }
  //  Can't override Object.getClass()
  public boolean isClass() {
	return false;
  }
  public Class<?> getConst() {
	return null;
  }
  public String getContinue() {
	return null;
  }
  public List<String> getDefault() throws IOException {
	return null;
  }
  public Object getDo() {
	return null;
  }
  public double getDouble() {
	return 0.0;
  }
  public Object getElse() {
	return null;
  }
  public Object getEnum() {
	return null;
  }
  public Object getExtends() {
	return null;
  }
  public Object getFalse() {
	return null;
  }
  public Object getFinal() {
	return null;
  }
  public Object getFinally() {
	return null;
  }
  public float getFloat() {
	return 1.0f;
  }
  public Object getFor() throws IOException {
	return null;
  }
  public Object getGoto() {
	return null;
  }
  public Object getIf() {
	return null;
  }
  public Object getImplements() {
	return null;
  }
  public Object getImport() {
	return null;
  }
  public Object getInstanceof() {
	return null;
  }
  public int getInt() throws IOException {
	return 1;
  }
  public Object getInterface() {
	return null;
  }
  public long getLong() {
	return 1;
  }
  public Object getNative() {
	return null;
  }
  public Object getNew() {
	return null;
  }
  public Object getNull() {
	return null;
  }
  public Object getPackage() {
	return null;
  }
  public Object getPrivate() {
	return null;
  }
  public Object getProtected() {
	return null;
  }
  public Object getPublic() {
	return null;
  }
  public Object getReturn() {
	return null;
  }
  public short getShort() {
	return 1;
  }
  public Object getStatic() {
	return null;
  }
  public Object getStrictfp() {
	return null;
  }
  public Object getSuper() {
	return null;
  }
  public Object getSynchronized() {
	return null;
  }
  public String[] getSwitch() throws IOException {
	return null;
  }
  public Object getThis() {
	return null;
  }
  public Object getThrow() {
	return null;
  }
  public Object getThrows() {
	return null;
  }
  public Object getTransient() {
	return null;
  }
  public Object getTrue() {
	return null;
  }
  public Object getTry() {
	return null;
  }
  public Void getVoid() {
	return null;
  }
  public Object getVolatile() {
	return null;
  }
  public Object getWhile() {
	return null;
  }
}
