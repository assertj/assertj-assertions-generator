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
package org.assertj.assertions.generator;

import java.io.File;

public class DefaultTemplateRegistryProducer {

  // default file for templates
  static final String DEFAULT_IS_ASSERTION_TEMPLATE = "is_assertion_template.txt";
  static final String DEFAULT_IS_WITHOUT_NEG_ASSERTION_TEMPLATE = "is_without_negative_assertion_template.txt";
  static final String DEFAULT_IS_WRAPPER_ASSERTION_TEMPLATE = "is_wrapper_assertion_template.txt";
  static final String DEFAULT_IS_WRAPPER_WITHOUT_NEG_ASSERTION_TEMPLATE = "is_wrapper_without_negative_assertion_template.txt";
  static final String DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ARRAY = "has_elements_assertion_template_for_array.txt";
  static final String DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ITERABLE = "has_elements_assertion_template_for_iterable.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE = "has_assertion_template.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_PRIMITIVE = "has_assertion_template_for_primitive.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_PRIMITIVE_WRAPPER = "has_assertion_template_for_primitive_wrapper.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_CHAR = "has_assertion_template_for_char.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_CHARACTER = "has_assertion_template_for_character.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_WHOLE_NUMBER = "has_assertion_template_for_whole_number.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_WHOLE_NUMBER_WRAPPER = "has_assertion_template_for_whole_number_wrapper.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_REAL_NUMBER = "has_assertion_template_for_real_number.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_REAL_NUMBER_WRAPPER = "has_assertion_template_for_real_number_wrapper.txt";
  static final String DEFAULT_CUSTOM_ASSERTION_CLASS_TEMPLATE = "custom_assertion_class_template.txt";
  static final String DEFAULT_CUSTOM_HIERARCHICAL_ASSERTION_CLASS_TEMPLATE = "custom_hierarchical_assertion_class_template.txt";
  static final String DEFAULT_CUSTOM_ABSTRACT_ASSERTION_CLASS_TEMPLATE = "custom_abstract_assertion_class_template.txt";
  static final String DEFAULT_ASSERTIONS_ENTRY_POINT_CLASS_TEMPLATE = "standard_assertions_entry_point_class_template.txt";
  static final String DEFAULT_ASSERTION_ENTRY_POINT_METHOD_TEMPLATE = "standard_assertion_entry_point_method_template.txt";
  static final String DEFAULT_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE = "soft_assertions_entry_point_class_template.txt";
  static final String DEFAULT_JUNIT_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE = "junit_soft_assertions_entry_point_class_template.txt";
  static final String DEFAULT_SOFT_ENTRY_POINT_ASSERTION_METHOD_TEMPLATE = "soft_assertion_entry_point_method_template.txt";
  static final String DEFAULT_BDD_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE = "bdd_assertions_entry_point_class_template.txt";
  static final String DEFAULT_BDD_ENTRY_POINT_ASSERTION_METHOD_TEMPLATE = "bdd_assertion_entry_point_method_template.txt";
  static final String DEFAULT_BDD_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE = "bdd_soft_assertions_entry_point_class_template.txt";
  static final String DEFAULT_BDD_SOFT_ENTRY_POINT_ASSERTION_METHOD_TEMPLATE = "bdd_soft_assertion_entry_point_method_template.txt";
  static final String DEFAULT_JUNIT_BDD_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE = "junit_bdd_soft_assertions_entry_point_class_template.txt";
  static final String DEFAULT_AUTO_CLOSEABLE_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE = "auto_closeable_soft_assertions_entry_point_class_template.txt";
  static final String DEFAULT_AUTO_CLOSEABLE_BDD_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE = "auto_closeable_bdd_soft_assertions_entry_point_class_template.txt";

