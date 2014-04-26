package org.assertj.assertions.generator;


public enum AssertionsEntryPointType {
  STANDARD("Assertions.java"),  BDD("BddAssertions.java"), SOFT("SoftAssertions.java");

  private String fileName;

  AssertionsEntryPointType(String fileName) {
   this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }
}
