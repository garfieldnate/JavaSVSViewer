package edu.umich.soar.svsviewer.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.*;

public class WildcardMapTest {

  @Test
  public void testPut() {
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
    map.put("nantucket is nice", "foo");

    List<WildcardMap.Entry<String>> names = map.getWithWildcards("na*e");

    assertTrue(
        names.contains(new WildcardMap.Entry<>("name", "Amy")), "Amy should be in the result set");
    assertTrue(
        names.contains(new WildcardMap.Entry<>("nate", "Nate")),
        "Nate should be in the result set");
    assertTrue(
        names.contains(new WildcardMap.Entry<>("na*e", "Star")),
        "Star should be in the result set");
    assertTrue(
        names.contains(new WildcardMap.Entry<>("nantucket is nice", "foo")),
        "Star should be in the result set");
  }

  @Test
  public void getWithWildcardAtBeginAndEnd() {
    WildcardMap<String> map = new WildcardMap<>();
    map.put("name", "Amy");
    map.put("nate", "Nate");
    map.put("natte", "Natte");
    map.put("knave", "knight");
    map.put("knaves", "knights");
    map.put("kknaves", "kknights");

    List<WildcardMap.Entry<String>> names = map.getWithWildcards("*na*e*");

    assertTrue(
        names.contains(new WildcardMap.Entry<>("name", "Amy")), "Amy should be in the result set");
    assertTrue(
        names.contains(new WildcardMap.Entry<>("nate", "Nate")),
        "Nate should be in the result set");
    assertTrue(
        names.contains(new WildcardMap.Entry<>("natte", "Natte")),
        "Natte should be in the result set");
    assertTrue(
        names.contains(new WildcardMap.Entry<>("knave", "knight")),
        "knight should be in the result set");
    assertTrue(
        names.contains(new WildcardMap.Entry<>("knaves", "knights")),
        "knight should be in the result set");
    assertTrue(
        names.contains(new WildcardMap.Entry<>("kknaves", "kknights")),
        "knight should be in the result set");
  }

  @Test
  public void testRemove() {
    WildcardMap<String> map = new WildcardMap<>();
    map.put("name", "Amy");
    map.put("nate", "Nate");
    map.put("na*e", "Star");

    String name = map.remove("name");

    assertEquals("Amy", name, "Expected map to contain 'name'=>'Amy'");
    assertEquals(2, map.size(), "Expected size=2");

    assertTrue(map.containsKey("nate"), "Expected map to still contain 'nate'");
    assertTrue(map.containsKey("na*e"), "Expected map to still contain 'na*e'");
  }

  @Test
  public void testRemoveWithWildcards() {
    WildcardMap<String> map = new WildcardMap<>();
    map.put("name", "Amy");
    map.put("nate", "Nate");
    map.put("na*e", "Star");

    int numRemoved = map.removeWithWildcards("na*e");

    assertEquals(3, numRemoved, "Expected 3 entries to be removed");
    assertEquals(0, map.size(), "Expected map to be empty");
  }
}
