package edu.umich.soar.svsviewer;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

import java.io.Serial;

// Structure copied from JavaFX's DialogEvent.

public class SVSViewerEvent extends Event {
  @Serial private static final long serialVersionUID = 7845942106040255241L;

  /** Common supertype for all server event types. */
  public static final EventType<SVSViewerEvent> ANY = new EventType<>(Event.ANY, "SVS_VIEWER");

  public static final EventType<SVSViewerEvent> SCENE_RERENDER_REQUESTED =
      new EventType<>(SVSViewerEvent.ANY, "SCENE_RERENDER_REQUESTED");

  /**
   * @see Event#Event(Object, EventTarget, EventType)
   */
  public SVSViewerEvent(
      final @NamedArg("source") EventTarget source,
      final @NamedArg("eventType") EventType<? extends Event> eventType) {
    super(source, source, eventType);
  }

  /**
   * Returns a string representation of this {@code SVSViewerEvent} object.
   *
   * @return a string representation of this {@code SVSViewerEvent} object.
   */
  @Override
  public String toString() {
    return "SVSViewerEvent ["
        + "source = "
        + getSource()
        + ", target = "
        + getTarget()
        + ", eventType = "
        + getEventType()
        + ", consumed = "
        + isConsumed()
        + "]";
  }

  @Override
  public SVSViewerEvent copyFor(Object newSource, EventTarget newTarget) {
    return (SVSViewerEvent) super.copyFor(newSource, newTarget);
  }

  @SuppressWarnings("unchecked")
  @Override
  public EventType<SVSViewerEvent> getEventType() {
    return (EventType<SVSViewerEvent>) super.getEventType();
  }
}
