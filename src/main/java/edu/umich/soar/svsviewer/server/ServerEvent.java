package edu.umich.soar.svsviewer.server;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.control.Dialog;

import java.io.Serial;

public class ServerEvent extends Event {

  /** Common supertype for all server event types. */
  public static final EventType<ServerEvent> ANY = new EventType<>(Event.ANY, "SERVER");

  public static final EventType<ServerEvent> CONNECTED =
      new EventType<>(ServerEvent.ANY, "CONNECTED");
  public static final EventType<ServerEvent> DISCONNECTED =
      new EventType<>(ServerEvent.ANY, "DISCONNECTED");
  @Serial private static final long serialVersionUID = 7845942106040255241L;

  /**
   * Construct a new {@code Event} with the specified event source, target and type. If the source
   * or target is set to {@code null}, it is replaced by the {@code NULL_SOURCE_TARGET} value.
   *
   * @param source the event source which sent the event
   * @param eventType the event type
   */
  public ServerEvent(
      final @NamedArg("source") Dialog<?> source,
      final @NamedArg("eventType") EventType<? extends Event> eventType) {
    super(source, source, eventType);
  }

  /**
   * Returns a string representation of this {@code ServerEvent} object.
   *
   * @return a string representation of this {@code ServerEvent} object.
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ServerEvent [");

    sb.append("source = ").append(getSource());
    sb.append(", target = ").append(getTarget());
    sb.append(", eventType = ").append(getEventType());
    sb.append(", consumed = ").append(isConsumed());

    return sb.append("]").toString();
  }

  @Override
  public ServerEvent copyFor(Object newSource, EventTarget newTarget) {
    return (ServerEvent) super.copyFor(newSource, newTarget);
  }

  @SuppressWarnings("unchecked")
  @Override
  public EventType<ServerEvent> getEventType() {
    return (EventType<ServerEvent>) super.getEventType();
  }
}
