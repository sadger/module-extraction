package uk.ac.liv.moduleextraction.propositional.cnf;

class NameGenerator{
    private static long index = 1;
    private static StringBuilder builder;

    public static String getFreshName(){
        builder = new StringBuilder(5);
        builder.append("x_");
        builder.append(index++);
        return builder.toString();
    }
}

