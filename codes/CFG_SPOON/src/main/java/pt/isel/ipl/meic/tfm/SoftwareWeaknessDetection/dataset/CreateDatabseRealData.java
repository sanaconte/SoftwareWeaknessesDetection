package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.dataset;

import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.NaiveExceptionControlFlowStrategy;
import org.apache.commons.io.IOUtils;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation.ReachingDefinition;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation.UseDefinitionChain;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.utils.CsvUtil;
import spoon.Launcher;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CreateDatabseRealData {

    private static void forProject(Map<String, List<Integer>> fileList,
                            String projectDirectory,
                            List<List<String>> listaMatrizes){

        for(Map.Entry<String, List<Integer>> entry:fileList.entrySet()){
            String uri = projectDirectory+entry.getKey();
            List<Integer> vulnerableLines = entry.getValue();
            //int startLine = location.getPhysicalLocation().getRegion().getStartLine();

            FileInputStream vulFile = null;
            try {
                vulFile = new FileInputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String content = null;
            try {
                content = IOUtils.toString(vulFile, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<CtElement> ctElement =
                    Launcher
                            .parseClass(content)
                            .getElements(el -> el instanceof CtMethod)
                            .stream()
                            .map(ctEl -> (CtMethod)ctEl)
                            //.filter(ctMethod -> /**ctMethod.getSimpleName().startsWith("good") ||**/
                            //      ctMethod.getSimpleName().startsWith("bad") ||
                            //            ctMethod.getSimpleName().contains("bad")  )
                            .filter(ctMethod -> ctMethod.getBody() != null)
                            .collect(Collectors.toList());


            ctElement.forEach(ctElem -> {

                ControlFlowBuilder builder = new ControlFlowBuilder();

                EnumSet<NaiveExceptionControlFlowStrategy.Options> options;
                options = EnumSet.of(NaiveExceptionControlFlowStrategy.Options.ReturnWithoutFinalizers);
                builder.setExceptionControlFlowStrategy(new NaiveExceptionControlFlowStrategy(options));

                ControlFlowGraph graph =
                        builder.build(ctElem);
                graph.simplifyBlockNodes();
                graph.simplify();
                //System.out.println(graph.toGraphVisText());
                ReachingDefinition rd = new ReachingDefinition(graph, ctElem);
                UseDefinitionChain useDefinition = new UseDefinitionChain(rd, vulnerableLines);
                String projectMethodName = uri + "-" + ((CtMethod) ctElem).getSimpleName();
                List<String> transform =
                        useDefinition.transformFile(projectMethodName);
                listaMatrizes.add(transform);

            });
        }

    }

    public static void main(String[] args) {
        Map<String, List<Integer>> fileList = new HashMap<>();

        //String datasetTestName = "test-ci-dataset.csv";
        //String datasetTestName = "test-npd-dataset.csv";

        // project SasanLabs
        //String datasetTestName = "test-ci-dataset.csv";
        //String projectDirectory = "E:/TFM/TEST-DATA-CI/SasanLabs/";
        //List<Integer> potentialVulnerabilityLines = Arrays.asList(44,46,62,75,89,104,116,127);
        //fileList.put("CommandInjection.java", potentialVulnerabilityLines);

        // Project ArmazeFileManager
//        String datasetTestName = "test-ci-ArmazeFileManager-dataset.csv";
//        String projectDirectory = "E:/TFM/NVD/AmazeFileManager-3.5.1/AmazeFileManager-3.5.1";
//        List<Integer> potentialVulnerabilityLines = Arrays.asList(61, 647);
//        fileList.put("/app/src/test/java/com/amaze/filemanager/test/ShadowShellInteractive.java", potentialVulnerabilityLines);
//        fileList.put("/app/src/main/java/com/amaze/filemanager/filesystem/Operations.java", potentialVulnerabilityLines);

        // Project opentsdb
        String datasetTestName = "test-ci-opentsdb-dataset.csv";
        String projectDirectory = "E:/TFM/NVD/OpenTSDB/opentsdb";
        List<Integer> potentialVulnerabilityLines =
                Arrays.asList(884, 749, 757, 765, 773, 781, 781,
                        789, 797, 805, 808, 811, 819, 827, 835, 843, 851, 861);
        fileList.put("/src/tsd/GraphHandler.java", potentialVulnerabilityLines);

        try {
            List<List<String>> listaMatrizes = new ArrayList<>();
            forProject(fileList, projectDirectory, listaMatrizes);
            CsvUtil.criarUnicaMatriz(listaMatrizes, datasetTestName);

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
