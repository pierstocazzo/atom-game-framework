package sg.atom.managex.api.event;

import java.lang.reflect.*;
import java.util.*;

/**
 * An EventSource is any object that can dispatch events. It maintains a list of
 * "event links", which are methods to be called when specific types of events
 * occur. <p> EventSource is the superclass of Widget, and the vast majority of
 * EventSources are Widgets.
 *
 * @author Peter Eastman
 */
@Deprecated
public class EventSource {

    protected ArrayList eventLinks;

    /**
     * Create a new EventSource.
     */
    public EventSource() {
    }

    /**
     * Create an event link from this object. The target object must have a
     * method called processEvent(), which either takes no arguments, or takes
     * an object of class eventType (or any of its superclasses or interfaces)
     * as its only argument. When events of the desired class (or any of its
     * subclasses) are generated by this object, that method will be called.
     *
     * @param eventType the event class or interface which the target method
     * wants to receive
     * @param target the object to send the events to
     */
    public void addEventLink(Class eventType, Object target) {
        addEventLink(eventType, target, "processEvent");
    }

    /**
     * Create an event link from this object. When events of the desired class
     * (or any of its subclasses) are generated by this object, the specified
     * method will be called on the target object.
     *
     * @param eventType the event class or interface which the target method
     * wants to receive
     * @param target the object to send the events to
     * @param method the name of the method to invoke on the target object. The
     * method must either take no arguments, or take an object of class
     * eventType (or any of its superclasses or interfaces) as its only
     * argument.
     */
    public void addEventLink(Class eventType, Object target, String method) {
        Class cls = target.getClass();
        while (cls != null) {
            Method m[] = cls.getDeclaredMethods();
            for (int i = 0; i < m.length; i++) {
                if (m[i].getName().equals(method)) {
                    Class param[] = m[i].getParameterTypes();
                    if (param.length == 0 || (param.length == 1 && param[0].isAssignableFrom(eventType))) {
                        addEventLink(eventType, target, m[i]);
                        return;
                    }
                }
            }
            cls = cls.getSuperclass();
        }
        throw new IllegalArgumentException("No method found which matches " + method + "(" + eventType.getName() + ")");
    }

    /**
     * Create an event link from this object. When events of the desired class
     * (or any of its subclasses) are generated by this object, the specified
     * method will be called on the target object.
     *
     * @param eventType the event class or interface which the target method
     * wants to receive
     * @param target the object to send the events to
     * @param method the method to invoke on the target object. The method must
     * either take no arguments, or take an object of class eventType (or any of
     * its superclasses or interfaces) as its only argument.
     */
    public void addEventLink(Class eventType, Object target, Method method) {
        if (eventLinks == null) {
            eventLinks = new ArrayList();
        }
        if (!Modifier.isPublic(method.getDeclaringClass().getModifiers()) || !Modifier.isPublic(method.getModifiers())) {
            method.setAccessible(true);
        }
        for (int i = 0; i < eventLinks.size(); i++) {
            EventLinkRecord rec = (EventLinkRecord) eventLinks.get(i);
            if (rec.getEventType() == eventType) {
                rec.addLink(target, method);
                return;
            }
        }
        EventLinkRecord rec = new EventLinkRecord(eventType);
        rec.addLink(target, method);
        eventLinks.add(rec);
    }

    /**
     * Remove an event link so that an object will no longer be notified of
     * events of a particular type.
     *
     * @param eventType the event class or interface which should no longer be
     * sent
     * @param target the object which was receiving the events
     */
    public void removeEventLink(Class eventType, Object target) {
        if (eventLinks == null) {
            return;
        }
        for (int i = 0; i < eventLinks.size(); i++) {
            EventLinkRecord rec = (EventLinkRecord) eventLinks.get(i);
            if (rec.getEventType() == eventType) {
                rec.removeLink(target);
                return;
            }
        }
    }

    /**
     * Send out an object representing an event to every appropriate event link
     * that has been added to this object.
     */
    public void dispatchEvent(Object event) {
        if (eventLinks == null) {
            return;
        }
        for (int i = 0; i < eventLinks.size(); i++) {
            EventLinkRecord rec = (EventLinkRecord) eventLinks.get(i);
            if (rec.getEventType().isInstance(event)) {
                rec.dispatchEvent(event);
            }
        }
    }
}
