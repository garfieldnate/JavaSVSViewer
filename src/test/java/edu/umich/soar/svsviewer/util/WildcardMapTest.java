package edu.umich.soar.svsviewer.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.*;

public class WildcardMapTest {

  @Test
  public void put() {
    WildcardMap<String> map = new WildcardMap<>();
    map.put("name", "John");
    map.put("address", "4200");
    map.put("DOB", "12/2/92");

    assertEquals(3, map.size(), "Expected size=3");
  }

  @Test
  public void testGet() {
    WildcardMap<String> map = new WildcardMap<>();
    map.put("name", "Amy");
    map.put("names", "Amys");

    String name = map.get("name");

    assertEquals("Amy", name, "Expected map to contain 'name'=>'Amy'");
  }

  @Test
  public void testGetAsteriskIsTreatedLiterally() {
    WildcardMap<String> map = new WildcardMap<>();
    map.put("name*", "Amy");
    map.put("name*s", "Amys");

    String name = map.get("name*");

    assertEquals("Amy", name, "Expected map to contain 'name'=>'Amy'");
  }

  @Test
  public void getWithWildcards() {
    WildcardMap<String> map = new WildcardMap<>();
    map.put("name", "Amy");
    map.put("nate", "Nate");
    map.put("na*e", "Star");

    List<String> names = map.getWithWildcards("na*e");

    assertTrue(names.contains("Amy"), "Amy should be in the result set");
    assertTrue(names.contains("Nate"), "Nate should be in the result set");
    assertTrue(names.contains("Star"), "Star should be in the result set");
  }

  @Test
  public void getWithWildcardAtBeginAndEnd() {
    WildcardMap<String> map = new WildcardMap<>();
    map.put("name", "Amy");
    map.put("nate", "Nate");
    map.put("knave", "knight");
    map.put("knaves", "knights");

    List<String> names = map.getWithWildcards("*na*e*");

    assertTrue(names.contains("Amy"), "Amy should be in the result set");
    assertTrue(names.contains("Nate"), "Nate should be in the result set");
    assertTrue(names.contains("knight"), "knight should be in the result set");
    assertTrue(names.contains("knights"), "knight should be in the result set");
  }
}
