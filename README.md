# Welcome to the AssertJ assertions generator project !

[![Build Status](https://travis-ci.org/joel-costigliola/assertj-assertions-generator.svg?branch=master)](https://travis-ci.org/joel-costigliola/assertj-assertions-generator) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.assertj/assertj-assertions-generator-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.assertj/assertj-assertions-generator-maven-plugin)

## Overview 

The Assertion Generator is able to create assertions specific to your own classes, it comes with :
* a CLI tool (this project) and a [**maven plugin**](https://github.com/joel-costigliola/assertj-assertions-generator-maven-plugin).

Let's say that you have a `Player` class with `name` and `team` properties, the generator is able to create a `PlayerAssert` assertions class with `hasName` and `hasTeam` assertions, to write code like :

```java
assertThat(mvp).hasName("Lebron James").hasTeam("Miami Heat");
```

## Documentation

Please have a look at the complete documentation in [**assertj.org assertions generator section**](http://joel-costigliola.github.io/assertj/assertj-assertions-generator.html), including a [**quickstart guide**](http://joel-costigliola.github.io/assertj/assertj-assertions-generator.html#quickstart).
