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
 * Copyright 2012-2017 the original author or authors.
 */
package org.assertj.assertions.generator.data;

import java.util.ArrayList;
import java.util.List;

import org.assertj.assertions.generator.data.nba.Player;

public class Team {

  // to check that no assertion is created for public static field
  public static String staticField;
  
  public String name;
  public double victoryRatio;
  public boolean westCoast;
  public int rank;
  public List<Player> players = new ArrayList<Player>();
  public List<int[]> points = new ArrayList<int[]>();
  public String[] oldNames = {};
  
  // private field : assertion should be generated if configured so 
  @SuppressWarnings("unused")
  private String privateField;
  
  // property : assertion should be generated
  private String division;

  public String getDivision() {
    return division;
  }

  public Team(String name, String division) {
    this.name = name;
    this.division = division;
  }

}
