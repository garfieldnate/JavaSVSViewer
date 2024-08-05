package edu.umich.soar.svsviewer.util;

import java.util.*;
import java.util.stream.Collectors;

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

  public record Entry<T>(String key, T value) {}

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
    List<Entry<T>> result = new ArrayList<>();
    search(false, root, "", 0, keyString, result);
    if (result.isEmpty()) {
      return null;
    }
    return result.get(0).value();
  }

  /**
   * Return all contained key/value pairs where the keys match {@code keyWithWildcards}.
   *
   * @param keyWithWildcards matches keys of the desired values; {@code *} matches 0 or more
   *     characters
   */
  public List<Entry<T>> getWithWildcards(String keyWithWildcards) {
    List<Entry<T>> result = new ArrayList<>();
    search(true, root, "", 0, keyWithWildcards, result);
    return result;
  }

  private void search(
      boolean useWildcards,
      Node<T> parent,
      String currentPath,
      int index,
      String key,
      List<Entry<T>> results) {
    if (parent == null) return;
    if (index == key.length()) {
      results.add(new Entry<>(currentPath, parent.value));
      return;
    }
    char c = key.charAt(index);
    if (useWildcards && c == '*') {
      // consume zero characters
      search(true, parent, currentPath, index + 1, key, results);
      for (Map.Entry<Character, Node<T>> entry : parent.children.entrySet()) {
        // consume one character
        search(true, entry.getValue(), currentPath + entry.getKey(), index, key, results);
      }
    } else {
      // attempt to consume current character (no results if child for that character does not
      // exist)
      search(useWildcards, parent.children.get(c), currentPath + c, index + 1, key, results);
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
    if (!(key instanceof String keyString)) {
      throw new IllegalArgumentException("only String keys are allowed");
    }
    // keep track of the parent nodes that lead to the node with the key;
    // if they become empty after removing the node, they have to be removed
    LinkedList<Node<T>> path = new LinkedList<>();
    Node<T> currentNode = root;
    path.add(root); // Add root to path
    for (char c : keyString.toCharArray()) {
      currentNode = currentNode.children.get(c);
      if (currentNode == null) {
        return null; // Key does not exist
      }
      path.add(currentNode);
    }
    T previousValue = currentNode.value;
    if (previousValue != null) {
      currentNode.value = null;
      cleanUpPath(keyString, path);
      size--;
    }
    return previousValue;
  }

  /**
   * Remove nodes from the path that are not needed anymore (i.e. have no value and no children).
   */
  private void cleanUpPath(String key, LinkedList<Node<T>> path) {
    int index = key.length();
    while (index >= 0 && !path.isEmpty()) {
      Node<T> node = path.removeLast();
      if (node.value == null && node.children.isEmpty()) {
        if (index > 0) { // Not root
          Node<T> parent = path.peekLast();
          if (parent != null) {
            parent.children.remove(key.charAt(index - 1));
          }
        }
      } else {
        break; // Stop if node has value or children
      }
      index--;
    }
  }

  public Collection<T> removeWithWildcards(String keyWithWildcards) {
    List<Entry<T>> pairsToRemove = getWithWildcards(keyWithWildcards);
    for (Entry<T> entry : pairsToRemove) {
      remove(entry.key());
    }
    return pairsToRemove.stream().map(Entry::value).toList();
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
    return entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet());
  }

  @Override
  public Collection<T> values() {
    return entrySet().stream().map(Map.Entry::getValue).toList();
  }

  @Override
  public Set<Map.Entry<String, T>> entrySet() {
    Set<Map.Entry<String, T>> entries = new HashSet<>();
    collectEntries(root, "", entries);
    return entries;
  }

  private void collectEntries(Node<T> node, String currentKey, Set<Map.Entry<String, T>> entries) {
    if (node == null) {
      return;
    }
    if (node.value != null) {
      entries.add(new AbstractMap.SimpleEntry<>(currentKey, node.value));
    }
    for (Map.Entry<Character, Node<T>> child :
        node.children.entrySet()) { // Assuming 'children' is a map of child nodes
      char nextChar = child.getKey();
      Node<T> nextNode = child.getValue();
      collectEntries(nextNode, currentKey + nextChar, entries);
    }
  }
}
