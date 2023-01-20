import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.NaiveExceptionControlFlowStrategy;
import fr.inria.spoon.dataflow.scanners.CheckersScanner;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.experimental.SpoonifierVisitor;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.support.reflect.declaration.CtMethodImpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
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
            CtElement ctElement =
                    model.getElements(el -> el instanceof CtMethod)
                            .stream()
                            .map(ctEl -> (CtMethod)ctEl)
                            //.peek(ctMethod -> System.out.println("ctMethod_peek: "+ ctMethod.getSimpleName()))
                            .filter(ctMethod -> "bad".equals(ctMethod.getSimpleName()))
                            .collect(Collectors.toList())
                            .get(0);

            //System.out.println("ctElement: "+ctElement);
           /* CtElement ctElement =
            Launcher
                    .parseClass(data)
                    .getElements(el -> el instanceof CtMethod).get(0);
            */
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
            ReachingDefinition rd = new ReachingDefinition(graph, ctElement);
            //FunctionReachingDefinition frd = new FunctionReachingDefinition(graph);
            //frd.prettyPrint();
           // rd.prettyPrint();
            UseDefinitionChain useDefinition = new UseDefinitionChain(rd);
            //useDefinition.prettyPrint();
            //useDefinition.printDataset();
            useDefinition.printFunctionUseDef(userDirectory+"/dataset/"+project+".csv", project);


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

    @Test
    void testTransformFile(){
        String userDirectory = FileSystems.getDefault()
                .getPath("")
                .toAbsolutePath()
                .toString();

        InputStream inputStream = null;
        try {

            List<String> projectList = List.of("55091-v1.0.0", "55105-v1.0.0", "55165-v1.0.0", "139910-v1.0.0");
            List<List<String>> listaMatrizes = forEachProject(userDirectory, projectList);
            CsvUtil.criarUnicaMatriz(listaMatrizes);

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

    private List<List<String>> forEachProject(String userDirectory, List<String> projectList) {
        List<List<String>> listaMatrizes = new ArrayList<>();
        for (String project: projectList) {

            Launcher launcher = new Launcher();
            String val = "E:\\TFM\\SAMATE-DATA\\"+project;
            launcher.addInputResource(val+"/src/main/java");
            launcher.getEnvironment().setNoClasspath(true);
            CtModel model = launcher.buildModel();

            SpoonifierVisitor v = new SpoonifierVisitor(true);
            List<CtElement> ctElement =
                    model.getElements(el -> el instanceof CtMethod)
                            .stream()
                            .map(ctEl -> (CtMethod)ctEl)
                            //.peek(ctMethod -> System.out.println("ctMethod_peek: "+ ctMethod.getSimpleName()))
                            .filter(ctMethod -> ctMethod.getSimpleName().startsWith("bad"))
                            .filter(ctMethod -> ctMethod.getBody() != null)
                            .collect(Collectors.toList());
                            //.get(0);

            ControlFlowBuilder builder = new ControlFlowBuilder();

            EnumSet<NaiveExceptionControlFlowStrategy.Options> options;
            options = EnumSet.of(NaiveExceptionControlFlowStrategy.Options.ReturnWithoutFinalizers);
            builder.setExceptionControlFlowStrategy(new NaiveExceptionControlFlowStrategy(options));

            ctElement.forEach(ctElem -> {

                ControlFlowGraph graph =
                        builder.build(ctElem);
                graph.simplifyBlockNodes();
                graph.simplify();
                System.out.println(graph.toGraphVisText());
                ReachingDefinition rd = new ReachingDefinition(graph, ctElem);
                UseDefinitionChain useDefinition = new UseDefinitionChain(rd);
                String projectMethodName = project + "-" + ((CtMethod) ctElem).getSimpleName();
                List<String> transform =
                        useDefinition.transformFile(projectMethodName);
                listaMatrizes.add(transform);

            });

        }
        return listaMatrizes;
    }
}
