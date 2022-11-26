import fr.inria.spoon.dataflow.checkers.NullDereferenceChecker;
import fr.inria.spoon.dataflow.scanners.CheckersScanner;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtCatch;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.io.InputStream;
import java.util.stream.Collectors;

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

        CtElement ctMethod = model.getElements(el -> el instanceof CtMethod).get(0);
        System.out.println("ctMethod: "+ctMethod);
        CatchProcessor catchProcessor = new CatchProcessor();
        catchProcessor.process(model.getRootPackage());

        CheckersScanner scanner = new CheckersScanner(launcher.getFactory());
        NullDereferenceChecker nullChecker = new NullDereferenceChecker(scanner);


    }

}
