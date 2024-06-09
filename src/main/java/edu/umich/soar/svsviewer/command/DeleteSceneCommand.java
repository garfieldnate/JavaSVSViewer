package edu.umich.soar.svsviewer.command;

public record DeleteSceneCommand(NameMatcher sceneMatcher) implements Command {}
