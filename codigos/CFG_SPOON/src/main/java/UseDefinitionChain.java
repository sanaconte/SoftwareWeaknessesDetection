import fr.inria.controlflow.ControlFlowNode;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UseDefinitionChain {

    ReachingDefinition reachingDefinition;
    private Map<String, Set<String>> useDefinitionChain;
    private Map<ControlFlowNode, Map<String, Integer>> functionUseDef;

    private Set<String> functionSet;

    public UseDefinitionChain(ReachingDefinition reaching){
        reachingDefinition = reaching;

        useDefinitionChain = new HashMap();
        functionSet = new HashSet();
        fillUseDefinitionChain();
        fillFunctionUseDefEmpty();
        fillFunctionUseDefinition();
    }

    private void fillFunctionUseDefEmpty(){
        functionUseDef = new HashMap( );
        reachingDefinition.getGraph().vertexSet().forEach(c -> {
            Map<String, Integer> innerMap = new HashMap();
           List<CtInvocation> ctInvocationSet = getCtInvocation(c);
            functionSet.forEach(funcName -> {
                innerMap.put(funcName, 0);
            });
            ctInvocationSet.forEach(ctInvocation -> {
                if(ctInvocation != null){
                    String functionName = ctInvocation.prettyprint().split("\\(")[0];
                    innerMap.put(functionName, 1);
                }
            });
            functionUseDef.put(c, innerMap);
        });
    }


    private Map<String, ControlFlowNode> getUseSet(){
        Map<String, ControlFlowNode> useSet = new HashMap();
        Set<ControlFlowNode> controlFlowNodes = reachingDefinition.getGraph().vertexSet();
        controlFlowNodes.forEach(c -> {

            if(c.getStatement() instanceof CtLocalVariable){
                CtLocalVariable ctLocalVariable = (CtLocalVariable)c.getStatement();
                boolean is = ctLocalVariable.getAssignment() instanceof CtInvocation;
                if(is){
                    treatCtInvocation((CtInvocation) ctLocalVariable.getAssignment(), reachingDefinition, useSet, c);
                }
                if(ctLocalVariable.getAssignment() != null){
                    String expression = ctLocalVariable.getAssignment().toString();
                    reachingDefinition.getLocalVariables()
                            .stream().filter(var -> expression.contains(var))
                            .forEach(var -> useSet.put(var+":"+c.getId(), c));
                }
            }
            if(c.getStatement() instanceof CtAssignment) {
                CtAssignment ctAssignment = (CtAssignment) c.getStatement();
                boolean is = ctAssignment.getAssignment() instanceof CtInvocation;
                if(is){
                    treatCtInvocation((CtInvocation) ctAssignment.getAssignment(), reachingDefinition, useSet, c);
                }
                String expression = ctAssignment.getAssignment().toString();
                reachingDefinition.getLocalVariables()
                        .stream().filter(var -> expression.contains(var))
                        .forEach(var -> useSet.put(var+":"+c.getId(), c));
            }
            // para considerar a presenca de invocação das funçoes que usam variveis
             if(c.getStatement() instanceof CtInvocation) {
                 treatCtInvocation((CtInvocation) c.getStatement(), reachingDefinition, useSet, c);
             }
            // considerar os Nodes que têm vários elementos ctAssignment
             if(c.getStatement() != null){
                 c.getStatement()
                 .getElements(el -> el instanceof CtAssignment)
                 .forEach(ctElement -> {
                     CtAssignment ctAssignment = (CtAssignment) ctElement;
                     boolean is = ctAssignment.getAssignment() instanceof CtInvocation;
                     if(is){
                         treatCtInvocation((CtInvocation) ctAssignment.getAssignment(), reachingDefinition, useSet, c);
                     }
                 });
             }
        });
        return useSet;
    }

    private void treatCtInvocation(CtInvocation ctInvocation, ReachingDefinition reachingDefinition, Map<String, ControlFlowNode> useSet, ControlFlowNode c) {
        List<CtExpression> expressions = (List<CtExpression>)ctInvocation.getArguments();
        List<String> arguments = expressions.stream().map(exp -> exp.toString()).collect(Collectors.toList());
        String functionName = ctInvocation.prettyprint().split("\\(")[0];
        functionSet.add(functionName);
        reachingDefinition.getLocalVariables()
                .stream().filter(var -> arguments.stream().anyMatch(a -> a.contains(var)))
                .forEach(var -> {
                    useSet.put(var+":"+ c.getId(), c);
                });

    }

    private void fillUseDefinitionChain(){
        for(Map.Entry<String, ControlFlowNode> entry : getUseSet().entrySet()){
            String varName = entry.getKey().split(":")[0];
           // System.out.println("varName: " + varName + " key: " +entry.getValue()+ " in: "+reachingDefinition.getIn().get(entry.getValue()));
            Set<String> udChains = reachingDefinition.getIn().get(entry.getValue())
                    .stream()
                    .filter(e -> e.startsWith(varName))
                    .collect(Collectors.toSet());
            useDefinitionChain.put(entry.getKey(), udChains);
        }
    }

    private List<CtInvocation> getCtInvocation(ControlFlowNode c){
        if(c.getStatement() instanceof CtLocalVariable){
            CtLocalVariable ctLocalVariable = (CtLocalVariable)c.getStatement();
            boolean is = ctLocalVariable.getAssignment() instanceof CtInvocation;
            if(is) {
                List<CtInvocation> set = new ArrayList();
                set.add((CtInvocation) ctLocalVariable.getAssignment());
                return set;
            }else {
                return new ArrayList();
            }
        }
        else if(c.getStatement() instanceof CtAssignment) {
            CtAssignment ctAssignment = (CtAssignment) c.getStatement();
            boolean is = ctAssignment.getAssigned() instanceof CtInvocation;
            if(is) {
                List<CtInvocation> set = new ArrayList();
                set.add((CtInvocation) ctAssignment.getAssignment());
                return set;
            }else {
                return new ArrayList();
            }
        }
        else if(c.getStatement() instanceof CtInvocation){
            List<CtInvocation> set = new ArrayList();
            set.add((CtInvocation) c.getStatement());
            return set;
        }
        else if(c.getStatement() != null) {
            // considerar os Nodes que têm vários elementos ctAssignment
            List<CtElement> els =  c.getStatement()
                    .getElements(el -> el instanceof CtAssignment);
            return els.stream()
                    .map(el -> (CtAssignment) el)
                    .map( ctAssignment -> (CtInvocation) ctAssignment.getAssignment())
                    .collect(Collectors.toList());
        }
        else{
           return new ArrayList();
        }
    }

    private ControlFlowNode getControlFlowNodeFromDefinition(String definition){
        int id = Integer.parseInt(definition.split(":")[1]);
        return reachingDefinition.getGraph().findNodeById(id);
    }

    private boolean processNode(ControlFlowNode c, List<CtInvocation> ctInvocationSet){
        boolean[] res = {false};
        ctInvocationSet.forEach(ctInvocation -> {
            if(ctInvocation != null) {
                // List<CtExpression> expressions = (List<CtExpression>) ctInvocation.getArguments();
                //List<String> arguments = expressions.stream().map(exp -> exp.toString()).collect(Collectors.toList());
                String functionName = ctInvocation.prettyprint().split("\\(")[0];
                // System.out.println("procElement: " + "functionName: "+functionName);
                functionUseDef.get(c).put(functionName, 1);
                res[0] = true;
            }
        });
        return res[0];
    }

    private void fillFunctionUseDefinition(){
        for(Map.Entry<String, ControlFlowNode> entry : getUseSet().entrySet()){
           // String varName = entry.getKey().split(":")[0];
            ControlFlowNode c = entry.getValue();
            List<CtInvocation> set = getCtInvocation(c);
            boolean processed = processNode(c, set);
            if(processed) {
                processNodeChain(c, entry, set);
                /**
                List<CtExpression> expressions = set.stream()
                        .map(ctInvocation -> (List<CtExpression>)ctInvocation.getArguments())
                        .flatMap(list -> list.stream()).collect(Collectors.toList());
                List<String> arguments = expressions.stream().map(exp -> exp.toString()).collect(Collectors.toList());
                System.out.println("varName: "+ entry.getKey() + " In: "+reachingDefinition.getIn().get(entry.getValue()));
                // String functionName = ctInvocation.prettyprint().split("\\(")[0];
               // functionUseDef.get(c).put(functionName, 1);
                reachingDefinition.getIn().get(entry.getValue())
                        .stream()
                        .filter(def -> def.startsWith(varName) || arguments.stream().anyMatch(arg -> arg.contains(def.split("\\:")[0])))
                        .collect(Collectors.toList())
                        .forEach(def -> {
                            ControlFlowNode cfNode = getControlFlowNodeFromDefinition(def);
                            List<CtInvocation> ctInvocationSet = getCtInvocation(cfNode);
                            processNode(c, ctInvocationSet);
                        });
                 **/
            }
        }
    }

    private void processNodeChain( ControlFlowNode c, Map.Entry<String,
            ControlFlowNode> entry, List<CtInvocation> set){
        List<CtExpression> expressions = set.stream()
                .map(ctInvocation -> (List<CtExpression>)ctInvocation.getArguments())
                .flatMap(list -> list.stream()).collect(Collectors.toList());

        List<String> arguments = expressions
                .stream().map(exp -> exp.toString())
                .collect(Collectors.toList());

        List<String> filterList =  reachingDefinition.getIn().get(entry.getValue())
                .stream()
                .filter(def ->  arguments.stream()
                        .anyMatch(arg -> arg.contains(def.split("\\:")[0]))
                )
                .collect(Collectors.toList());
        if(filterList.isEmpty())
            return;
        filterList.forEach(def -> {
            ControlFlowNode cfNode = getControlFlowNodeFromDefinition(def);
            List<CtInvocation> ctInvocationSet = getCtInvocation(cfNode);
            processNode(c, ctInvocationSet);
            processNodeChain( c, entry, ctInvocationSet);
        });
    }

    private String printSet(Set<String> set){
        return set.stream().collect(Collectors.joining(",","{","}"));
    }
    public void prettyPrint(){
        System.out.println("useDefinitionChain: "+ useDefinitionChain);
        //System.out.println("functionSet: "+ functionSet);

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

    public void printFunctionUseDef(){
        String format = "   ";
        Set<ControlFlowNode> controlFlowNodes = reachingDefinition.getGraph().vertexSet();
        String functions = functionSet.stream().collect(Collectors.joining(format));
        functions = "Node"+format+functions;
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.println(functions);
        System.out.println();
        System.out.println("---------------------------------------------------------------------------------------------");
        controlFlowNodes.forEach(b -> {
            Map<String, Integer> innerMap = functionUseDef.get(b);
            String line[] = {"" + b.getId()};
            for (Map.Entry<String, Integer> entry : innerMap.entrySet()) {
                Integer value = entry.getValue();
                line[0] = line[0] + ";   " + value;
            }
            System.out.println(line[0]);
            System.out.println();
        });
    }


    public void printDataset(){
        List<String> functionList = new ArrayList();
        functionList.add("Node");
        functionList.addAll(functionSet);
        System.out.println("functionSet: "+ functionList);
        Set<ControlFlowNode> controlFlowNodes = reachingDefinition.getGraph().vertexSet();
        String format = functionList.stream().map(el -> "%5s").collect(Collectors.joining(", "));
        //format = format + ", %5s";
        //prints the list objects in tabular format
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.printf(format, functionList.toArray());
        String[] formatStr = {functionList.stream().map(el -> "%8s").collect(Collectors.joining(", "))};
        //System.out.println(functions);
        System.out.println();
        System.out.println("---------------------------------------------------------------------------------------------");
        controlFlowNodes.forEach(b -> {
            Map<String, Integer> innerMap = functionUseDef.get(b);
            List<Integer> list = new ArrayList();
            list.add(b.getId());
            list.addAll(innerMap.values());
            System.out.printf(formatStr[0], list.toArray());
            System.out.println();
        });

        System.out.println("----------------------------------------------------------------------------------------------");
    }

}
