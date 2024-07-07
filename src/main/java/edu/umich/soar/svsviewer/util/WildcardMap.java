package edu.umich.soar.svsviewer.util;

import java.util.*;

/**
 * Map with string keys that also supports wildcard searching; {@code *} matches 0 or more
 * characters. Actual keys are not allowed to contain {@code *}. Underlying implementation is a trie
 *
 * @param <T>
 */
public class WildcardMap<T> implements Map<String, T> {
  private Node<T> root = new Node<>();
  private int size = 0;

  private static class Node<T> {
    Map<Character, Node<T>> children = new HashMap<>();
    T value;
  }

  @Override
  public T put(String key, T value) {
    Node<T> currentNode = root;
    for (char c : key.toCharArray()) {
      currentNode.children.putIfAbsent(c, new Node<>());
      currentNode = currentNode.children.get(c);
    }
    T previousValue = currentNode.value;
    currentNode.value = value;
    size++;
    return previousValue;
  }

  @Override
  public T get(Object key) {
    if (!(key instanceof String keyString)) {
      throw new IllegalArgumentException("only String keys are allowed");
    }
    List<T> result = new ArrayList<>();
    search(false, root, 0, keyString, result);
    if (result.isEmpty()) {
      return null;
    }
    return result.get(0);
  }

  /**
   * Return all contained values with keys that match {@codfe keyWithWildcards}.
   *
   * @param keyWithWildcards matches keys of the desired values; {@code *} matches 0 or more
   *     characters
   */
  public List<T> getWithWildcards(String keyWithWildcards) {
    List<T> result = new ArrayList<>();
    search(true, root, 0, keyWithWildcards, result);
    return result;
  }

  private void search(boolean useWildcards, Node<T> parent, int index, String key, List<T> result) {
    if (parent == null) return;
    if (index == key.length()) {
      result.add(parent.value);
      return;
    }
    char c = key.charAt(index);
    if (useWildcards && c == '*') {
      // consume zero characters
      search(true, parent, index + 1, key, result);
      for (Node<T> childNode : parent.children.values()) {
        // consume one character
        search(true, childNode, index, key, result);
      }
    } else {
      // attempt to consume current character (no results if child for that character does not
      // exist)
      search(useWildcards, parent.children.get(c), index + 1, key, result);
    }
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public T remove(Object key) {
    throw new UnsupportedOperationException();
  }

  public int removeWithWildcards(String keyWithWildcards) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(Map<? extends String, ? extends T> m) {
    m.forEach(this::put);
  }

  @Override
  public void clear() {
    root = new Node<>();
  }

  @Override
  public Set<String> keySet() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<T> values() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Entry<String, T>> entrySet() {
    throw new UnsupportedOperationException();
  }
}
