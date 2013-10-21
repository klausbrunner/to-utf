package net.e175.tools;

/**
 * Interface for a (possibly stateful) character filter.
 * 
 * @author Klaus Brunner
 */
public interface CharFilter {

    /**
     * Filter an incoming character
     * 
     * @param character
     *            incoming character. If -1, it means that this is the last call
     *            to this filter instance, so any buffered characters should now
     *            be released.
     * @return An array containing zero, one or more characters as a result of
     *         the filter operation.
     */
    String filter(final int character);

    /**
     * Reset this filter instance, dropping all state information.
     */
    void reset();

}
