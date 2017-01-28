package uk.ac.liv.moduleextraction.util;

import com.google.common.base.Joiner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;


public class CSVWriter {

    private LinkedHashMap<String,String> fields;
    private static final Joiner commaJoiner = Joiner.on(',');
    private String location;

    public CSVWriter(String location){
        this.location = location;
        this.fields = new LinkedHashMap<>();
    }

    public void addMetric(String header, Object value){
        fields.put(header,String.valueOf(value));
    }

    public void writeCSVFile() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(location, false));
        writer.write(commaJoiner.join(fields.keySet()) + "\n");
        writer.write(commaJoiner.join(fields.values()) + "\n");
        writer.flush();
        writer.close();
    }

    public void printCSVFileToOutput(){
        //System.out.print(commaJoiner.join(fields.keySet()) + "\n");
        System.out.print(commaJoiner.join(fields.values()) + "\n");
    }

}
