package edu.njit.cs314;

/**
 * Author: Ravi Varadarajan
 * Date created: 4/29/20
 */
public class TerminalToken extends GrammarToken {

    public static TerminalToken EPSILON = new TerminalToken() {};

    public TerminalToken(String value) {
        super(value);
    }

    public TerminalToken() {
        this("epsilon");
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TerminalToken)) {
            return false;
        }
        TerminalToken other = (TerminalToken) obj;
        return other.value.equals(this.value);
    }


}
