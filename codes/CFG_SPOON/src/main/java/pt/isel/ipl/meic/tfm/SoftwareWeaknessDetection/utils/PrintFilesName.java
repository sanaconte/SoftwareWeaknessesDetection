package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

public class PrintFilesName {

    public static void main(String[] args) throws IOException {

        //String directory = "E:/TFM/SAMATE-CI/";

        String directory = "E:/TFM/SAMATE-NPD/";

        Stream.of(new File(directory).listFiles())
                .filter(file -> !file.isDirectory())
                .forEach(file -> System.out.println('"'+file.getName()+'"'+","));
                //.collect(Collectors.toSet())
    }
}
