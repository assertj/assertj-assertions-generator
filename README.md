# Welcome to the AssertJ assertions generator project !

## Overview 

The Assertion Generator is able to create assertions specific to your own classes, it comes with :
* a CLI tool (this project) and a [**maven plugin**](https://github.com/joel-costigliola/assertj-assertions-generator-maven-plugin).

Let's say that you have a `Player` class with `name` and `team` properties, the generator is able to create a `PlayerAssert` assertions class with `hasName` and `hasTeam` assertions, to write code like :

```java
assertThat(mvp).hasName("Lebron James").hasTeam("Miami Heat");
```

## Documentation

Please have a look at the complete documentation in [**assertj.org assertions generator section**](http://joel-costigliola.github.io/assertj/assertj-assertions-generator.html), including a [**quickstart guide**](http://joel-costigliola.github.io/assertj/assertj-assertions-generator.html#quickstart).

[![Build Status](https://assertj.ci.cloudbees.com/buildStatus/icon?job=assertj-assertions-generator)](https://assertj.ci.cloudbees.com/job/assertj-assertions-generator/)
