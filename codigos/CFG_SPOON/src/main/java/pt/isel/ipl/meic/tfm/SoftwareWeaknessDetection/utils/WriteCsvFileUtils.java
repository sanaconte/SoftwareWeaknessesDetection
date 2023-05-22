package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.utils;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WriteCsvFileUtils {

    public static String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(str -> escapeSpecialCharacters(str) )
                .collect(Collectors.joining(","));
    }

    public static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public static void writeData(String filePath, List<String[]> data){
        // first create file object for file placed at location
        // specified by filepath
        File file = new File(filePath);
        CSVWriter writer = null;
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            writer = new CSVWriter(outputfile);
            /**
            // adding header to csv
            String[] header = { "Name", "Class", "Marks" };
            writer.writeNext(header);
            // add data to csv
            String[] data1 = { "Aman", "10", "620" };
            writer.writeNext(data1);
            String[] data2 = { "Suraj", "10", "630" };
            writer.writeNext(data2);
            **/
            writer.writeAll(data);

        }catch(Exception e){
            e.printStackTrace();
        }
        finally{
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
