package edu.umich.soar.svsviewer.command;

public record DeleteGeometryCommand(NameMatcher sceneMatcher, NameMatcher geometryMatcher)
    implements Command {}
