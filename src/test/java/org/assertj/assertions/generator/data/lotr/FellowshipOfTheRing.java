package org.assertj.assertions.generator.data.lotr;

import static org.assertj.assertions.generator.data.lotr.Race.dwarf;
import static org.assertj.assertions.generator.data.lotr.Race.elf;
import static org.assertj.assertions.generator.data.lotr.Race.hobbit;
import static org.assertj.assertions.generator.data.lotr.Race.maia;
import static org.assertj.assertions.generator.data.lotr.Race.man;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FellowshipOfTheRing {

  protected final TolkienCharacter frodo = new TolkienCharacter("Frodo", 33, hobbit);
  protected final TolkienCharacter sam = new TolkienCharacter("Sam", 38, hobbit);
  protected final TolkienCharacter merry = new TolkienCharacter("Merry", 36, hobbit);
  protected final TolkienCharacter pippin = new TolkienCharacter("Pippin", 28, hobbit);
  protected final TolkienCharacter gandalf = new TolkienCharacter("Gandalf", 2020, maia);
  protected final TolkienCharacter gimli = new TolkienCharacter("Gimli", 139, dwarf);
  protected final TolkienCharacter legolas = new TolkienCharacter("Legolas", 1000, elf);
  protected final TolkienCharacter aragorn = new TolkienCharacter("Aragorn", 87, man);
  protected final TolkienCharacter boromir = new TolkienCharacter("Boromir", 87, man);
  protected final TolkienCharacter sauron = new TolkienCharacter("Sauron", 50000, maia);
  protected final TolkienCharacter galadriel = new TolkienCharacter("Legolas", 3000, elf);
  protected final TolkienCharacter elrond = new TolkienCharacter("Legolas", 3000, elf);
  protected final List<TolkienCharacter> fellowshipOfTheRing = new ArrayList<TolkienCharacter>();

  public FellowshipOfTheRing() {
    super();
    fellowshipOfTheRing.add(frodo);
    fellowshipOfTheRing.add(sam);
    fellowshipOfTheRing.add(merry);
    fellowshipOfTheRing.add(pippin);
    fellowshipOfTheRing.add(gandalf);
    fellowshipOfTheRing.add(legolas);
    fellowshipOfTheRing.add(gimli);
    fellowshipOfTheRing.add(aragorn);
    fellowshipOfTheRing.add(boromir);
  }
  
  public Map<Race, List<TolkienCharacter>> getFellowsByRace() {
    Map<Race, List<TolkienCharacter>> fellowsByRace = new HashMap<Race, List<TolkienCharacter>>();
    fellowsByRace.put(man, tolkienCharacterList(aragorn, boromir));
    fellowsByRace.put(hobbit, tolkienCharacterList(frodo, sam, pippin, merry));
    fellowsByRace.put(dwarf, tolkienCharacterList(gimli));
    fellowsByRace.put(elf, tolkienCharacterList(legolas));
    fellowsByRace.put(maia, tolkienCharacterList(gandalf));
    return fellowsByRace;

  }

  private static List<TolkienCharacter> tolkienCharacterList(TolkienCharacter... characters) {
    List<TolkienCharacter> list = new ArrayList<TolkienCharacter>(characters.length);
    for (TolkienCharacter tolkienCharacter : characters) {
      list.add(tolkienCharacter);
    }
    return list;

  }
}
