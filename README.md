# AssertJ - Assertions Generator [![Maven Central](https://img.shields.io/maven-central/v/org.assertj/assertj-assertions-generator.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.assertj%22%20AND%20a:%22assertj-assertions-generator%22) [![Javadocs](http://www.javadoc.io/badge/org.assertj/assertj-assertions-generator.svg)](http://www.javadoc.io/doc/org.assertj/assertj-assertions-generator)

[![CI](https://github.com/assertj/assertj-assertions-generator/actions/workflows/main.yml/badge.svg?branch=main)](https://github.com/assertj/assertj-assertions-generator/actions/workflows/main.yml?query=branch%3Amain)

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
