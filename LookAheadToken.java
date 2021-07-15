package edu.njit.cs314;

/**
 * Author: Ravi Varadarajan
 * Date created: 11/17/20
 */
public class LookAheadToken extends StackToken {

    public LookAheadToken(TerminalToken tok) {
        super("L"+tok.value);
    }
}
