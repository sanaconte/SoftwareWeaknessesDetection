import fr.inria.controlflow.ControlFlowNode;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtLocalVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UseDefinitionChain {

    ReachingDefinition reachingDefinition;
    private Map<String, Set<String>> useDefinitionChain;

    public UseDefinitionChain(ReachingDefinition reaching){
        reachingDefinition = reaching;

        useDefinitionChain = new HashMap();
        fillUseDefinitionChain();
    }

    private Map<String, ControlFlowNode> getUseSet(){
        Map<String, ControlFlowNode> useSet = new HashMap();
        Set<ControlFlowNode> controlFlowNodes = reachingDefinition.getGraph().vertexSet();
        controlFlowNodes.forEach(c -> {
            if(c.getStatement() instanceof CtLocalVariable){
                CtLocalVariable ctLocalVariable = (CtLocalVariable)c.getStatement();
                if(ctLocalVariable.getAssignment() != null){
                    String expression = ctLocalVariable.getAssignment().toString();
                    reachingDefinition.getLocalVariables()
                            .stream().filter(var -> expression.contains(var))
                            .forEach(var -> useSet.put(var+":"+c.getId(), c));
                }
            }
            else if(c.getStatement() instanceof CtAssignment) {
                CtAssignment ctAssignment = (CtAssignment) c.getStatement();
                String expression = ctAssignment.getAssignment().toString();
                reachingDefinition.getLocalVariables()
                        .stream().filter(var -> expression.contains(var))
                        .forEach(var -> useSet.put(var+":"+c.getId(), c));
            }
        });
        return useSet;
    }

    private void fillUseDefinitionChain(){
        for(Map.Entry<String, ControlFlowNode> entry : getUseSet().entrySet()){
            String varName = entry.getKey().split(":")[0];
            Set<String> udChains = reachingDefinition.getIn().get(entry.getValue())
                    .stream()
                    .filter(e -> e.startsWith(varName))
                    .collect(Collectors.toSet());
            useDefinitionChain.put(entry.getKey(), udChains);
        }
    }

    private String printSet(Set<String> set){
        return set.stream().collect(Collectors.joining(",","{","}"));
    }
    public void prettyPrint(){
        System.out.println("useDefinitionChain: ");

        System.out.println("---------------------------------------------------------------------------------------------");

        System.out.printf("%10s %10s", "Use", "UD Chain");
        System.out.println();
        System.out.println("---------------------------------------------------------------------------------------------");

        for(Map.Entry<String, Set<String>> entry : useDefinitionChain.entrySet()){
            //System.out.println(entry.getKey() + ": " + printSet(entry.getValue()));
            System.out.format("%10s %10s", entry.getKey(), printSet(entry.getValue()));
            System.out.println();
        }

        System.out.println("----------------------------------------------------------------------------------------------");
    }

}
