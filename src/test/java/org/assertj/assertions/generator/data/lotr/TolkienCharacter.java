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
package org.assertj.assertions.generator.data.lotr;

/**
 * 
 * A simple class to illustrate AssertJ assertions.
 * 
 * @author Joel Costigliola
 */
public class TolkienCharacter {

  private String name;
  private Race race;
  private int age;

  public TolkienCharacter(String name, int age, Race race) {
    super();
    this.name = name;
    this.age = age;
    this.race = race;
  }

  public String getName() {
    return name;
  }

  public Race getRace() {
    return race;
  }

  public int getAge() {
    return age;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  public void setRace(Race race) {
    this.race = race;
  }

  public void setAge(int age) {
    this.age = age;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + age;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((race == null) ? 0 : race.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    TolkienCharacter other = (TolkienCharacter) obj;
    if (age != other.age) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (race == null) {
      if (other.race != null) return false;
    } else if (!race.equals(other.race)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "Character [name=" + name + ", race=" + race + ", age=" + age + "]";
  }

}
