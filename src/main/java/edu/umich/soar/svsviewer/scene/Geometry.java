package edu.umich.soar.svsviewer.scene;

import javafx.scene.Group;

// The contained group may have 0 or 1 children; the 1 child would be a Shape3d.
// We use a Group because we need to be able to specify position/rotation/scale
// without any particular shape yet specified.
public record Geometry(SVSScene parent, Group group) {}
