package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation;

import fr.inria.controlflow.BranchKind;
import fr.inria.controlflow.ControlFlowNode;
import org.apache.commons.collections4.list.TreeList;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtVariableReference;
import spoon.support.reflect.code.CtLocalVariableImpl;

import java.util.*;
import java.util.stream.Collectors;



public class FeaturesExtraction {

    private static final String VULNERABLE = "VULNERABLE";
    private final UseDefinition useDefinition;

    private ReachingDefinition reachingDefinition;

    private Map<ControlFlowNode, String> funcFeature;
    private Map<ControlFlowNode, String> varFeature;
    private Map<ControlFlowNode, Integer> classeValues;
    private String fileName;
    private Map<String, List<Integer>> vulnerabilityMap;

    private List<ControlFlowNode> extractedNodes;

    public FeaturesExtraction(ReachingDefinition reachingDefinition, UseDefinition useDefinition, String fileName, Map<String, List<Integer>> vulnerabilityMap) {
        this.reachingDefinition = reachingDefinition;
        this.useDefinition = useDefinition;
        this.fileName = fileName;
        this.vulnerabilityMap = vulnerabilityMap;
        initiateLists();
        //executeExtraction();
    }

    public void executeExtraction(){
        makeExtraction();
    }

    private void initiateLists(){
        funcFeature = new HashMap<>();
        varFeature = new HashMap<>();
        classeValues = new HashMap<>();
        extractedNodes = new ArrayList<>();
    }

    private  void makeExtraction(){
//        useDefinition.getUseFunctionNodes()
//       // reachingDefinition.getGraph()
//         //       .statements()
//                .stream()
//                .filter(node -> isInstanceOfCtInvocation(node))
//                .forEach(node -> forEachNode(node));
        //System.out.println(useDefinition.getFunctionUseDefinition());
        useDefinition
                .getFunctionUseDefinition()
                .forEach((key, value) -> forEachInvocation(key, value));
    }

    private void forEachNode(ControlFlowNode node) {
        CtElement element = getFunctionInvoked(node);
        if(element instanceof CtInvocation) {
            CtInvocation ctInvocation = (CtInvocation) element;
            String funcName = ctInvocation.getExecutable().getSimpleName() + "()";
            List<CtExpression<?>> arguments = ctInvocation.getArguments();
            processNode(node, funcName, arguments);
        }
        else if(element instanceof CtConstructorCall){
            CtConstructorCall ctConstructorCall = (CtConstructorCall) element;
            String funcName = ctConstructorCall.getExecutable().prettyprint().split("\\(")[0]+"()";
            List<CtExpression<?>> arguments = ctConstructorCall.getArguments();
            processNode(node, funcName, arguments);
        }
    }

    private void forEachInvocation(ControlFlowNode key, CtElement value){
        CtElement element = value;
        if(element instanceof CtInvocation) {
            CtInvocation ctInvocation = (CtInvocation) element;
            String funcName = ctInvocation.getExecutable().getSimpleName() + "()";
            List<CtExpression<?>> arguments = ctInvocation.getArguments();
            processNode(key, funcName, arguments);
        }
        else if(element instanceof CtConstructorCall){
            CtConstructorCall ctConstructorCall = (CtConstructorCall) element;
            String funcName = ctConstructorCall.getExecutable().prettyprint().split("\\(")[0]+"()";
            List<CtExpression<?>> arguments = ctConstructorCall.getArguments();
            processNode(key, funcName, arguments);
        }
    }

    private void processNode(ControlFlowNode node, String funcName, List<CtExpression<?>> arguments) {
        if (!arguments.isEmpty()) {
            String valueArguments = arguments
                    .stream()
                    .map(expression -> treatExpression(expression))
                    .collect(Collectors.joining(" "));
            valueArguments = valueArguments.replace(",", "");
            funcFeature.put(node, funcName);
            varFeature.put(node, valueArguments);
            extractedNodes.add(node);
            processVulnerability(node);
        }
    }

    private void processVulnerability(ControlFlowNode node) {
        SourcePosition position = node.getStatement().getPosition();
        System.out.println("fileName: "+ node.getStatement().getPosition().getFile().getName());
        System.out.println("vulnerabilityMap: "+vulnerabilityMap);
        List<Integer> lines = vulnerabilityMap.get(position.getFile().getName());
        boolean contains = lines != null && lines.contains(position.getLine());
//        boolean anyMatch = vulnerabilityMap
//                .entrySet()
//                .stream()
//                .anyMatch(entry ->
//                        entry.getKey().equals(position.getFile().getName()) &&
//                                entry.getValue() == position.getLine());
        if(contains){
            classeValues.put(node, 1);
        }else {
            classeValues.put(node, 0);
        }
    }

    private String treatExpression(CtExpression<?> expression) {
        if (expression instanceof CtLiteral) {
            return getLiteralValue((CtLiteral) expression);
        } else if (expression instanceof CtVariableAccess) {
            return getVariableOrigenValue((CtVariableAccess) expression);
        } else  if(expression instanceof CtBinaryOperator){
            return treatCtBinaryOperator((CtBinaryOperator) expression);
        }
        else if(expression instanceof CtInvocation){
            return treatCtInvocation((CtInvocation)expression);
        }
        else if(expression instanceof CtConstructorCall){
            return treatCtConstructorCall((CtConstructorCall)expression);
        }else if(expression instanceof  CtAssignment){
            CtAssignment ctAssignment = (CtAssignment) expression;
            return treatExpression(ctAssignment.getAssignment());
        }
        else {
            return "";
        }
    }

