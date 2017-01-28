package uk.ac.liv.moduleextraction.propositional.nSeparability;

import java.util.HashMap;


public class nConvertorFactory {

    private static HashMap<Integer, HashMap<Integer, nClassExpressionConvertor>> convertorMap = new HashMap<Integer, HashMap<Integer, nClassExpressionConvertor>>();

    private nConvertorFactory(){

    }

    public static nClassExpressionConvertor getClassExpressionConvertor(int[] domainElements, int chosenElement){
        HashMap<Integer, nClassExpressionConvertor> domainMap = convertorMap.get(domainElements);

        if(domainMap == null){
            domainMap = new HashMap<>();
             convertorMap.put(domainElements.length, domainMap);
        }

        nClassExpressionConvertor convertor = domainMap.get(chosenElement);

        if(convertor == null){
            convertor = new nClassExpressionConvertor(domainElements,chosenElement);
            domainMap.put(domainElements.length,convertor);
        }


        return convertor;
    }



}
