# AssertJ - Assertions Generator

![CI](https://github.com/assertj/assertj-assertions-generator/workflows/CI/badge.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.assertj/assertj-assertions-generator-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.assertj/assertj-assertions-generator-maven-plugin)
[![codecov](https://codecov.io/gh/szatyinadam/assertj-assertions-generator/branch/main/graph/badge.svg?token=T0OTMN3AMY)](https://codecov.io/gh/szatyinadam/assertj-assertions-generator)

## Overview 

The Assertions Generator can create specific assertions for your classes. It comes with :
* a CLI tool (this project) 
* a [**maven plugin**](https://github.com/assertj/assertj-assertions-generator-maven-plugin).
* a [**gradle plugin**](https://github.com/assertj/assertj-generator-gradle-plugin).

Let's say you have a `Player` class with `name` and `team` properties. The generator will create a `PlayerAssert` assertions class with `hasName` and `hasTeam` assertions. This allows you to write :

```java
assertThat(mvp).hasName("Lebron James").hasTeam("Miami Heat");
```

## Documentation

Please have a look at the complete documentation in the [**assertions generator section**](http://joel-costigliola.github.io/assertj/assertj-assertions-generator.html), including a [**quickstart guide**](http://joel-costigliola.github.io/assertj/assertj-assertions-generator.html#quickstart).
