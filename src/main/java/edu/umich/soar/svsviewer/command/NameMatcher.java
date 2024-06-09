package edu.umich.soar.svsviewer.command;

public record NameMatcher(String namePattern, NameMatchType matchType) {
  public enum NameMatchType {
    // wildcard name patterns may contain "*" to match arbitrary text
    EXACT,
    WILDCARD
  }
}
