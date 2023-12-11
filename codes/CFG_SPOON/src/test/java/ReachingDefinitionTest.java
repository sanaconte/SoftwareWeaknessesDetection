import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.NaiveExceptionControlFlowStrategy;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.utils.CsvUtil;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation.ReachingDefinition;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation.UseDefinitionChain;
import spoon.Launcher;
import spoon.experimental.SpoonifierVisitor;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReachingDefinitionTest {

    @Test
    void lambdaExpressions() {
        List<Integer> numbers = Arrays.asList(1, 2, 3);
        assertTrue(numbers.stream()
                .mapToInt(val -> val)//Integer::intValue
                .sum() > 5, () -> "Sum should be greater than 5");


    }

    @Test
    void prettyPrint() {
        String userDirectory = FileSystems.getDefault()
                .getPath("")
                .toAbsolutePath()
                .toString();

        InputStream inputStream = null;
        try {

            FileInputStream fis =
                    new FileInputStream(userDirectory + "/src/main/resources/ProgramTest.java");
            String data = IOUtils.toString(fis, "UTF-8");

            Launcher launcher = new Launcher();
            //55165-v1.0.0
            String project = "55165-v1.0.0";
            String val = "E:\\TFM\\SAMATE-DATA\\"+project;
            launcher.addInputResource(val+"/src/main/java");
            launcher.getEnvironment().setNoClasspath(true);
            CtModel model = launcher.buildModel();

            SpoonifierVisitor v = new SpoonifierVisitor(true);
            /*CtElement ctElement =
                    model.getElements(el -> el instanceof CtMethod)
                            .stream()
                            .map(ctEl -> (CtMethod)ctEl)
                            //.peek(ctMethod -> System.out.println("ctMethod_peek: "+ ctMethod.getSimpleName()))
                            .filter(ctMethod -> "bad".equals(ctMethod.getSimpleName()))
                            .collect(Collectors.toList())
                            .get(0);

             */

            //System.out.println("ctElement: "+ctElement);
            CtElement ctElement =
            Launcher
                    .parseClass(data)
                    .getElements(el -> el instanceof CtMethod).get(0);

           // System.out.println("ctElement: "+ctElement.toString());
            //ctElement.accept(v);
            ControlFlowBuilder builder = new ControlFlowBuilder();
            /*
             * Todas as exceções lançadas dentro de um bloco de try
             * são apanhados pelos catch imediatamente associados o bloco.
             * */
            EnumSet<NaiveExceptionControlFlowStrategy.Options> options;
            options = EnumSet.of(NaiveExceptionControlFlowStrategy.Options.ReturnWithoutFinalizers);

            builder.setExceptionControlFlowStrategy(new NaiveExceptionControlFlowStrategy(options));

            ControlFlowGraph graph =
                    builder.build(ctElement);
            graph.simplifyBlockNodes();
            graph.simplify();
            System.out.println(graph.toGraphVisText());
            //ReachingDefinition rd = new ReachingDefinition(graph, ctElement);
            //FunctionReachingDefinition frd = new FunctionReachingDefinition(graph);
            //frd.prettyPrint();
           // rd.prettyPrint();
           // UseDefinitionChain useDefinition = new UseDefinitionChain(rd);
            //useDefinition.prettyPrint();
            //useDefinition.printDataset();
            ReachingDefinition rd = new ReachingDefinition(graph, ctElement);
            UseDefinitionChain useDefinition = new UseDefinitionChain(rd, Arrays.asList(7));
            String projectMethodName = project + "-" + ((CtMethod) ctElement).getSimpleName();
            List<String> transform =
                    useDefinition.transformFile(projectMethodName);
            List<List<String>> listaMatrizes = new ArrayList<>();
            listaMatrizes.add(transform);
            CsvUtil.criarUnicaMatriz(listaMatrizes, "test-fileName");

           /* CheckersScanner scanner = new CheckersScanner(launcher.getFactory());
            scanner.scan(ctElement);
            scanner.getWarnings().forEach(warning -> {
                System.out.println("warning: "+warning);
            });*/


        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String projectDirectory = "E:/TFM/TEST-DATA-CI/";

    @Test
    void testTransformFile(){
        String userDirectory = FileSystems.getDefault()
                .getPath("")
                .toAbsolutePath()
                .toString();

        String datasetTestName = "test-ci-dataset.csv";

        try {

            //String fileName = "CommandInjection.java";
            List<String> projectList = List.of("SasanLabs");
            List<Integer> potentialVulnerabilityLines = Arrays.asList(44,46,62,75,89,104,116,127);
            Map<String, List<Integer>> fileList = new HashMap<>();
            fileList.put("CommandInjection.java", potentialVulnerabilityLines);
            //List<List<String>> listaMatrizes = forEachProject(projectList, potentialVulnerabilityLines);
            String projectDirectory = "E:/TFM/TEST-DATA-CI/SasanLabs/";
            List<List<String>> listaMatrizes = new ArrayList<>();
            forProject(fileList, projectDirectory, listaMatrizes);
            CsvUtil.criarUnicaMatriz(listaMatrizes, datasetTestName);

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private String getContent(String fileName){

        String uri = projectDirectory+fileName;

        FileInputStream vulFile =
                null;
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

        return content;
    }



    private void forProject(Map<String, List<Integer>> fileList,
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

            ControlFlowBuilder builder = new ControlFlowBuilder();

            EnumSet<NaiveExceptionControlFlowStrategy.Options> options;
            options = EnumSet.of(NaiveExceptionControlFlowStrategy.Options.ReturnWithoutFinalizers);
            builder.setExceptionControlFlowStrategy(new NaiveExceptionControlFlowStrategy(options));

            ctElement.forEach(ctElem -> {

                ControlFlowGraph graph =
                        builder.build(ctElem);
                graph.simplifyBlockNodes();
                graph.simplify();
                //System.out.println(graph.toGraphVisText());
                ReachingDefinition rd = new ReachingDefinition(graph, ctElem);
                UseDefinitionChain useDefinition = new UseDefinitionChain(rd, vulnerableLines);
                String projectMethodName = projectDirectory + "-" + ((CtMethod) ctElem).getSimpleName();
                List<String> transform =
                        useDefinition.transformFile(projectMethodName);
                listaMatrizes.add(transform);

            });
        }

    }

    private List<List<String>> forEachProject(List<String> projectList, List<Integer> potentialVulnerabilityLines) {
        List<List<String>> listaMatrizes = new ArrayList<>();
        for (String project: projectList) {

           // String content = getContent(project);
            String val = "E:/TFM/TEST-DATA-CI/"+project;
            Launcher launcher = new Launcher();
            launcher.addInputResource(val);
            launcher.getEnvironment().setNoClasspath(true);
            CtModel model = launcher.buildModel();

            List<CtElement> ctElement =
                    model.getElements(el -> el instanceof CtMethod)
                            .stream()
                            .map(ctEl -> (CtMethod)ctEl)
                            //.peek(ctMethod -> System.out.println("ctMethod_peek: "+ ctMethod.getSimpleName()))
                            //.filter(ctMethod -> "getResponseFromPingCommand".equals(ctMethod.getSimpleName()))
                            .filter(ctMethod -> ctMethod.getBody() != null)
                            .collect(Collectors.toList());


            ControlFlowBuilder builder = new ControlFlowBuilder();

            EnumSet<NaiveExceptionControlFlowStrategy.Options> options;
            options = EnumSet.of(NaiveExceptionControlFlowStrategy.Options.ReturnWithoutFinalizers);
            builder.setExceptionControlFlowStrategy(new NaiveExceptionControlFlowStrategy(options));

             ctElement.forEach(ctElem -> {
                 ControlFlowGraph graph =
                         builder.build(ctElem);
                 graph.simplifyBlockNodes();
                 graph.simplify();
                 //System.out.println(graph.toGraphVisText());
                 ReachingDefinition rd = new ReachingDefinition(graph, ctElem);
                 UseDefinitionChain useDefinition = new UseDefinitionChain(rd, potentialVulnerabilityLines);
                 String projectMethodName = project + "-" + ((CtMethod) ctElem).getSimpleName();
                 List<String> transform =
                         useDefinition.transformFile(projectMethodName);
                 listaMatrizes.add(transform);
             });
        }
        return listaMatrizes;
    }
}
