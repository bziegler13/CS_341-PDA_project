package edu.njit.cs314;

import java.util.*;

/**
 * Author: Ravi Varadarajan
 * Date created: 11/18/20
 */
public class DPDA {

    private static class Rule {
        public final VariableToken lhs;
        public final List<GrammarToken> rhs;

        public Rule(VariableToken lhs, List<GrammarToken> rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }
    }

    private static class Transition {
        public final int currState;
        public final TerminalToken inputSymbol;
        public final List<StackToken> stackTop; // first symbol on top of the stack
        public final int nextState;
        public final List<StackToken> stackTopReplacement; // first symbol on top of stack
        public final List<Rule> rulesForReduction;

        public Transition(int currState, TerminalToken inputSymbol, List<StackToken> stackTop,
                          int nextState, List<StackToken> stackTopReplacement,
                          List<Rule> ruleForReduction) {
            this.currState = currState;
            this.inputSymbol = inputSymbol;
            this.stackTop = stackTop;
            this.nextState = nextState;
            this.stackTopReplacement = stackTopReplacement;
            this.rulesForReduction = ruleForReduction;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            builder.append(currState);
            builder.append(",");
            builder.append(inputSymbol);
            builder.append(",");
            if (stackTop.isEmpty()) {
                builder.append("epsilon");
            } else {
                for (StackToken token : stackTop) {
                    builder.append(token);
                }
            }
            builder.append(")");
            builder.append("->");
            builder.append("(");
            builder.append(nextState);
            builder.append(",");
            if (stackTopReplacement.isEmpty()) {
                builder.append("epsilon");
            } else {
                for (StackToken token : stackTopReplacement) {
                    builder.append(token);
                }
            }
            builder.append(")");
            return builder.toString();
        }

    }

    private class Configuration {
        public final int currState;
        public final List<StackToken> fromStackState;
        public List<TerminalToken> remainingInput;
        public final Transition transition;

        public Configuration(int currState, List<TerminalToken> remainingInput,
                             List<StackToken> fromStackState,
                             Transition transition) {
            this.currState = currState;
            this.remainingInput = remainingInput;
            this.fromStackState = fromStackState;
            this.transition = transition;
        }

        public boolean isAccepting() {
            if (acceptStates.contains(transition.nextState)) {
                return true;
            } else {
                return false;
            }
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            builder.append(currState);
            builder.append(",");
            if (remainingInput.isEmpty()) {
                builder.append("epsilon");
            } else {
                for (TerminalToken token : remainingInput) {
                    builder.append(token);
                }
            }
            builder.append(",");
            if (fromStackState.isEmpty()) {
                builder.append("epsilon");
            } else {
                for (StackToken token : fromStackState) {
                    builder.append(token);
                }
            }
            builder.append(")");
            return builder.toString();
        }
    }

    private final Set<TerminalToken> terminalTokens = new HashSet<>();
    private final Set<VariableToken> variableTokens = new HashSet<>();
    private Stack<StackToken> stack = new Stack<>();
    public static List<StackToken> EPSILON_STACK = new ArrayList<>();


    private final int nStates;
    private final int startState;
    private final Set<Integer> acceptStates;
    private Map<Integer,List<Transition>> transitionMap = new HashMap<>();

    public DPDA(int nStates, int startState,
                    Set<String> terminals,
                    Set<String> variables,
                    Set<Integer> acceptStates) {
        this.nStates = nStates;
        this.acceptStates = acceptStates;
        this.startState = startState;
        for (String terminal : terminals) {
            terminalTokens.add(new TerminalToken(terminal));
        }
        for (String variable : variables) {
            variableTokens.add(new VariableToken(variable));
        }
    }

