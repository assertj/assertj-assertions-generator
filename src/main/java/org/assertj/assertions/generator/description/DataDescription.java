package org.assertj.assertions.generator.description;

/**
 * base class to describe a field or a property/getter
 */
public abstract class DataDescription {

  private final String name;
  protected final TypeDescription typeDescription;

  public DataDescription(String name, TypeDescription typeDescription) {
    super();
    this.name = name;
    this.typeDescription = typeDescription;
  }

  public String getName() {
    return name;
  }

  public String getTypeName() {
    return typeDescription.getSimpleNameWithOuterClass();
  }

  public boolean isIterableType() {
    return typeDescription.isIterable();
  }

  public boolean isArrayType() {
    return typeDescription.isArray();
  }

  public boolean isPrimitiveType() {
    return typeDescription.isPrimitive();
  }

  public boolean isRealNumberType() {
    return typeDescription.isRealNumber();
  }

  public boolean isBooleanType() {
    return typeDescription.isBoolean();
  }

  public String getElementTypeName() {
    return typeDescription.getElementTypeName() == null ? null : typeDescription.getElementTypeName().getSimpleNameWithOuterClass();
  }

}