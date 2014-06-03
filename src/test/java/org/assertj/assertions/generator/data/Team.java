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
  
  // private field : no assertion should be generated
  @SuppressWarnings("unused")
  private String privateField;
  
  // property : assertion should be generated
  private String division;

  public String getDivision() {
    return division;
  }

  public Team(String name, String division) {
    super();
    this.name = name;
    this.division = division;
  }

}
