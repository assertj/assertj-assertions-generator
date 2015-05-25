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
