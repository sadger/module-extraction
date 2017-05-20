package uk.ac.liv.moduleextraction.propositional.satclauses;

import java.util.HashSet;

/**
 * Created by william on 04/11/14.
 */
public class SATSet extends HashSet<Integer>{
    HashSet<Integer> variables = new HashSet<Integer>();

    public void addVariable(int var){
        variables.add(var);
    }

    public HashSet<Integer> getVariables() {
        return variables;
    }
}
