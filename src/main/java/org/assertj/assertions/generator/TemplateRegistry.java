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

import java.util.HashMap;

import org.apache.commons.lang3.Validate;

@SuppressWarnings("serial")
public class TemplateRegistry extends HashMap<Template.Type, Template> {

  public void register(Template template) {
    checkTemplateParameter(template);

    super.put(template.getType(), template);
  }

  public Template getTemplate(Template.Type templateType) {
    return super.get(templateType);
  }

  private static void checkTemplateParameter(Template assertionClassTemplate) {
    Validate.notNull(assertionClassTemplate, "Expecting a non null Template");
    Validate.notNull(assertionClassTemplate.getContent(), "Expecting a non null content in the Template");
  }
}
