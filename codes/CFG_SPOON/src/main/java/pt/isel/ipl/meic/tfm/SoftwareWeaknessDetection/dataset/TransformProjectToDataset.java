package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.dataset;

import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.NaiveExceptionControlFlowStrategy;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation.FeaturesExtraction;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation.ReachingDefinition;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation.Transform;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation.UseDefinition;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.utils.CsvUtil;
import spoon.reflect.declaration.CtElement;

import java.util.*;

public class TransformProjectToDataset {

    public static void main(String[] args) {

        if(args.length==0 || args[0]==null){
            throw new RuntimeException("No such project directory.");
        }


        if(args.length==0||args[1] == null){
            throw new RuntimeException("specify dataset name");
        }

        System.out.println("projectDirectory: "+args[0]);
        System.out.println("Dataset name: "+args[1]);
        String projectDirectory = args[0];
        List<CtElement> methodsFromProject = Transform.getMethodsFromProject(projectDirectory);
        List<List<String>> listaMatrizes = new ArrayList<>();
        methodsFromProject .forEach(ctElem -> {
            ControlFlowBuilder builder = new ControlFlowBuilder();

            EnumSet<NaiveExceptionControlFlowStrategy.Options> options;
            options = EnumSet.of(NaiveExceptionControlFlowStrategy.Options.ReturnWithoutFinalizers);
            builder.setExceptionControlFlowStrategy(new NaiveExceptionControlFlowStrategy(options));

            ControlFlowGraph graph =
                    builder.build(ctElem);
            graph.simplifyBlockNodes();
            graph.simplify();
            System.out.println(graph.toGraphVisText());
            String projectMethodName = ctElem.getPosition().getFile().getName();
            ReachingDefinition rd = new ReachingDefinition(graph, ctElem);
            UseDefinition useDefinition = new UseDefinition(rd);
            useDefinition.printUseDefinitionChain();

            /*
            TODO -> lidar com caso de não preencimento de coluna VULNERABLE para projetos reais.
             */
            Map<String, List<Integer>> emptyList = new HashMap<>();
            FeaturesExtraction featuresExtraction = new FeaturesExtraction(rd, useDefinition, projectMethodName, emptyList );
            featuresExtraction.executeExtraction();
            List<String> transform =
                    featuresExtraction.getResultExtraction();
            listaMatrizes.add(transform);

        });
        String datasetFileName = args[1];
        CsvUtil.createDataset(listaMatrizes, datasetFileName);
    }
}
