import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvUtil {


    public static List<String> getMergedLines(List<Path> paths) throws IOException {
        List<String> mergedLines = new ArrayList<>();
        for (Path p : paths){
            List<String> lines = Files.readAllLines(p, Charset.forName("UTF-8"));
            if (!lines.isEmpty()) {
                if (mergedLines.isEmpty()) {
                    mergedLines.add(lines.get(0)); //add header only once
                }
                mergedLines.addAll(lines.subList(1, lines.size()));
            }
        }
        return mergedLines;
    }

    public static List<String> getMergedLinesV2(List<Path> paths) throws IOException {
        List<String> mergedLines = new LinkedList<>();
        String headAct = "PROJECT_NAME,Node,VULNERABLE";
        for (Path p : paths){
            List<String> lines = Files.readAllLines(p, Charset.forName("UTF-8"));
            if (!lines.isEmpty()) {
                String padding = Arrays.stream(headAct.split(","))
                        .filter(val -> !val.contains("Node") && !val.contains("VULNERABLE") && !val.contains("PROJECT_NAME"))
                        .map(val -> " ")
                        .collect(Collectors.joining(","));
                System.out.println("padding:"+padding+"AQUI");
                List<String> subListLines = lines.subList(1, lines.size());
                List<String> newSubList = subListLines.stream().map(line -> {
                    String[] split = line.split(",");
                    String newLine =
                            Arrays.stream(split).skip(3).collect(Collectors.joining(","));
                    //System.out.println("newLine: "+newLine);
                    if(padding.isEmpty()){
                        newLine = split[0]+","+split[1]+","+split[2]+","+newLine;
                    }else{
                        newLine = split[0]+","+split[1]+","+split[2]+","+padding+","+newLine;
                    }
                    return newLine;
                }).collect(Collectors.toList());

                String headerLines = lines.get(0);
                String h = toStringWithoutNodeAndVul(headerLines);
                headAct = headAct+","+h;
                mergedLines.addAll(newSubList);
            }
        }
        System.out.println("final Head: "+headAct);
        mergedLines.add(0, headAct);
        return mergedLines;
    }

    private static String toStringWithoutNodeAndVul(String headerLines) {
        String h = Arrays.stream(headerLines.split(","))
                //.peek(col -> System.out.println(col))
                .filter(col -> !col.contains("Node") && !col.contains("VULNERABLE") && !col.contains("PROJECT_NAME"))
                .collect(Collectors.joining(","));
        return h;
    }

    public static void datatocsv(List<String> data, String filename){
        IOUtil.addTextToFile(data,filename);

    }




    public static void main(String[] args) {
        String path = "E:\\TFM\\Trabalho\\SoftwareWeaknessesDetection\\codigos\\CFG_SPOON\\dataset";
        List<Path> csvlist = IOUtil.listdirectory(path, "csv");
        try {
            List<String> mergedData = getMergedLinesV2(csvlist);
            datatocsv(mergedData, path+"\\merged.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
