package nl.jk5.pumpkin.server.scripting;

/**
 * Used to signal that the direct call limit for the current server tick has
 * been reached in {@link Machine#invoke(String, String, Object[])}.
 */
public class LimitReachedException extends Exception {
}
