package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation;

import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.NaiveExceptionControlFlowStrategy;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.utils.CsvUtil;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.stream.Collectors;

public class Transform {

    public static final List<String> CI_FEATURES =
            Arrays.asList("ProcessBuilder", "System.Runtime.getRuntime().exec", "Runtime.exec");

    private static boolean containsVulnerableFeatures(CtMethod method, Set<String> features){

        return method.getBody() != null &&
                features
                        .stream()
                        .anyMatch(feature -> method.getBody().prettyprint().contains(feature));

    }

    private static String getFileContent(String uri) throws IOException {
        FileInputStream vulFile = new FileInputStream(uri);
        String content = null;
        content = IOUtils.toString(vulFile, "UTF-8");
        return content;
    }

    public static List<CtElement> getMethodsFromProject(String projectDirectory, Set<String> features){
        Launcher launcher = new Launcher();
        //55165-v1.0.0
        //String project =  projectName //"55165-v1.0.0";
        //String val =  "E:\\TFM\\SAMATE-DATA\\"+project;
        launcher.addInputResource(projectDirectory+"/src/main/java");
        launcher.getEnvironment().setNoClasspath(true);
        CtModel model = launcher.buildModel();
        return  model.getElements(el -> el instanceof CtMethod)
                        .stream()
                        .map(ctEl -> (CtMethod)ctEl)
                        .filter(method -> method.getBody() !=null)
                        .filter(ctMethod -> containsVulnerableFeatures(ctMethod, features))
                        .collect(Collectors.toList());

    }

    public static List<CtElement> filterMethodByName(String projectDirectory, Set<String> methods){
        Launcher launcher = new Launcher();
        //55165-v1.0.0
        //String project =  projectName //"55165-v1.0.0";
        //String val =  "E:\\TFM\\SAMATE-DATA\\"+project;
        launcher.addInputResource(projectDirectory+"/src/main/java");
        launcher.getEnvironment().setNoClasspath(true);
        CtModel model = launcher.buildModel();
        return  model.getElements(el -> el instanceof CtMethod)
                .stream()
                .map(ctEl -> (CtMethod)ctEl)
                .filter(method -> method.getBody() !=null)
                .filter(ctMethod -> methods.stream().anyMatch(feat-> ctMethod.getSimpleName().equals(feat) || ctMethod.getSimpleName().contains(feat)) )
                .collect(Collectors.toList());

    }

    private static final List<CtElement> getMethodsFromFile(String fileUri) throws IOException {
        String data = getFileContent(fileUri);
        return  Launcher
                    .parseClass(data)
                    .getElements(el -> el instanceof CtMethod)
                    .stream()
                    .map(ctEl -> (CtMethod)ctEl)
                    //.filter(method -> containsVulnerableFeatures(method))
                    .filter(method -> method.getBody() != null)
                    .collect(Collectors.toList());
    }

    public static void main(String[] args) {

        Set<String> features = Set.of("ProcessBuilder", "Runtime.exec", "Runtime.getRuntime().exec");

        String userDirectory = FileSystems.getDefault()
                .getPath("")
                .toAbsolutePath()
                .toString();

        //InputStream inputStream = null;
        try {
            // inputs - exemplo para um projeto de test
            String projectDirectory = "E:/TFM/Trabalho/ProgramaTest/";
            List<Integer> vulnerableLines = Arrays.asList(8);
            String datasetFileName = "programTest.csv";
            List<CtElement> methodList = getMethodsFromProject(projectDirectory, features);

            //exemplo para um projeto SAMATE
//            String projectDirectory =  "E:/TFM/SAMATE-DATA-CI/144758-v1.0.0";
//            //String file = "/src/main/resources/ProgramTest.java";
//            List<Integer> vulnerableLines = Arrays.asList(134,170,204);
//            String datasetFileName = "144758-v1.0.0.csv";
//            List<CtElement> methodList =getMethodsFromProject(projectDirectory, features);

            List<List<String>> listaMatrizes = new ArrayList<>();
//            methodList .forEach(ctElem -> {
//
//                ControlFlowBuilder builder = new ControlFlowBuilder();
//
//                EnumSet<NaiveExceptionControlFlowStrategy.Options> options;
//                options = EnumSet.of(NaiveExceptionControlFlowStrategy.Options.ReturnWithoutFinalizers);
//                builder.setExceptionControlFlowStrategy(new NaiveExceptionControlFlowStrategy(options));
//
//                ControlFlowGraph graph =
//                        builder.build(ctElem);
//                graph.simplifyBlockNodes();
//                graph.simplify();
//                System.out.println(graph.toGraphVisText());
//                ReachingDefinition rd = new ReachingDefinition(graph, ctElem);
//                AdaptedUseDefinitionChain useDefinition = new AdaptedUseDefinitionChain(rd, vulnerableLines, features);
//                String projectMethodName = ctElem.getPosition().getFile().getName();
//                List<String> transform =
//                        useDefinition.transformFile(projectMethodName);
//                listaMatrizes.add(transform);
//                //useDefinition.printDataset();
//
//            });
            methodList .forEach(ctElem -> {
                ControlFlowBuilder builder = new ControlFlowBuilder();

                EnumSet<NaiveExceptionControlFlowStrategy.Options> options;
                options = EnumSet.of(NaiveExceptionControlFlowStrategy.Options.ReturnWithoutFinalizers);
                builder.setExceptionControlFlowStrategy(new NaiveExceptionControlFlowStrategy(options));

                ControlFlowGraph graph =
                        builder.build(ctElem);
                graph.simplifyBlockNodes();
                graph.simplify();
                System.out.println(graph.toGraphVisText());
                ReachingDefinition rd = new ReachingDefinition(graph, ctElem);
                String projectMethodName = ctElem.getPosition().getFile().getName();
                Pair<String, Integer> pa = new ImmutablePair<>("a.java", 2);

                Map<String, List<Integer>> VulnerabilityMap = new HashMap<>();
                VulnerabilityMap.put("a.java", Arrays.asList(33, 19));
                System.out.println("VulnerabilityMap: "+VulnerabilityMap);
                FeaturesExtraction featuresExtraction = new FeaturesExtraction(rd, projectMethodName, VulnerabilityMap);
                featuresExtraction.executeExtraction();
                List<String> transform =
                        featuresExtraction.getResultExtraction();
                listaMatrizes.add(transform);
                //useDefinition.printDataset();
            });
            CsvUtil.printDataset(listaMatrizes);
            //CsvUtil.createDataset(listaMatrizes, datasetFileName);
            //System.out.println(listaMatrizes);

        }catch (Exception e){
            e.printStackTrace();
        }
//        finally {
//            if (inputStream != null) {
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }
}
