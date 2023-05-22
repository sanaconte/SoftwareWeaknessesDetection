package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvUtil {


    private static List<String> getMergedLines(List<Path> paths) throws IOException {
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

    private static List<String> getMergedLinesV2(List<Path> paths) throws IOException {
        List<String> mergedLines = new LinkedList<>();
        String cabecalhoActual = "PROJECT_NAME,Node,VULNERABLE";
        for (Path p : paths){
            List<String> lines = Files.readAllLines(p, Charset.forName("UTF-8"));
            if (!lines.isEmpty()) {
                String padding = Arrays.stream(cabecalhoActual.split(","))
                        .filter(val -> !val.contains("Node") && !val.contains("VULNERABLE") && !val.contains("PROJECT_NAME"))
                        .map(val -> String.valueOf(0))
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
                cabecalhoActual = cabecalhoActual+","+h;
                mergedLines.addAll(newSubList);
            }
        }
        //System.out.println("final Head: "+cabecalhoActual);
        mergedLines.add(0, cabecalhoActual);
        return adjustLines(mergedLines);
    }

    private static List<String> adjustLines(List<String> mergedLines){
        int headerLength = mergedLines.get(0).split(",").length;
        return mergedLines
                .stream()
                .map(line -> {
                    int lineLength = line.split(",").length;
                    if(lineLength != headerLength){
                        int paddLength = headerLength - lineLength;
                        String padd = Stream.iterate(0, n -> n+1)
                                .takeWhile(n -> n < paddLength)
                                .map(n -> ""+0)
                                .collect(Collectors.joining(","));
                        return line+","+padd;
                    }
                    return line;
                }).collect(Collectors.toList());
    }


    private static List<String> getMergedLinesV3(List<List<String>> listaMatrizes) throws IOException {
        List<String> mergedLines = new LinkedList<>();
        String cabecalhoActual = "PROJECT_NAME,Node,VULNERABLE";
        for (List<String> lines : listaMatrizes){
           // List<String> lines = Files.readAllLines(p, Charset.forName("UTF-8"));
            if (!lines.isEmpty()) {
                String padding = Arrays.stream(cabecalhoActual.split(","))
                        .filter(val -> !val.contains("Node") && !val.contains("VULNERABLE") && !val.contains("PROJECT_NAME"))
                        .map(val -> "0")
                        .collect(Collectors.joining(","));
                //System.out.println("padding:"+padding+"AQUI");
                List<String> subListLines = lines.subList(1, lines.size());
                List<String> newSubList = subListLines.stream().map(line -> {
                    String[] split = line.split(",");
                    return getNewLine(padding, split);
                }).collect(Collectors.toList());

                String headerLines = lines.get(0);
                String newFeature = toStringWithoutNodeAndVul(headerLines);
                if(!newFeature.isBlank()){
                    cabecalhoActual = cabecalhoActual+","+newFeature;
                }
                mergedLines.addAll(newSubList);
            }
        }
        //System.out.println("final Head: "+cabecalhoActual);
        mergedLines.add(0, cabecalhoActual);
        return adjustLines(mergedLines);
    }

    private static List<String> getMergedLinesV4(List<List<String>> listaMatrizes){
        List<String> mergedLines = new ArrayList<>();
        for (List<String> lines : listaMatrizes){
            //List<String> lines = Files.readAllLines(p, Charset.forName("UTF-8"));
            if (!lines.isEmpty()) {
                if (mergedLines.isEmpty()) {
                    mergedLines.add(lines.get(0)); //add header only once
                }
                mergedLines.addAll(lines.subList(1, lines.size()));
            }
        }
        return mergedLines;
    }

    private static String getNewLine(String padding, String[] split) {
        String newLine =
                Arrays.stream(split).skip(3).collect(Collectors.joining(","));
        //System.out.println("newLine: "+newLine);
        if(padding.isBlank() && !newLine.isBlank()){
            newLine = split[0]+","+ split[1]+","+ split[2]+","+newLine;
        }
        else if(!padding.isBlank() && newLine.isBlank()){
            newLine = split[0]+","+ split[1]+","+ split[2]+","+ padding;
        }
        else if(!padding.isBlank() && !newLine.isBlank()){
            newLine = split[0]+","+ split[1]+","+ split[2]+","+ padding +","+newLine;
        }
        else{
            newLine = split[0]+","+ split[1]+","+ split[2];
        }
        return newLine;
    }

    private static String toStringWithoutNodeAndVul(String headerLines) {
        String news = Arrays.stream(headerLines.split(","))
                //.peek(col -> System.out.println(col))
                .filter(col -> !col.contains("Node") && !col.contains("VULNERABLE") && !col.contains("PROJECT_NAME"))
                .collect(Collectors.joining(","));
        return news;
    }

    public static void datatocsv(List<String> data, String filename){
        IOUtil.addTextToFile(data,filename);

    }

    public  static void criarUnicaMatriz(List<List<String>> listaMatrizes, String fileName){
        String path = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/CFG_SPOON/dataset/";
        try {
            List<String> mergedData = getMergedLinesV3(listaMatrizes);
            datatocsv(mergedData, path+fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createDataset(List<List<String>> listaMatrizes, String fileName) {
        String path = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/CFG_SPOON/dataset/";
        List<String> mergedData = getMergedLinesV4(listaMatrizes);
        datatocsv(mergedData, path + fileName);
    }

    public static void printDataset(List<List<String>> listaMatrizes){
        List<String> mergedData = getMergedLinesV4(listaMatrizes);
        mergedData.forEach(System.out::println);
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
