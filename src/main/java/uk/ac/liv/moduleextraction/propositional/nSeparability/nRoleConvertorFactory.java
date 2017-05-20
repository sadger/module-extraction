package uk.ac.liv.moduleextraction.propositional.nSeparability;


import java.util.HashMap;

public class nRoleConvertorFactory {

    private static HashMap<Integer, HashMap<Integer, nElementRoleConvertor>> convertorMap = new HashMap<Integer, HashMap<Integer, nElementRoleConvertor>>();


    public static nElementRoleConvertor getNElementRoleConvertor(int firstElement, int secondElement){

        HashMap<Integer, nElementRoleConvertor> firstElementMap = convertorMap.get(firstElement);

        if(firstElementMap == null){
             firstElementMap = new HashMap<Integer,nElementRoleConvertor>();
             convertorMap.put(firstElement,firstElementMap);
        }

        nElementRoleConvertor convertor = firstElementMap.get(secondElement);

        if(convertor == null){
            convertor = new nElementRoleConvertor(firstElement,secondElement);
            firstElementMap.put(secondElement,convertor);
        }

        return convertor;

    }
}
