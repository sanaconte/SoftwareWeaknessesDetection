import spoon.processing.AbstractParallelProcessor;
import spoon.processing.AbstractProcessor;
import spoon.processing.Processor;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtCatch;
import spoon.reflect.declaration.CtElement;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CatchProcessor extends AbstractProcessor<CtElement> {

    @Override
    public void process(CtElement ctElement) {
        ctElement
            .getElements(c -> c instanceof CtCatch)
            .stream().map( c -> (CtCatch)c)
            .forEach(el -> {
                if (el.getBody().getStatements().size() == 0) {
                    System.err.println(" empty ␣ catch ␣ clause ␣at␣"+ el.getPosition());
                    //getFactory().getEnvironment().report(this, Level.WARN, element, "empty catch clause");
                }
            });
    }
}
