package uk.ac.liv.moduleextraction.propositional.nSeparability;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by william on 17/10/14.
 */
public class RSets {


    private ArrayList<int[]> rSets;

    /**
     * Generate all subsets of a set of a given size
     * @param set - input set
     * @param subsetSize - size of required subsets
     */
    public RSets(int[] set, int subsetSize){
        rSets = new ArrayList<int[]>();
        processSubsets(set, subsetSize);
    }

    public ArrayList<int[]> getRsets() {
        return rSets;
    }

    /**
     * Create the initial subset of the size required and
     * pass it to processLargerSubsets for population
     * @param set - input set
     * @param subsetSize - required subset size
     */
    private void processSubsets(int[] set, int subsetSize) {
        int[] subset = new int[subsetSize];
        processLargerSubsets(set, subset, 0, 0);
    }

    /**
     * Generate all subsets of a given size recursively
     * @param set - Input set
     * @param subset - current subset we are generating
     * @param subsetSize - required subset size
     * @param nextIndex - index of the current subset we are modifying
     */
    private void processLargerSubsets(int[] set, int[] subset, int subsetSize, int nextIndex) {
        //System.out.println(Arrays.toString(set) + "," + Arrays.toString(subset) + "," + subsetSize + "," + nextIndex);
        if (subsetSize == subset.length) {
            rSets.add(Arrays.copyOf(subset,subset.length));
        } else {
            for (int j = nextIndex; j < set.length; j++) {
                subset[subsetSize] = set[j];
                processLargerSubsets(set, subset, subsetSize + 1, j + 1);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int[] set = {1,2,3,4};
        RSets r  = new RSets(set, 2);
        for(int[] rrrr : r.getRsets()){
            System.out.println(Arrays.toString(rrrr));
        }

    }


}

