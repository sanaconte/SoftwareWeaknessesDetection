package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation;

import fr.inria.controlflow.ControlFlowNode;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtVariableReference;

import java.util.*;
import java.util.stream.Collectors;

public class UseDefinition {

    /*
        key -> definition x
        value -> use definition chain para definition x
     */
    private Map<String, Set<String>> useDefinitionChain;
    private ReachingDefinition reachingDefinition;

    /**
     * key - nós onde foi invocado a função/construtor
     * value -> função/contrutor invocado
     */
    Map<ControlFlowNode, CtElement> functionUseDefinition;

    public UseDefinition(ReachingDefinition reachingDefinition) {
        this.reachingDefinition = reachingDefinition;
        this.functionUseDefinition = new HashMap<>();
        computeUseDefinitionChain();
    }

    public Map<String, Set<String>> getUseDefinitionChain() {
        return useDefinitionChain;
    }

    public Map<ControlFlowNode, CtElement> getFunctionUseDefinition() {
        return functionUseDefinition;
    }

    private void addFunctionUseDefinition(CtElement value, ControlFlowNode key){
        functionUseDefinition.put(key, value);
    }

    public List<ControlFlowNode> getUseFunctionNodes(){
        return useDefinitionChain
                .keySet()
                .stream()
                .map(key -> Integer.parseInt(key.split(":")[1]))
                .map(id -> reachingDefinition.getGraph().findNodeById(id))
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> getValueFromExpression(CtExpression<?> expression){
        if (expression instanceof CtLiteral) {
            return List.of(getLiteralValue((CtLiteral) expression));
        } else if (expression instanceof CtVariableAccess) {
            return List.of(getVariableName((CtVariableAccess) expression));
        } else  if(expression instanceof CtBinaryOperator){
            return treatCtBinaryOperator((CtBinaryOperator) expression);
        }
        else {
            return  List.of(expression.toString());
        }
    }

    private String getLiteralValue(CtLiteral literal){
        return literal.prettyprint();
    }

    private String getVariableName(CtVariableAccess ctVariableAccess){
        CtVariableReference variable = ctVariableAccess.getVariable();
        return variable.getSimpleName();
    }

    private List<String> treatCtBinaryOperator(CtBinaryOperator binaryOperator){
        CtExpression<?> leftHandOperand = binaryOperator.getLeftHandOperand();
        CtExpression<?> rightHandOperand = binaryOperator.getRightHandOperand();

        List<String> right = getValueFromExpression(rightHandOperand);
        List<String> left = getValueFromExpression(leftHandOperand);
        List<String> toReturn = new ArrayList<>();
        toReturn.addAll(right);
        toReturn.addAll(left);
        return toReturn;
    }

    private void treatCtInvocation(CtInvocation ctInvocation, ReachingDefinition reachingDefinition, Map<String, ControlFlowNode> useSet, ControlFlowNode c) {
        List<CtExpression> expressions = (List<CtExpression>)ctInvocation.getArguments();
        List<String> arguments = expressions
                .stream()
                .flatMap(exp -> getValueFromExpression(exp).stream())
                .collect(Collectors.toList());
        if(arguments.isEmpty()){
            return;
        }

        //functionUseDefinition.put(c, ctInvocation);
        // para remoção de todos os comentários.
        ctInvocation.setComments(new ArrayList());
        String functionName = ctInvocation.getExecutable().getSimpleName();
        if(!functionName.isBlank()) {
            //functionSet.add(functionName); -> TODO
            List<String> anyMatchList = reachingDefinition.getLocalVariables()
                    .stream().filter(var -> arguments.stream().anyMatch(a -> var.equals(a)))
                    .collect(Collectors.toList());
            anyMatchList.forEach(var -> {
                        useSet.put(var + ":" + c.getId(), c);
            });
            if(!anyMatchList.isEmpty()){
                addFunctionUseDefinition(ctInvocation, c);
            }
        }
    }

    private void treatCtConstructorCall(CtConstructorCall ctInvocation, ReachingDefinition reachingDefinition, Map<String, ControlFlowNode> useSet, ControlFlowNode c ){
        List<CtExpression> expressions = (List<CtExpression>)ctInvocation.getArguments();
        List<String> arguments = expressions.stream()
                .flatMap(exp -> getValueFromExpression(exp).stream())
                .collect(Collectors.toList());

        if(arguments.isEmpty()){
            return;
        }

        // para remoção de todos os comentários.
        ctInvocation.setComments(new ArrayList());
        String functionName = ctInvocation.getExecutable().getSimpleName();
        if(!functionName.isBlank()) {
            //functionSet.add(functionName); -> TODO
            List<String> anyMatchList = reachingDefinition.getLocalVariables()
                    .stream().filter(var -> arguments.stream().anyMatch(a -> var.equals(a)))
                    .collect(Collectors.toList());
            anyMatchList.forEach(var -> {
                        useSet.put(var + ":" + c.getId(), c);
                    });

            if(!anyMatchList.isEmpty()){
                addFunctionUseDefinition(ctInvocation, c);
            }
        }
    }

    private Map<String, ControlFlowNode> getUseSet(){
        Map<String, ControlFlowNode> useSet = new HashMap();
        Set<ControlFlowNode> controlFlowNodes = reachingDefinition.getGraph().vertexSet();
        controlFlowNodes.forEach(c -> {
            computeNode(useSet, c, c.getStatement());
        });
        return useSet;
    }

    private void computeNode(Map<String, ControlFlowNode> useSet, ControlFlowNode c, CtElement principalElement) {
        if(principalElement == null){
            return;
        }
        if(principalElement instanceof CtLocalVariable){
            CtLocalVariable ctLocalVariable = (CtLocalVariable) principalElement;
            boolean isCtInvocation = ctLocalVariable.getAssignment() instanceof CtInvocation;
            boolean isCtConstructorCall = ctLocalVariable.getAssignment() instanceof CtConstructorCall;
            if(isCtInvocation){
                treatCtInvocation((CtInvocation) ctLocalVariable.getAssignment(), reachingDefinition, useSet, c);
            }
            if(isCtConstructorCall){
                treatCtConstructorCall((CtConstructorCall) ctLocalVariable.getAssignment(), reachingDefinition, useSet, c);
            }
            if(ctLocalVariable.getAssignment() != null){
                treatCtAssignment(ctLocalVariable.getAssignment(), reachingDefinition, useSet, c);
            }
        }
        else if(principalElement instanceof CtAssignment) {
            CtAssignment ctAssignment = (CtAssignment) principalElement;
            boolean is = principalElement instanceof CtInvocation;
            if(is){
                treatCtInvocation((CtInvocation) ctAssignment.getAssignment(), reachingDefinition, useSet, c);
            }
            boolean isCtConstructorCall = principalElement instanceof CtConstructorCall;
            if(isCtConstructorCall){
                treatCtConstructorCall((CtConstructorCall)principalElement, reachingDefinition, useSet, c);
            }
            treatCtAssignment(ctAssignment, reachingDefinition, useSet, c);
        }
        // para considerar a presenca de invocação das funçoes que usam variveis
        else if(principalElement instanceof CtInvocation) {
            treatCtInvocation((CtInvocation) principalElement, reachingDefinition, useSet, c);
        }
        else if(principalElement instanceof CtConstructorCall){
            CtConstructorCall ctConstructorCall = (CtConstructorCall) principalElement;
            treatCtConstructorCall(ctConstructorCall, reachingDefinition, useSet, c);
        }
        // considerar os Nodes que têm vários elementos ctAssignment
        else {
            List<CtElement> elements = principalElement.getElements(ele -> ele instanceof CtLocalVariable ||
                    ele instanceof CtAssignment || ele instanceof CtInvocation || ele instanceof CtConstructorCall);
            if(!elements.isEmpty()) {
                elements.forEach(ctElement -> {
                    computeNode(useSet, c, ctElement);
                });
            }
        }
    }

    private void treatCtAssignment(CtElement ctAssignment, ReachingDefinition reachingDefinition, Map<String, ControlFlowNode> useSet, ControlFlowNode c) {
        List<String> variableAccesses = ctAssignment.getElements(e -> e instanceof CtVariableRead)
                .stream()
                .map(e -> (CtVariableAccess) e)
                .map(e -> e.getVariable().getSimpleName())
                .collect(Collectors.toList());
        //String expression = ctLocalVariable.getAssignment().toString();
        reachingDefinition.getLocalVariables()
                .stream().filter(var -> variableAccesses.stream().anyMatch(vac -> var.equals(vac)) /*expression.contains(var)*/)
                .forEach(var -> useSet.put(var+":"+ c.getId(), c));
    }



    private void computeUseDefinitionChain(){
        useDefinitionChain = new HashMap();
        Map<String, ControlFlowNode> mapUseSet = getUseSet();
        for(Map.Entry<String, ControlFlowNode> entry : mapUseSet.entrySet()){
            String varName = entry.getKey().split(":")[0];
            // System.out.println("varName: " + varName + " key: " +entry.getValue()+ " in: "+reachingDefinition.getIn().get(entry.getValue()));
            Set<String> udChains = reachingDefinition.getIn().get(entry.getValue())
                    .stream()
                    .filter(e -> e.split(":")[0].equals(varName))
                    .collect(Collectors.toSet());
            if(!udChains.isEmpty()) {
                useDefinitionChain.put(entry.getKey(), udChains);
            }
        }
    }

    private String printSet(Set<String> set){
        return set.stream().collect(Collectors.joining(",","{","}"));
    }

    public void printUseDefinitionChain(){
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
}
