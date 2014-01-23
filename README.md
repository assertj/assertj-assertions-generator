# Welcome to the AssertJ assertions generator project !

## Overview 

The Assertion Generator is able to create assertions specific to your own classes, it comes with :
* a CLI tool (this project) and a [**maven plugin**](https://github.com/joel-costigliola/assertj-assertions-generator-maven-plugin).

Let's say that you have a `Player` class with `name` and `team` properties, the generator is able to create a `PlayerAssert` assertions class with `hasName` and `hasTeam` assertions, to write code like :

```java
assertThat(mvp).hasName("Lebron James").hasTeam("Miami Heat");
```
Since 1.2.0 version, the generator also creates an `Assertions` class with `assertThat` methods giving access to each generated `*Assert` classes.  
In the case where `PlayerAssert` and `GameAssert` have been generated, the generator will also create the `Assertions` class below:

```java
public class Assertions {

  /**
   * Creates a new instance of <code>{@link GameAssert}</code>.
   *
   * @param actual the actual value.
   * @return the created assertion object.
   */
  public static GameAssert assertThat(Game actual) {
    return new GameAssert(actual);
  }

  /**
   * Creates a new instance of <code>{@link PlayerAssert}</code>.
   *
   * @param actual the actual value.
   * @return the created assertion object.
   */
  public static PlayerAssert assertThat(Player actual) {
    return new PlayerAssert(actual);
  }

  /**
   * Creates a new </code>{@link Assertions}</code>.
   */
  protected Assertions() {
    // empty
  }
}
```

## Documentation

Please have a look at the complete documentation in [**assertj.org assertions generator section**](http://joel-costigliola.github.io/assertj/assertj-assertions-generator.html), including a [**quickstart guide**](http://joel-costigliola.github.io/assertj/assertj-assertions-generator.html#quickstart).
