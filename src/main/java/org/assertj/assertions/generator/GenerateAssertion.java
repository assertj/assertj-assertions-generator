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
 * Copyright 2012-2024 the original author or authors.
 */
package org.assertj.assertions.generator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this annotation to tell the generator to generate assertions for the annotated elements, you can annotate public 
 * methods or classes, if used on a class all public methods are selected for assertions generation. 
 * <p>
 * In the following example, assertions will be generated for thisIsAProperty, anotherProperty and getStuff 
 * (standard getter):
 * <pre><code class='java'>
 * public class AnnotatedClass {
 *
 *  {@literal @}GenerateAssertion
 *   public boolean thisIsAProperty() {
 *     return false;
 *   }
 *
 *   public boolean thisIsNotAProperty() {
 *     return false;
 *   }
 *
 *   public boolean getStuff() {
 *   return false;
 *   }
 *
 *  {@literal @}GenerateAssertion
 *   public Object anotherProperty() {
 *     return null;
 *   }
 * }</code></pre>
 * In the next example, assertions will be generated for all methods 
 * (standard getter):
 * <pre><code class='java'>  {@literal @}GenerateAssertion
 * public class AnnotatedClass {
 *
 *   public boolean thisIsAProperty() {
 *     return false;
 *   }
 *
 *   public boolean getStuff() {
 *   return false;
 *   }
 *
 *   public Object anotherProperty() {
 *     return null;
 *   }
 * }</code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateAssertion {
}
