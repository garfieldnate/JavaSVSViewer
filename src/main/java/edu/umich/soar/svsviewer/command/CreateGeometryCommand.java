package edu.umich.soar.svsviewer.command;

public record CreateGeometryCommand(NameMatcher sceneMatcher, String geometryName)
    implements Command {}
