package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation;

import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.NaiveExceptionControlFlowStrategy;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class DataControlFlowGraph {

    public static void main(String[] args) {
        Set<String> features = Set.of("bad", "good");
        try {
            String projectDirectory =  "E:/TFM/SAMATE-DATA-CI/144758-v1.0.0";
            //String file = "/src/main/resources/ProgramTest.java";
            //List<Integer> vulnerableLines = Arrays.asList(134,170,204);
            //String datasetFileName = "144758-v1.0.0.csv";
            List<CtElement> methodList = Transform.filterMethodByName(projectDirectory, features);

            System.out.println("methodList.size: "+methodList.size());

            methodList .forEach(ctElem -> {

                ControlFlowBuilder builder = new ControlFlowBuilder();

                EnumSet<NaiveExceptionControlFlowStrategy.Options> options;
                options = EnumSet.of(NaiveExceptionControlFlowStrategy.Options.ReturnWithoutFinalizers);
                builder.setExceptionControlFlowStrategy(new NaiveExceptionControlFlowStrategy(options));

                ControlFlowGraph graph =
                        builder.build(ctElem);
                System.out.println("methodName: "+((CtMethod)ctElem).getSimpleName());
                graph.simplifyBlockNodes();
                graph.simplify();
                System.out.println("graph.branchCount(): "+graph.branchCount());
                System.out.println("graph.statementCount(): "+graph.statementCount());
                System.out.println("graph.vertexSet().size(): "+graph.vertexSet().size());
                System.out.println(graph.toGraphVisText());
            });


        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
