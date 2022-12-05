import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.NaiveExceptionControlFlowStrategy;
import fr.inria.spoon.dataflow.checkers.NullDereferenceChecker;
import fr.inria.spoon.dataflow.scanners.CheckersScanner;
import fr.inria.spoon.dataflow.warning.Warning;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static fr.inria.spoon.dataflow.warning.WarningKind.NULL_DEREFERENCE;

public class ASTExample {

    public static void main(String[] args) {
        InputStream inputStream = null;
        // Contruir o modelo a partir do projecto.
        //System.out.println(v.getResult());
        Launcher launcher = new Launcher();
        String val = "E:\\TFM\\SAMATE-DATA\\139910-v1.0.0";
        launcher.addInputResource(val+"/src/main/java");
        //launcher.addInputResource(""); -> com isso posso adicionar vários projetos
        launcher.getEnvironment().setNoClasspath(true);
        CtModel model = launcher.buildModel();


        //CtElement ctMethod = model.getElements(el -> el instanceof CtMethod).get(0);
       // System.out.println("ctMethod: "+ctMethod);
       // CatchProcessor catchProcessor = new CatchProcessor();
      //  catchProcessor.process(model.getRootPackage());
        /**
        CheckersScanner scanner = new CheckersScanner(launcher.getFactory());
        NullDereferenceChecker nullChecker = new NullDereferenceChecker(scanner);

        model
                .getElements(el -> el instanceof CtAssignment)
                .stream()
                .map(ctElement -> (CtAssignment) ctElement)
                .peek(c -> System.out.println("c: "+c))
                .collect(Collectors.toList())
                .forEach(c -> {
                    nullChecker.checkAssignmentResult(c);
                });

        */

        CtElement ctElement =
                model.getElements(el -> el instanceof CtMethod).get(0);
        System.out.println("ctElement: "+ctElement);



        //NullDereferenceChecker nullChecker = new NullDereferenceChecker(scanner);

       // model.getAllTypes().forEach(element -> scanner.scan(element));

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

        CheckersScanner scanner = new CheckersScanner(launcher.getFactory());
        graph.vertexSet().forEach(c -> {
            scanner.scan(c.getStatement());
            List<Warning> warnings = scanner
                    .getWarnings().stream()
                    .filter(warning -> warning.kind == NULL_DEREFERENCE)
                    .collect(Collectors.toList());

            System.out.println("warnings.size: "+warnings.size());
            for (Warning warn : warnings) {
                System.out.println("warn.message: "+warn.message + " warn.position: " + warn.position);
            }
        });


    }

}