    /**
     * Matches two stack tops given as lists (First symbol in the list is top of stack)
     * @param stackTop1
     * @param stackTop2
     * @return true if one list is a sublist of another that starts from the beginning
     */
    private boolean matchStackTop(List<StackToken> stackTop1,
                                  List<StackToken> stackTop2) {
        /** TO DO **/
        Iterator<StackToken> iter1 = stackTop1.iterator();
        Iterator<StackToken> iter2 = stackTop2.iterator();
        List<StackToken> matchedStackTop = new ArrayList<>();
        while (iter1.hasNext() && iter2.hasNext()) {
            StackToken token = iter2.next();
            if (!iter1.next().equals(token)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds transition to pda but verifies DPDA properties
     * @param currState
     * @param inputSymbol
     * @param stackTop
     * @param nextState
     * @param stackTopReplacement
     * @param ruleForReduction
     */
    public void addTransition(int currState, TerminalToken inputSymbol, List<StackToken> stackTop,
                              int nextState, List<StackToken> stackTopReplacement,
                              List<Rule> ruleForReduction)
            throws InvalidStateException, InvalidSymbolException {
        // Check if all tokens and states in the inputs to the function are valid, otherwise
        // throw exception
        if (currState < 0 || currState >= nStates) {
            throw new InvalidStateException("State " + currState + " is not valid");
        }
        if (nextState < 0 || nextState >= nStates) {
            throw new InvalidStateException("State " + nextState + " is not valid");
        }
        if (inputSymbol != TerminalToken.EPSILON && !terminalTokens.contains(inputSymbol)) {
            throw new InvalidSymbolException("Symbol " + inputSymbol + " is not valid");
        }
        for (StackToken token : stackTop) {
            if (token instanceof TerminalToken) {
                if (!terminalTokens.contains(token)) {
                    throw new InvalidSymbolException("Input symbol " + token + " is not valid");
                }
            } else if (token instanceof  VariableToken) {
                if (!variableTokens.contains(token)) {
                    throw new InvalidSymbolException("Variable symbol " + token + " is not valid");
                }
            } else if (token instanceof StateToken) {
                StateToken stateToken = (StateToken) token;
                if (stateToken != StateToken.WILDCARD
                        && (stateToken.state() < 0 || stateToken.state() > nStates)) {
                    throw new InvalidSymbolException("State" + token + " is not valid");
                }
            }
        }
        for (StackToken token : stackTopReplacement) {
            if (token instanceof TerminalToken) {
                if (!terminalTokens.contains((TerminalToken) token)) {
                    throw new InvalidSymbolException("Symbol " + token + " is not valid");
                }
            } else if (token instanceof  VariableToken) {
                if (!variableTokens.contains(token)) {
                    throw new InvalidSymbolException("Symbol " + token + " is not valid");
                }
            }
        }
        // verify if it satisfies properties of DPDA, else throw exception
        List<Transition> transitions = transitionMap.get(currState);
        if (transitions == null) {
            transitions = new ArrayList<Transition>();
            transitionMap.put(currState, transitions);
        }
        for (Transition transition : transitions) {
            if (!transition.stackTop.equals(EPSILON_STACK) && !stackTop.equals(EPSILON_STACK) &&
                    matchStackTop(transition.stackTop, stackTop)) {
                // epsilon stack transition
                if (transition.inputSymbol == TerminalToken.EPSILON) {
                    throw new IllegalArgumentException("Epsilon input/epsilon stack transition already exists for state "
                            + currState + ":" + transition);
                }
                if (inputSymbol == TerminalToken.EPSILON) {
                    throw new IllegalArgumentException("Cannot add epsilon input/epsilon transition for state "
                            + currState + " when transition for " + transition.inputSymbol
                            + " already exists: " + transition);
                }
                if (transition.inputSymbol.equals(inputSymbol)) {
                    throw new IllegalArgumentException("Cannot add epsilon stack transition for state "
                            + currState + " when epsilon transition for " + transition.inputSymbol
                            + " already exists: " + transition);
                }
            }
        }
        transitions.add(new Transition(currState, inputSymbol, stackTop, nextState,
                stackTopReplacement, ruleForReduction));
    }

    private List<StackToken> copyStack() {
        List<StackToken> stackTop = new ArrayList<>();
        Iterator<StackToken> iter = stack.iterator();
        while (iter.hasNext()) {
            stackTop.add(0,iter.next());
        }
        return stackTop;
    }

    /**
     * Attempts to find a matching transition from current state, input symbol
     * which may be epsilon and a boolean to indicate if epsilon for  stack needs to be matched
     * in transition
     * @param currState
     * @param inputSymbol
     * @param epsilonStack
     * @return
     */
    private Transition match(int currState, TerminalToken inputSymbol, boolean epsilonStack) {
        List<Transition> transitions = transitionMap.get(currState);
        List<StackToken> stackTop = copyStack();
        for (Transition transition : transitions) {
             /** TODO **/
            if (transition.inputSymbol.equals(inputSymbol)) {
                if (epsilonStack) {
                    if (transition.stackTop.isEmpty()) {
                        return transition;
                    }
                } else if (matchStackTop(stackTop,transition.stackTop)) {
                    return transition;
                }
            }
        }
        return null;
    }

    /**
     * Uses the transition to pop from stack matching symbols from transition.stackTop
     * and then pushes the symbols stackTopReplacement on to stack
     * @param transition
     */
    private void actOnStack(Transition transition) {
        /** TODO **/
        if (transition != null) {
            List<StackToken> stackTop = transition.stackTop;
            // pop
            for (StackToken token : stackTop) {
                StackToken stackToken = stack.peek();
                if (stackToken == null || !stackToken.equals(token)) {
                    return;
                }
                stack.pop();
            }
            // push
            for (int i=transition.stackTopReplacement.size()-1; i >= 0; i--) {
                stack.push(transition.stackTopReplacement.get(i));
            }
        }
    }

    public List<Configuration> process(List<TerminalToken> input) {
        stack.clear();
        int i = 0;
        int currState = startState;
        Transition transition =  null;
        List<Configuration> configurations = new ArrayList<> ();
        TerminalToken inputToken = null;
        while (i < input.size() || stack.size() > 1) {
            // check for epsilon stack/ epsilon input transition
            inputToken = TerminalToken.EPSILON;
            transition = match(currState, inputToken, true);
            if (transition == null) {
                // check for epsilon stack / input symbol transition
                if (i < input.size()) {
                    inputToken = input.get(i);
                    transition = match(currState, inputToken, true);
                } else {
                    transition = null;
                }
                if (transition == null) {
                    // match stack with epsilon input
                    inputToken = TerminalToken.EPSILON;
                    transition = match(currState, inputToken, false);
                    if (transition == null) {
                        if (i < input.size()) {
                            inputToken = input.get(i);
                            transition = match(currState, inputToken, false);
                        } else{
                            transition = null;
                        }
                        if (transition == null) {
                            // reject input
                            return configurations;
                        }
                    }
                }
            }
            List<TerminalToken> sl = Arrays.asList(inputToken);
            configurations.add(new Configuration(currState, sl,copyStack(), transition));
            if (inputToken != TerminalToken.EPSILON) {
                i++;
            }
            actOnStack(transition);
            currState = transition.nextState;
        }
        if (configurations.get(configurations.size()-1).isAccepting()) {
            configurations.add(new Configuration(currState, Arrays.asList(TerminalToken.EPSILON),
                    copyStack(), transition));
        }
        return configurations;
    }

    public static TerminalToken toTerminal(String val) {
        return new TerminalToken(val);
    }

    public static  VariableToken toVariable(String val) {
        return new VariableToken(val);
    }

    public static StateToken toState(int val) {
        return new StateToken(val);
    }

    public static List<TerminalToken> toTerminalTokens(String [] vals) {
        List<TerminalToken> lst = new ArrayList<>();
        for (String val : vals) {
            lst.add(toTerminal(val));
        }
        return lst;
    }

    public static void printConfigs(List<Configuration> configs) {
        boolean firstConfig = true;
        int i = 0;
        for (Configuration config : configs) {
            if (i++ % 4 == 1) {
                System.out.println("");
            }
            if (!firstConfig) {
                System.out.print("->");
            }
            System.out.print(config);
            firstConfig = false;
        }
        System.out.println("");
    }

    private boolean acceptString(List<Configuration> configs) {
        if (!configs.isEmpty() && configs.get(configs.size()-1).isAccepting()) {
            return true;
        } else {
            return false;
        }
    }

    private static List<GrammarToken> getRightMostReplacement(List<GrammarToken> derivation,
                                                              Rule rule) {
        int k=derivation.size()-1;
        List<GrammarToken> newDerivation = new ArrayList<>();
        while (k >= 0) {
            if (!derivation.get(k).equals(rule.lhs)) {
                newDerivation.add(0,derivation.get(k));
                k--;
            } else {
                break;
            }
        }
        if (k >= 0) {
            for (int m=rule.rhs.size()-1; m >= 0; m--) {
                newDerivation.add(0,rule.rhs.get(m));
            }
            for (int p=k-1; p >=0; p--) {
                newDerivation.add(0,derivation.get(p));
            }
        }
        return newDerivation;
    }

    private List<List<GrammarToken>> getDerivations(List<Configuration> configs) {
        List<List<GrammarToken>> derivations = new ArrayList<>();
        boolean addStart = true;
        List<GrammarToken> lastDerivation = null;
        for (int i=configs.size()-2; i >= 0; i--) {
            Configuration config = configs.get(i);
            List<Rule> rules = config.transition.rulesForReduction;
            for (int j= rules.size()-1; j >= 0; j--) {
                Rule rule = rules.get(j);
                if (addStart) {
                    derivations.add(Arrays.asList(rule.lhs));
                    lastDerivation = rule.rhs;
                    derivations.add(lastDerivation);
                } else {
                    // do right most replacement
                    derivations.add(getRightMostReplacement(lastDerivation,rule));
                    lastDerivation = derivations.get(derivations.size()-1);
                }
                addStart = false;
            }
        }
        return derivations;
    }

    public void printDerivations(List<Configuration> configs) {
        if (!this.acceptString(configs)) {
            return;
        }
        List<List<GrammarToken>> derivations = getDerivations(configs);
        boolean first = true;
        for (List<GrammarToken> derivation : derivations) {
            if (!first) {
                System.out.print("-->");
            }
            for (GrammarToken token : derivation) {
                System.out.print(token);
            }
            first = false;
        }
        System.out.println("");
    }

    public static void test1() throws Exception {
        Set<String> terminals = new HashSet<>(Arrays.asList("a","b", "$"));
        Set<String> variables = new HashSet<>(Arrays.asList("A","B"));
        DPDA pda = new DPDA(6,0,terminals,variables,new HashSet<>(Arrays.asList(5)));
        TerminalToken aToken = toTerminal("a");
        TerminalToken bToken = toTerminal("b");
        TerminalToken endToken = toTerminal("$");
        pda.addTransition(0,TerminalToken.EPSILON, EPSILON_STACK,1,
                Arrays.asList(endToken),null);
        pda.addTransition(1, aToken, Arrays.asList(endToken), 2, Arrays.asList(endToken),null);
        pda.addTransition(1, bToken, Arrays.asList(endToken), 3, Arrays.asList(endToken),null);
        pda.addTransition(2, TerminalToken.EPSILON, EPSILON_STACK, 4, Arrays.asList(aToken),null);
        pda.addTransition(3, TerminalToken.EPSILON, EPSILON_STACK, 4, Arrays.asList(bToken),null);
        pda.addTransition(4, aToken, Arrays.asList(endToken), 2, Arrays.asList(endToken),null);
        pda.addTransition(4, aToken, Arrays.asList(aToken), 2, Arrays.asList(aToken),null);
        pda.addTransition(4, bToken, Arrays.asList(endToken), 3, Arrays.asList(endToken),null);
        pda.addTransition(4, bToken, Arrays.asList(bToken), 3, Arrays.asList(bToken),null);
        pda.addTransition(4, bToken, Arrays.asList(aToken), 4, EPSILON_STACK,null);
        pda.addTransition(4, aToken, Arrays.asList(bToken), 4, EPSILON_STACK,null);
        pda.addTransition(4, endToken, Arrays.asList(endToken), 5, EPSILON_STACK,null);
        List<Configuration> configurations = pda.process(toTerminalTokens(new String []
                {"a","a", "b","b","b", "a", "a", "b", "$"}));
        printConfigs(configurations);
        System.out.println("Accept string " + "aabbbaab$ ? " + pda.acceptString(configurations));
        configurations = pda.process(toTerminalTokens(new String []
                {"b","a", "b","b","b", "a", "a", "b", "$"}));
        printConfigs(configurations);
        System.out.println("Accept string " + "babbbaab$ ? " + pda.acceptString(configurations));
    }

    public static void test2() throws Exception {
        Set<String> terminals = new HashSet<>(Arrays.asList("n", "+", "*", "(", ")", "$"));
        Set<String> variables = new HashSet<>(Arrays.asList("S","E", "T", "F"));
        DPDA pda = new DPDA(14, 0, terminals, variables, new HashSet<>(Arrays.asList(5)));
        TerminalToken nToken = toTerminal("n");
        TerminalToken endToken = toTerminal("$");
        TerminalToken plusToken = toTerminal("+");
        TerminalToken multToken = toTerminal("*");
        TerminalToken openParToken = toTerminal("(");
        TerminalToken closedParToken = toTerminal(")");

        VariableToken sToken = toVariable("S");
        VariableToken eToken = toVariable("E");
        VariableToken tToken = toVariable("T");
        VariableToken fToken = toVariable("F");
        StackToken laClosedParToken = new LookAheadToken(closedParToken);
        StackToken laEndToken = new LookAheadToken(endToken);
        StackToken laPlusToken = new LookAheadToken(plusToken);

        // Rules for grammer
        Rule rule1 = new Rule(sToken, Arrays.asList(eToken, endToken));
        Rule rule2 = new Rule(eToken, Arrays.asList(tToken, plusToken, eToken));
        Rule rule3 = new Rule(eToken, Arrays.asList(tToken));
        Rule rule4 = new Rule(tToken, Arrays.asList(fToken, multToken, tToken));
        Rule rule5 = new Rule(tToken, Arrays.asList(fToken));
        Rule rule6 = new Rule(fToken, Arrays.asList(openParToken,eToken,closedParToken));
        Rule rule7 = new Rule(fToken, Arrays.asList(nToken));

        List<Rule> emptyRules = new ArrayList<>();
        pda.addTransition(0, TerminalToken.EPSILON, EPSILON_STACK,1,
                Arrays.asList(toState(1)),emptyRules);
        pda.addTransition(1, nToken, EPSILON_STACK,2,
                Arrays.asList(toState(2), nToken),emptyRules);
        pda.addTransition(1, openParToken, EPSILON_STACK,3,
                Arrays.asList(toState(3), openParToken),emptyRules);
        int [] prevStatesForVariable = new int [] {1,3,10,11};
        int [] nextStatesForVariable = new int [] {8,8,8,8};
        for (int i=0; i < prevStatesForVariable.length; i++) {
            int prevState = prevStatesForVariable[i];
            int nextState = nextStatesForVariable[i];
            pda.addTransition(2, TerminalToken.EPSILON,
                    Arrays.asList(toState(2), nToken, toState(prevState)), nextState,
                    Arrays.asList(toState(nextState),fToken,toState(prevState)), Arrays.asList(rule7));
        }
        pda.addTransition(3, nToken, EPSILON_STACK,2,
                Arrays.asList(toState(2), nToken),emptyRules);
        pda.addTransition(3, openParToken, EPSILON_STACK,3,
                Arrays.asList(toState(3), openParToken),emptyRules);
        pda.addTransition(4, endToken, Arrays.asList(toState(4)),5,
                Arrays.asList(toState(5), endToken), emptyRules);
        pda.addTransition(4, TerminalToken.EPSILON, Arrays.asList(laEndToken),5,
                Arrays.asList(toState(5), endToken), emptyRules);
        pda.addTransition(5, TerminalToken.EPSILON,
                Arrays.asList(toState(5), endToken, StateToken.WILDCARD, eToken, toState(1)),5,
                Arrays.asList(sToken), Arrays.asList(rule1));
        prevStatesForVariable = new int [] {1,3,10};
        nextStatesForVariable = new int [] {4,7,13};
        for (int i=0; i < prevStatesForVariable.length; i++) {
            int prevState = prevStatesForVariable[i];
            int nextState = nextStatesForVariable[i];
            pda.addTransition(6, closedParToken,
                    Arrays.asList(toState(6), tToken, toState(prevState)), nextState,
                    Arrays.asList(laClosedParToken, toState(nextState), eToken,toState(prevState)),
                    Arrays.asList(rule3));
            pda.addTransition(6, TerminalToken.EPSILON,
                    Arrays.asList(laClosedParToken,toState(6), tToken, toState(prevState)), nextState,
                    Arrays.asList(laClosedParToken, toState(nextState), eToken,toState(prevState)),
                    Arrays.asList(rule3));
            pda.addTransition(6, endToken,
                    Arrays.asList(toState(6), tToken, toState(prevState)), nextState,
                    Arrays.asList(laEndToken, toState(nextState), eToken,toState(prevState)),
                    Arrays.asList(rule3));
            pda.addTransition(6, TerminalToken.EPSILON,
                    Arrays.asList(laEndToken, toState(6), tToken, toState(prevState)), nextState,
                    Arrays.asList(laEndToken, toState(nextState), eToken,toState(prevState)),
                    Arrays.asList(rule3));
        }
        pda.addTransition(6, plusToken, EPSILON_STACK,10,
                Arrays.asList(toState(10), plusToken), emptyRules);
        pda.addTransition(6, TerminalToken.EPSILON, Arrays.asList(laPlusToken),10,
                Arrays.asList(toState(10), plusToken), emptyRules);
        pda.addTransition(7, closedParToken, Arrays.asList(toState(7)),9,
                Arrays.asList(toState(9), closedParToken), emptyRules);
        pda.addTransition(7, TerminalToken.EPSILON, Arrays.asList(laClosedParToken),9,
                Arrays.asList(toState(9), closedParToken), emptyRules);
        pda.addTransition(8, multToken, EPSILON_STACK,11,
                Arrays.asList(toState(11), multToken), emptyRules);
        prevStatesForVariable = new int [] {1,3,10,11};
        nextStatesForVariable = new int [] {6,6,6,12};
        for (int i=0; i < prevStatesForVariable.length; i++) {
            int prevState = prevStatesForVariable[i];
            int nextState = nextStatesForVariable[i];
            pda.addTransition(8, plusToken,
                    Arrays.asList(toState(8), fToken, toState(prevState)), nextState,
                    Arrays.asList(laPlusToken, toState(nextState), tToken,toState(prevState)),
                    Arrays.asList(rule5));
            pda.addTransition(8, closedParToken,
                    Arrays.asList(toState(8), fToken, toState(prevState)), nextState,
                    Arrays.asList(laClosedParToken, toState(nextState), tToken,toState(prevState)),
                    Arrays.asList(rule5));
            pda.addTransition(8, endToken,
                    Arrays.asList(toState(8), fToken, toState(prevState)), nextState,
                    Arrays.asList(laEndToken, toState(nextState), tToken,toState(prevState)),
                    Arrays.asList(rule5));
            pda.addTransition(8, TerminalToken.EPSILON,
                    Arrays.asList(laPlusToken, toState(8), fToken, toState(prevState)), nextState,
                    Arrays.asList(laPlusToken, toState(nextState), tToken,toState(prevState)),
                    Arrays.asList(rule5));
            pda.addTransition(8, TerminalToken.EPSILON,
                    Arrays.asList(laClosedParToken, toState(8), fToken, toState(prevState)), nextState,
                    Arrays.asList(laClosedParToken, toState(nextState), tToken,toState(prevState)),
                    Arrays.asList(rule5));
            pda.addTransition(8, TerminalToken.EPSILON,
                    Arrays.asList(laEndToken,toState(8), fToken, toState(prevState)), nextState,
                    Arrays.asList(laEndToken, toState(nextState), tToken,toState(prevState)),
                    Arrays.asList(rule5));
        }
        prevStatesForVariable = new int [] {1,3,10,11};
        nextStatesForVariable = new int [] {8,8,8,8};
        for (int i=0; i < prevStatesForVariable.length; i++) {
            int prevState = prevStatesForVariable[i];
            int nextState = nextStatesForVariable[i];
            pda.addTransition(9, TerminalToken.EPSILON,
                    Arrays.asList(toState(9), closedParToken,
                            StateToken.WILDCARD, eToken,
                            StateToken.WILDCARD, openParToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(toState(nextState),fToken,toState(prevState)), Arrays.asList(rule6));
            pda.addTransition(9, TerminalToken.EPSILON,
                    Arrays.asList(laClosedParToken, toState(9), closedParToken,
                            StateToken.WILDCARD, eToken,
                            StateToken.WILDCARD, openParToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(laClosedParToken, toState(nextState),fToken,toState(prevState)), Arrays.asList(rule6));
        }
        pda.addTransition(10, nToken, EPSILON_STACK,2,
                Arrays.asList(toState(2), nToken), emptyRules);
        pda.addTransition(10, openParToken, EPSILON_STACK,3,
                Arrays.asList(toState(3), openParToken), emptyRules);
        pda.addTransition(11, nToken, EPSILON_STACK,2,
                Arrays.asList(toState(2), nToken), emptyRules);
        pda.addTransition(11, openParToken, EPSILON_STACK,3,
                Arrays.asList(toState(3), openParToken), emptyRules);
        prevStatesForVariable = new int [] {1,3,10,11};
        nextStatesForVariable = new int [] {6,6,6,12};
        for (int i=0; i < prevStatesForVariable.length; i++) {
            int prevState = prevStatesForVariable[i];
            int nextState = nextStatesForVariable[i];
            pda.addTransition(12, TerminalToken.EPSILON,
                    Arrays.asList(toState(12), tToken,
                            StateToken.WILDCARD, multToken,
                            StateToken.WILDCARD, fToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(toState(nextState),tToken,toState(prevState)), Arrays.asList(rule4));
            pda.addTransition(12, TerminalToken.EPSILON,
                    Arrays.asList(laClosedParToken, toState(12), tToken,
                            StateToken.WILDCARD, multToken,
                            StateToken.WILDCARD, fToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(laClosedParToken,toState(nextState),tToken,toState(prevState)), Arrays.asList(rule4));
            pda.addTransition(12, TerminalToken.EPSILON,
                    Arrays.asList(laEndToken, toState(12), tToken,
                            StateToken.WILDCARD, multToken,
                            StateToken.WILDCARD, fToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(laEndToken, toState(nextState),tToken,toState(prevState)), Arrays.asList(rule4));
            pda.addTransition(12, TerminalToken.EPSILON,
                    Arrays.asList(laPlusToken, toState(12), tToken,
                            StateToken.WILDCARD, multToken,
                            StateToken.WILDCARD, fToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(laPlusToken, toState(nextState),tToken,toState(prevState)), Arrays.asList(rule4));
        }
        prevStatesForVariable = new int [] {1,3,10};
        nextStatesForVariable = new int [] {4,7,13};
        for (int i=0; i < prevStatesForVariable.length; i++) {
            int prevState = prevStatesForVariable[i];
            int nextState = nextStatesForVariable[i];
            pda.addTransition(13, TerminalToken.EPSILON,
                    Arrays.asList(toState(13), eToken,
                            StateToken.WILDCARD, plusToken,
                            StateToken.WILDCARD, tToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(toState(nextState),eToken,toState(prevState)), Arrays.asList(rule2));
            pda.addTransition(13, TerminalToken.EPSILON,
                    Arrays.asList(laClosedParToken, toState(13), eToken,
                            StateToken.WILDCARD, plusToken,
                            StateToken.WILDCARD, tToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(laClosedParToken, toState(nextState),eToken,toState(prevState)), Arrays.asList(rule2));
            pda.addTransition(13, TerminalToken.EPSILON,
                    Arrays.asList(laEndToken, toState(13), eToken,
                            StateToken.WILDCARD, plusToken,
                            StateToken.WILDCARD, tToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(laEndToken, toState(nextState),eToken,toState(prevState)), Arrays.asList(rule2));
        }
        List<Configuration> configurations = pda.process(toTerminalTokens(new String []
                {"n", "*", "n", "+", "(", "n", "*", "n",  ")", "$"}));
        printConfigs(configurations);
        pda.printDerivations(configurations);
        System.out.println("Accept string " + "n*n+(n*n)$? " + pda.acceptString(configurations));
        configurations = pda.process(toTerminalTokens(new String []
                {"n", "*", "n", "(", "n", "*", "n",  ")", "$"}));
        printConfigs(configurations);
        pda.printDerivations(configurations);
        System.out.println("Accept string " + "n*n(n*n)$? " + pda.acceptString(configurations));
        configurations = pda.process(toTerminalTokens(new String []
                {"(", "n", "*", "(", "n", "+", "n", ")", ")", "$"}));
        printConfigs(configurations);
        pda.printDerivations(configurations);
        System.out.println("Accept string " + "(n*(n+n))$? " + pda.acceptString(configurations));
    }

    public static void test3() throws Exception {
        Set<String> terminals = new HashSet<>(Arrays.asList("y","z","+","-","*","/","(",")","=",";","$"));
        Set<String> variables = new HashSet<>(Arrays.asList("S","B","T","T1","T2","F1","F2","X","C","N","I"));
        DPDA pda = new DPDA(34, 0, terminals, variables, new HashSet<>(Arrays.asList(3)));
        TerminalToken yToken = toTerminal("y");
        TerminalToken zToken = toTerminal("z");
        TerminalToken plusToken = toTerminal("+");
        TerminalToken minusToken = toTerminal("-");
        TerminalToken multToken = toTerminal("*");
        TerminalToken divToken = toTerminal("/");
        TerminalToken openParToken = toTerminal("(");
        TerminalToken closedParToken = toTerminal(")");
        TerminalToken eqToken = toTerminal("=");
        TerminalToken scToken = toTerminal(";");
        TerminalToken endToken = toTerminal("$");

        VariableToken sToken = toVariable("S");
        VariableToken bToken = toVariable("B");
        VariableToken tToken = toVariable("T");
        VariableToken t1Token = toVariable("T1");
        VariableToken t2Token = toVariable("T2");
        VariableToken f1Token = toVariable("F1");
        VariableToken f2Token = toVariable("F2");
        VariableToken xToken = toVariable("X");
        VariableToken cToken = toVariable("C");
        VariableToken nToken = toVariable("N");
        VariableToken iToken = toVariable("I");

        StackToken laEndToken = new LookAheadToken(endToken);
        StackToken laScToken = new LookAheadToken(scToken);
        StackToken laPlusToken = new LookAheadToken(plusToken);
        StackToken laClosedParToken = new LookAheadToken(closedParToken);
        StackToken laMinusToken = new LookAheadToken(minusToken);
        StackToken laMultToken = new LookAheadToken(multToken);
        StackToken laDivToken = new LookAheadToken(divToken);
        StackToken laIToken = new LookAheadToken(iToken);
        StackToken laXToken = new LookAheadToken(xToken);
        StackToken laEqToken = new LookAheadToken(eqToken);

        // Rules for grammer
        Rule rule1 = new Rule(sToken, Arrays.asList(bToken, endToken));
        Rule rule2 = new Rule(bToken, Arrays.asList(tToken));
        Rule rule3 = new Rule(bToken, Arrays.asList(xToken, eqToken, tToken, scToken, bToken));
        Rule rule4 = new Rule(tToken, Arrays.asList(tToken, plusToken, t1Token));
        Rule rule5 = new Rule(tToken, Arrays.asList(t1Token));
        Rule rule6 = new Rule(t1Token, Arrays.asList(t1Token,minusToken,t2Token))
        Rule rule7 = new Rule(t1Token, Arrays.asList(t2Token));
        Rule rule8 = new Rule(t2Token, Arrays.asList(t2Token,multToken,f1Token));
        Rule rule9 = new Rule(t2Token, Arrays.asList(f1Token));
        Rule rule10 = new Rule(f1Token, Arrays.asList(f1Token,divToken,f2Token));
        Rule rule11 = new Rule(f1Token, Arrays.asList(f2Token));
        Rule rule12 = new Rule(f2Token, Arrays.asList(iToken));
        Rule rule13 = new Rule(f2Token, Arrays.asList(xToken));
        Rule rule14 = new Rule(f2Token, Arrays.asList(openParToken,tToken,closedParToken));
        Rule rule15 = new Rule(iToken, Arrays.asList(nToken,iToken));
        Rule rule16 = new Rule(iToken, Arrays.asList(nToken));
        Rule rule17 = new Rule(nToken, Arrays.asList(zToken));
        Rule rule18 = new Rule(xToken, Arrays.asList(cToken,xToken));
        Rule rule19 = new Rule(xToken, Arrays.asList(cToken));
        Rule rule20 = new Rule(cToken, Arrays.asList(yToken));

        List<Rule> emptyRules = new ArrayList<>();
        pda.addTransition(0, TerminalToken.EPSILON, EPSILON_STACK,1,
                Arrays.asList(toState(1)),emptyRules);
        pda.addTransition(1, nToken, EPSILON_STACK,2,
                Arrays.asList(toState(2), nToken),emptyRules);
        pda.addTransition(1, openParToken, EPSILON_STACK,3,
                Arrays.asList(toState(3), openParToken),emptyRules);
        int [] prevStatesForVariable = new int [] {1,3,10,11};
        int [] nextStatesForVariable = new int [] {8,8,8,8};
        for (int i=0; i < prevStatesForVariable.length; i++) {
            int prevState = prevStatesForVariable[i];
            int nextState = nextStatesForVariable[i];
            pda.addTransition(2, TerminalToken.EPSILON,
                    Arrays.asList(toState(2), nToken, toState(prevState)), nextState,
                    Arrays.asList(toState(nextState),fToken,toState(prevState)), Arrays.asList(rule7));
        }
        pda.addTransition(3, nToken, EPSILON_STACK,2,
                Arrays.asList(toState(2), nToken),emptyRules);
        pda.addTransition(3, openParToken, EPSILON_STACK,3,
                Arrays.asList(toState(3), openParToken),emptyRules);
        pda.addTransition(4, endToken, Arrays.asList(toState(4)),5,
                Arrays.asList(toState(5), endToken), emptyRules);
        pda.addTransition(4, TerminalToken.EPSILON, Arrays.asList(laEndToken),5,
                Arrays.asList(toState(5), endToken), emptyRules);
        pda.addTransition(5, TerminalToken.EPSILON,
                Arrays.asList(toState(5), endToken, StateToken.WILDCARD, eToken, toState(1)),5,
                Arrays.asList(sToken), Arrays.asList(rule1));
        prevStatesForVariable = new int [] {1,3,10};
        nextStatesForVariable = new int [] {4,7,13};
        for (int i=0; i < prevStatesForVariable.length; i++) {
            int prevState = prevStatesForVariable[i];
            int nextState = nextStatesForVariable[i];
            pda.addTransition(6, closedParToken,
                    Arrays.asList(toState(6), tToken, toState(prevState)), nextState,
                    Arrays.asList(laClosedParToken, toState(nextState), eToken,toState(prevState)),
                    Arrays.asList(rule3));
            pda.addTransition(6, TerminalToken.EPSILON,
                    Arrays.asList(laClosedParToken,toState(6), tToken, toState(prevState)), nextState,
                    Arrays.asList(laClosedParToken, toState(nextState), eToken,toState(prevState)),
                    Arrays.asList(rule3));
            pda.addTransition(6, endToken,
                    Arrays.asList(toState(6), tToken, toState(prevState)), nextState,
                    Arrays.asList(laEndToken, toState(nextState), eToken,toState(prevState)),
                    Arrays.asList(rule3));
            pda.addTransition(6, TerminalToken.EPSILON,
                    Arrays.asList(laEndToken, toState(6), tToken, toState(prevState)), nextState,
                    Arrays.asList(laEndToken, toState(nextState), eToken,toState(prevState)),
                    Arrays.asList(rule3));
        }
        pda.addTransition(6, plusToken, EPSILON_STACK,10,
                Arrays.asList(toState(10), plusToken), emptyRules);
        pda.addTransition(6, TerminalToken.EPSILON, Arrays.asList(laPlusToken),10,
                Arrays.asList(toState(10), plusToken), emptyRules);
        pda.addTransition(7, closedParToken, Arrays.asList(toState(7)),9,
                Arrays.asList(toState(9), closedParToken), emptyRules);
        pda.addTransition(7, TerminalToken.EPSILON, Arrays.asList(laClosedParToken),9,
                Arrays.asList(toState(9), closedParToken), emptyRules);
        pda.addTransition(8, multToken, EPSILON_STACK,11,
                Arrays.asList(toState(11), multToken), emptyRules);
        prevStatesForVariable = new int [] {1,3,10,11};
        nextStatesForVariable = new int [] {6,6,6,12};
        for (int i=0; i < prevStatesForVariable.length; i++) {
            int prevState = prevStatesForVariable[i];
            int nextState = nextStatesForVariable[i];
            pda.addTransition(8, plusToken,
                    Arrays.asList(toState(8), fToken, toState(prevState)), nextState,
                    Arrays.asList(laPlusToken, toState(nextState), tToken,toState(prevState)),
                    Arrays.asList(rule5));
            pda.addTransition(8, closedParToken,
                    Arrays.asList(toState(8), fToken, toState(prevState)), nextState,
                    Arrays.asList(laClosedParToken, toState(nextState), tToken,toState(prevState)),
                    Arrays.asList(rule5));
            pda.addTransition(8, endToken,
                    Arrays.asList(toState(8), fToken, toState(prevState)), nextState,
                    Arrays.asList(laEndToken, toState(nextState), tToken,toState(prevState)),
                    Arrays.asList(rule5));
            pda.addTransition(8, TerminalToken.EPSILON,
                    Arrays.asList(laPlusToken, toState(8), fToken, toState(prevState)), nextState,
                    Arrays.asList(laPlusToken, toState(nextState), tToken,toState(prevState)),
                    Arrays.asList(rule5));
            pda.addTransition(8, TerminalToken.EPSILON,
                    Arrays.asList(laClosedParToken, toState(8), fToken, toState(prevState)), nextState,
                    Arrays.asList(laClosedParToken, toState(nextState), tToken,toState(prevState)),
                    Arrays.asList(rule5));
            pda.addTransition(8, TerminalToken.EPSILON,
                    Arrays.asList(laEndToken,toState(8), fToken, toState(prevState)), nextState,
                    Arrays.asList(laEndToken, toState(nextState), tToken,toState(prevState)),
                    Arrays.asList(rule5));
        }
        prevStatesForVariable = new int [] {1,3,10,11};
        nextStatesForVariable = new int [] {8,8,8,8};
        for (int i=0; i < prevStatesForVariable.length; i++) {
            int prevState = prevStatesForVariable[i];
            int nextState = nextStatesForVariable[i];
            pda.addTransition(9, TerminalToken.EPSILON,
                    Arrays.asList(toState(9), closedParToken,
                            StateToken.WILDCARD, eToken,
                            StateToken.WILDCARD, openParToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(toState(nextState),fToken,toState(prevState)), Arrays.asList(rule6));
            pda.addTransition(9, TerminalToken.EPSILON,
                    Arrays.asList(laClosedParToken, toState(9), closedParToken,
                            StateToken.WILDCARD, eToken,
                            StateToken.WILDCARD, openParToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(laClosedParToken, toState(nextState),fToken,toState(prevState)), Arrays.asList(rule6));
        }
        pda.addTransition(10, nToken, EPSILON_STACK,2,
                Arrays.asList(toState(2), nToken), emptyRules);
        pda.addTransition(10, openParToken, EPSILON_STACK,3,
                Arrays.asList(toState(3), openParToken), emptyRules);
        pda.addTransition(11, nToken, EPSILON_STACK,2,
                Arrays.asList(toState(2), nToken), emptyRules);
        pda.addTransition(11, openParToken, EPSILON_STACK,3,
                Arrays.asList(toState(3), openParToken), emptyRules);
        prevStatesForVariable = new int [] {1,3,10,11};
        nextStatesForVariable = new int [] {6,6,6,12};
        for (int i=0; i < prevStatesForVariable.length; i++) {
            int prevState = prevStatesForVariable[i];
            int nextState = nextStatesForVariable[i];
            pda.addTransition(12, TerminalToken.EPSILON,
                    Arrays.asList(toState(12), tToken,
                            StateToken.WILDCARD, multToken,
                            StateToken.WILDCARD, fToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(toState(nextState),tToken,toState(prevState)), Arrays.asList(rule4));
            pda.addTransition(12, TerminalToken.EPSILON,
                    Arrays.asList(laClosedParToken, toState(12), tToken,
                            StateToken.WILDCARD, multToken,
                            StateToken.WILDCARD, fToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(laClosedParToken,toState(nextState),tToken,toState(prevState)), Arrays.asList(rule4));
            pda.addTransition(12, TerminalToken.EPSILON,
                    Arrays.asList(laEndToken, toState(12), tToken,
                            StateToken.WILDCARD, multToken,
                            StateToken.WILDCARD, fToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(laEndToken, toState(nextState),tToken,toState(prevState)), Arrays.asList(rule4));
            pda.addTransition(12, TerminalToken.EPSILON,
                    Arrays.asList(laPlusToken, toState(12), tToken,
                            StateToken.WILDCARD, multToken,
                            StateToken.WILDCARD, fToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(laPlusToken, toState(nextState),tToken,toState(prevState)), Arrays.asList(rule4));
        }
        prevStatesForVariable = new int [] {1,3,10};
        nextStatesForVariable = new int [] {4,7,13};
        for (int i=0; i < prevStatesForVariable.length; i++) {
            int prevState = prevStatesForVariable[i];
            int nextState = nextStatesForVariable[i];
            pda.addTransition(13, TerminalToken.EPSILON,
                    Arrays.asList(toState(13), eToken,
                            StateToken.WILDCARD, plusToken,
                            StateToken.WILDCARD, tToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(toState(nextState),eToken,toState(prevState)), Arrays.asList(rule2));
            pda.addTransition(13, TerminalToken.EPSILON,
                    Arrays.asList(laClosedParToken, toState(13), eToken,
                            StateToken.WILDCARD, plusToken,
                            StateToken.WILDCARD, tToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(laClosedParToken, toState(nextState),eToken,toState(prevState)), Arrays.asList(rule2));
            pda.addTransition(13, TerminalToken.EPSILON,
                    Arrays.asList(laEndToken, toState(13), eToken,
                            StateToken.WILDCARD, plusToken,
                            StateToken.WILDCARD, tToken,
                            toState(prevState)
                    ), nextState,
                    Arrays.asList(laEndToken, toState(nextState),eToken,toState(prevState)), Arrays.asList(rule2));
        }
        List<Configuration> configurations = pda.process(toTerminalTokens(new String []
                {"n", "*", "n", "+", "(", "n", "*", "n",  ")", "$"}));
        printConfigs(configurations);
        pda.printDerivations(configurations);
        System.out.println("Accept string " + "n*n+(n*n)$? " + pda.acceptString(configurations));
        configurations = pda.process(toTerminalTokens(new String []
                {"n", "*", "n", "(", "n", "*", "n",  ")", "$"}));
        printConfigs(configurations);
        pda.printDerivations(configurations);
        System.out.println("Accept string " + "n*n(n*n)$? " + pda.acceptString(configurations));
        configurations = pda.process(toTerminalTokens(new String []
                {"(", "n", "*", "(", "n", "+", "n", ")", ")", "$"}));
        printConfigs(configurations);
        pda.printDerivations(configurations);
        System.out.println("Accept string " + "(n*(n+n))$? " + pda.acceptString(configurations));
    }

    public static void main(String [] args) throws Exception {
        test1();
        //test2();
    }

}