    private String treatCtInvocation(CtInvocation ctInvocation){
       return ctInvocation.getExecutable().getSimpleName()+"()";
       //return ctInvocation.prettyprint();
    }

    private String treatCtConstructorCall(CtConstructorCall ctConstructorCall){
        return ctConstructorCall.prettyprint();
    }

    private String treatCtBinaryOperator(CtBinaryOperator binaryOperator){
        CtExpression<?> leftHandOperand = binaryOperator.getLeftHandOperand();
        CtExpression<?> rightHandOperand = binaryOperator.getRightHandOperand();

        String left = treatExpression(leftHandOperand);
        String  right = treatExpression(rightHandOperand);
        boolean isLeft = left!=null&&!left.isBlank();
        boolean isRight = right!=null&&!right.isBlank();
        if(isLeft&&isRight){
            return left+" "+right;
        }
        else if(isLeft&& !isRight){
            return left;
        }else if(!isLeft&&isRight){
            return right;
        }else {
            return "";
        }

    }

    private ControlFlowNode getNodeFromDefinition(String def){
        int id = Integer.parseInt(def.split(":")[1]);
        return reachingDefinition.getGraph().findNodeById(id);
    }

    private String getVariableOrigenValue(CtVariableAccess ctVariableAccess){
        //return ""+ctVariableAccess.getVariable().prettyprint();
        String varName = ctVariableAccess.getVariable().getSimpleName();
        OptionalInt max = useDefinition
                .getUseDefinitionChain()
                .keySet()
                .stream().filter(use -> use.split(":")[0].equals(varName))
                .flatMap(use -> useDefinition.getUseDefinitionChain().get(use).stream())
                .mapToInt(def -> Integer.parseInt(def.split(":")[1]))
                .min();

        if(!max.isEmpty()){
            ControlFlowNode nodeById = reachingDefinition.getGraph().findNodeById(max.getAsInt());
            if(nodeById!= null && nodeById.getStatement()!=null){
                CtElement ctElement = nodeById.getStatement();
                return treatCtElement(ctElement);
                //return treatExpression((CtExpression<?>) nodeById.getStatement());
                //return ctAssignment.getAssignment().prettyprint();
                //return nodeById.getStatement().prettyprint();
            }
        }
        return   "";
    }

    private String treatCtElement(CtElement ctElement){
        if(ctElement instanceof CtAssignment){
            CtAssignment ctAssignment = (CtAssignment) ctElement;
            if(ctAssignment.getAssignment() instanceof CtInvocation)
                return treatCtInvocation((CtInvocation)ctAssignment.getAssignment());
            return  ctAssignment.getAssignment().prettyprint();
        }else if(ctElement instanceof CtLocalVariableImpl){
            CtLocalVariableImpl ctLocalVariable = (CtLocalVariableImpl) ctElement;
            if(ctLocalVariable.getAssignment() instanceof CtInvocation)
                return treatCtInvocation((CtInvocation)ctLocalVariable.getAssignment());
            return ctLocalVariable.getAssignment().prettyprint();
        }else if(ctElement instanceof CtLocalVariable){
            CtLocalVariable ctLocalVariable = (CtLocalVariable) ctElement;
            if(ctLocalVariable.getAssignment() instanceof CtInvocation)
                return treatCtInvocation((CtInvocation)ctLocalVariable.getAssignment());
            return ctLocalVariable.getAssignment().prettyprint();
        }
        return ctElement.prettyprint();
    }

    private String getLiteralValue(CtLiteral literal){
        return literal.prettyprint();
    }

    private CtElement getFunctionInvoked(ControlFlowNode node) {
        if(node instanceof CtInvocation) {
            return  node.getStatement();
        }else{
            List<CtElement> list = node.getStatement()
                    .getElements(e -> e instanceof CtInvocation ||  e instanceof CtConstructorCall);
            //System.out.println("list: "+list);
            return !list.isEmpty() ? list.get(0) : null;
        }
    }

    private boolean isInstanceOfCtInvocation(ControlFlowNode node) {
        CtElement element = node.getStatement();
        return element != null
                && (element instanceof CtInvocation ||
                !element.getElements(e -> e instanceof CtInvocation || e instanceof CtConstructorCall).isEmpty());
    }



    public List<String> getResultExtraction(){
        List<String[]> data = new TreeList<>();
        String format = "   ";

        String haed = "FILE_NAME"+format+"LINE_NUMBER"+format+"NODE"+format+ "Func"+format+"Var"+format+VULNERABLE;
        String[] header = haed.split(format);
        data.add(header);
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.println(haed);
        System.out.println();
        System.out.println("---------------------------------------------------------------------------------------------");

        for(ControlFlowNode flowNode:extractedNodes){
            //Map<String, Integer> innerMap = functionUseDef.get(flowNode);
            Integer vulValue = classeValues.get(flowNode);
            String funcValue = funcFeature.get(flowNode);
            String varValue = varFeature.get(flowNode);

            String form = ";   ";
            int lineNumber = flowNode.getStatement().getPosition().getLine();
            String line[] = {fileName+form+lineNumber+form+flowNode.getId()+form+funcValue+form+varValue+form+vulValue};
            //line[0] = line[0]+form+vulValue;
            data.add(line[0].split(form));
            System.out.println(line[0]);
            System.out.println();
        }
        return data.stream()
                .map(line -> Arrays.stream(line).collect(Collectors.joining(",")))
                .collect(Collectors.toList());
    }
}