  public static TemplateRegistry create(String templateDirectory) {
    TemplateRegistry templateRegistry = new TemplateRegistry();
    templateRegistry.register(new Template(Template.Type.ASSERT_CLASS,
                                           new File(templateDirectory, DEFAULT_CUSTOM_ASSERTION_CLASS_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.HIERARCHICAL_ASSERT_CLASS,
                                           new File(templateDirectory,
                                                    DEFAULT_CUSTOM_HIERARCHICAL_ASSERTION_CLASS_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.ABSTRACT_ASSERT_CLASS,
                                           new File(templateDirectory,
                                                    DEFAULT_CUSTOM_ABSTRACT_ASSERTION_CLASS_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.HAS,
                                           new File(templateDirectory, DEFAULT_HAS_ASSERTION_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.HAS_FOR_PRIMITIVE,
                                           new File(templateDirectory,
                                                    DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_PRIMITIVE)));
    templateRegistry.register(new Template(Template.Type.HAS_FOR_PRIMITIVE_WRAPPER,
                                           new File(templateDirectory,
                                                    DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_PRIMITIVE_WRAPPER)));
    templateRegistry.register(new Template(Template.Type.HAS_FOR_REAL_NUMBER,
                                           new File(templateDirectory,
                                                    DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_REAL_NUMBER)));
    templateRegistry.register(new Template(Template.Type.HAS_FOR_REAL_NUMBER_WRAPPER,
                                           new File(templateDirectory,
                                                    DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_REAL_NUMBER_WRAPPER)));
    templateRegistry.register(new Template(Template.Type.HAS_FOR_WHOLE_NUMBER,
                                           new File(templateDirectory,
                                                    DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_WHOLE_NUMBER)));
    templateRegistry.register(new Template(Template.Type.HAS_FOR_WHOLE_NUMBER_WRAPPER,
                                           new File(templateDirectory,
                                                    DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_WHOLE_NUMBER_WRAPPER)));
    templateRegistry.register(new Template(Template.Type.HAS_FOR_CHAR,
                                           new File(templateDirectory,
                                                    DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_CHAR)));
    templateRegistry.register(new Template(Template.Type.HAS_FOR_CHARACTER,
                                           new File(templateDirectory,
                                                    DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_CHARACTER)));
    templateRegistry.register(new Template(Template.Type.HAS_FOR_ITERABLE,
                                           new File(templateDirectory,
                                                    DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ITERABLE)));
    templateRegistry.register(new Template(Template.Type.HAS_FOR_ARRAY,
                                           new File(templateDirectory,
                                                    DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ARRAY)));
    templateRegistry.register(new Template(Template.Type.IS,
                                           new File(templateDirectory, DEFAULT_IS_ASSERTION_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.IS_WITHOUT_NEGATION,
                                           new File(templateDirectory, DEFAULT_IS_WITHOUT_NEG_ASSERTION_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.IS_WRAPPER,
                                           new File(templateDirectory, DEFAULT_IS_WRAPPER_ASSERTION_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.IS_WRAPPER_WITHOUT_NEGATION,
                                           new File(templateDirectory,
                                                    DEFAULT_IS_WRAPPER_WITHOUT_NEG_ASSERTION_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.ASSERTIONS_ENTRY_POINT_CLASS,
                                           new File(templateDirectory,
                                                    DEFAULT_ASSERTIONS_ENTRY_POINT_CLASS_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.ASSERTION_ENTRY_POINT,
                                           new File(templateDirectory,
                                                    DEFAULT_ASSERTION_ENTRY_POINT_METHOD_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.SOFT_ASSERTIONS_ENTRY_POINT_CLASS,
                                           new File(templateDirectory,
                                                    DEFAULT_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.JUNIT_SOFT_ASSERTIONS_ENTRY_POINT_CLASS,
                                           new File(templateDirectory,
                                                    DEFAULT_JUNIT_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.SOFT_ENTRY_POINT_METHOD_ASSERTION,
                                           new File(templateDirectory,
                                                    DEFAULT_SOFT_ENTRY_POINT_ASSERTION_METHOD_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.BDD_ASSERTIONS_ENTRY_POINT_CLASS,
                                           new File(templateDirectory,
                                                    DEFAULT_BDD_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.BDD_ENTRY_POINT_METHOD_ASSERTION,
                                           new File(templateDirectory,
                                                    DEFAULT_BDD_ENTRY_POINT_ASSERTION_METHOD_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.BDD_SOFT_ASSERTIONS_ENTRY_POINT_CLASS,
                                           new File(templateDirectory,
                                                    DEFAULT_BDD_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.BDD_SOFT_ENTRY_POINT_METHOD_ASSERTION,
                                           new File(templateDirectory,
                                                    DEFAULT_BDD_SOFT_ENTRY_POINT_ASSERTION_METHOD_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.JUNIT_BDD_SOFT_ASSERTIONS_ENTRY_POINT_CLASS,
                                           new File(templateDirectory,
                                                    DEFAULT_JUNIT_BDD_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.BDD_SOFT_ENTRY_POINT_METHOD_ASSERTION,
                                           new File(templateDirectory,
                                                    DEFAULT_BDD_SOFT_ENTRY_POINT_ASSERTION_METHOD_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.AUTO_CLOSEABLE_SOFT_ASSERTIONS_ENTRY_POINT_CLASS,
                                           new File(templateDirectory,
                                                    DEFAULT_AUTO_CLOSEABLE_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE)));
    templateRegistry.register(new Template(Template.Type.AUTO_CLOSEABLE_BDD_SOFT_ASSERTIONS_ENTRY_POINT_CLASS,
                                           new File(templateDirectory,
                                                    DEFAULT_AUTO_CLOSEABLE_BDD_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE)));
    return templateRegistry;
  }
}
