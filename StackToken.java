package edu.njit.cs314;

/**
 * Author: Ravi Varadarajan
 * Date created: 11/17/20
 */
public abstract class StackToken {

    public final String value;

    public StackToken(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof StackToken)) {
            return false;
        }
        StackToken other = (StackToken) obj;
        return other.value.equals(this.value);
    }

    @Override
    public String toString() {
        return "" + value;
    }

}
