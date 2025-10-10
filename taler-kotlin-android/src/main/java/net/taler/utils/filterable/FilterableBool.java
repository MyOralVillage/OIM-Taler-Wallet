package net.taler.utils.filterable;

/**
 * Filterable boolean enum for use in filtering systems.
 * Uses natural enum ordering: FALSE < TRUE
 */
public enum FilterableBool
    implements Filterable<FilterableBool> {

    // since enums fields have ordinal order,
    // the first field == 0, and the second field == 1
    FALSE,
    TRUE;

    /** @return {@code true} or {@code false} */
    public boolean getValue() {return this.ordinal() == 1;}
}
