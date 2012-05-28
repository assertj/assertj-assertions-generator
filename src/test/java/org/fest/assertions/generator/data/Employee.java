package org.fest.assertions.generator.data;

import org.fest.assertions.generator.Person;

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
