package org.fest.assertions.generator.data;

public class Employee extends Person {

  private String jobTitle;
  
  public Employee(String name, int age, String jobTitle) {
    super(name, age);
    this.jobTitle = jobTitle;
  }
  
  public String getJobTitle() {
    return jobTitle;
  }

}
