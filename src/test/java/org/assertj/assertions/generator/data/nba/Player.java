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
 * Copyright 2012-2021 the original author or authors.
 */
package org.assertj.assertions.generator.data.nba;

import org.assertj.assertions.generator.data.Name;
import org.assertj.assertions.generator.data.nba.team.Team;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.deepEquals;

/**
 * @author Joel Costigliola
 */
@SuppressWarnings("unused")
public class Player {

  private Name name;
  private boolean rookie;
  private int pointsPerGame;
  private Integer pointerPerGameWrapped;
  private int assistsPerGame;
  private int reboundsPerGame;
  private String team;
  private float size;
  private Float sizeAsFloatWrapper;
  private double sizeDouble;
  private Double sizeAsDoubleWrapper;
  // boolean property to test #46
  private boolean isDisabled;
  private List<? extends Player> teamMates = new ArrayList<>();
  private List<int[]> points = new ArrayList<>();
  private String[] previousTeamNames = {};
  public List<? extends Team> previousTeams = new ArrayList<>();
  private List<? extends CharSequence> previousNames = new ArrayList<>();

  private boolean bad;

  public Player(Name name, String team) {
    setName(name);
    setTeam(team);
  }

  public boolean isDisabled() {
    return isDisabled;
  }

  public void setDisabled(boolean isDisabled) {
    this.isDisabled = isDisabled;
  }

  public Name getName() {
    return name;
  }

  public void setName(Name name) {
    this.name = name;
  }

  public int getPointsPerGame() {
    return pointsPerGame;
  }

  public void setPointsPerGame(int pointsPerGame) {
    this.pointsPerGame = pointsPerGame;
  }

  public int getAssistsPerGame() {
    return assistsPerGame;
  }

  public void setAssistsPerGame(int assistsPerGame) {
    this.assistsPerGame = assistsPerGame;
  }

  public int getReboundsPerGame() {
    return reboundsPerGame;
  }

  public void setReboundsPerGame(int reboundsPerGame) {
    this.reboundsPerGame = reboundsPerGame;
  }

  public String getTeam() {
    return team;
  }

  public void setTeam(String team) {
    this.team = team;
  }

  public List<? extends Player> getTeamMates() {
    return teamMates;
  }

  public void setTeamMates(List<Player> teamMates) {
    this.teamMates = teamMates;
  }

  public String[] getPreviousTeamNames() {
    return previousTeamNames;
  }

  public void setPreviousTeamNames(String[] previousTeamNames) {
    this.previousTeamNames = previousTeamNames;
  }

  public boolean isRookie() {
    return rookie;
  }

  public boolean wasRookie() {
    return rookie;
  }

  public boolean shouldWin() {
    return false;
  }

  public boolean canWin() {
    return false;
  }

  public boolean willWin() {
    return false;
  }

  public boolean cannotWin() {
    return !canWin();
  }

  public boolean hasTrophy() {
    return false;
  }

  public boolean doesNotHaveFun() {
    return false;
  }

  public boolean shouldNotPlay() {
    return false;
  }

  public void setRookie(boolean rookie) {
    this.rookie = rookie;
  }

  public boolean isInTeam(String team) {
    return deepEquals(this.team, team);
  }

  public List<int[]> getPoints() {
    return points;
  }

  // for testing only
  public void getVoid() {
    // empty
  }

  // for testing only
  public String get() {
    return "something";
  }

  // for testing only
  public String is() {
    return "somebody";
  }

  // for testing only
  public String getWithParam(String param) {
    return param;
  }

  // for testing only
  public void isVoid() {
    // empty
  }

  // for testing only
  public String isWithParam(String param) {
    return param;
  }

  public float getSize() {
    return size;
  }

  public List<? extends CharSequence> getPreviousNames() {
    return previousNames;
  }

  @Override
  public String toString() {
    return format("%s[%s %s, team=%s]", getClass().getSimpleName(), name.getFirst(), name.getLast(), team);
  }
}

