package edu.njit.cs314;

/**
 * Author: Ravi Varadarajan
 * Date created: 11/17/20
 */
public class StateToken extends StackToken {

     public static StackToken WILDCARD = new StateToken(1000);
     private final int state;

     public StateToken(int state) {
         super("" + state);
         this.state = state;
     }

     public int state() {
         return state;
     }

    public boolean equals(Object obj) {
        if (!(obj instanceof StateToken)) {
            return false;
        }
        StateToken other = (StateToken) obj;
        return this == WILDCARD || other == WILDCARD || this.state() == other.state();
    }

}
