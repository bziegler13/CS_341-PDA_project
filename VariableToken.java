package edu.njit.cs314;

/**
 * Author: Ravi Varadarajan
 * Date created: 4/29/20
 */
public class VariableToken extends GrammarToken {

    public VariableToken(String value) {
        super(value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VariableToken)) {
            return false;
        }
        VariableToken other = (VariableToken) obj;
        return other.value.equals(this.value);
    }


}
