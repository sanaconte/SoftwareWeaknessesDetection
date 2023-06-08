package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation;

import fr.inria.controlflow.ControlFlowNode;
import fr.inria.spoon.dataflow.warning.Warning;
import org.apache.commons.collections4.list.TreeList;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.utils.WriteCsvFileUtils;
import spoon.Launcher;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtVariableReference;

import java.util.*;
import java.util.stream.Collectors;

public class AdaptedUseDefinitionChain {

    private static final String VULNERABLE = "VULNERABLE";

    private ReachingDefinition reachingDefinition;
    private Map<String, Set<String>> useDefinitionChain;
    private Map<ControlFlowNode, Map<String, Integer>> functionUseDef;
    private Set<String> functionSet;


    private Launcher launcher = new Launcher();
    //private CheckersScanner scanner = new CheckersScanner(launcher.getFactory());
    private Map<ControlFlowNode, Integer> vulnerabilityMap;
    private Map<ControlFlowNode, Integer> taintedLines = new HashMap<>();

    private List<Integer> vulnerableLines;

    public AdaptedUseDefinitionChain(ReachingDefinition reaching,
                                     List<Integer> vulnerableLines,
                                     Set<String> features){
        this.vulnerableLines = vulnerableLines;
        reachingDefinition = reaching;
        vulnerabilityMap = new HashMap();
        useDefinitionChain = new HashMap();
        functionSet = features;
        fillUseDefinitionChain();
        fillFunctionUseDefEmpty();
        fillFunctionUseDefinition();
    }

    private void getVulnerablePosition(){
        //System.out.println("getVulnerablePosition: vulnerableLines "+ vulnerableLines);
        List<ControlFlowNode> list =
                reachingDefinition.getGraph()
                        .vertexSet()
                        .stream()
                        .filter(node -> node.getStatement() != null)
                        .peek(node -> System.out.println("FileName: "+node.getStatement().getPosition().getFile().getName()))
                        .filter(node -> vulnerableLines.contains(node.getStatement().getPosition().getLine()) /*vulnerableLines.stream().anyMatch(any -> any==node.getStatement().getPosition().getLine())*/)
                        .collect(Collectors.toList());
        //.findFirst().orElse(null);
        list.forEach(controlFlowNode -> {
            if(controlFlowNode != null)
                vulnerabilityMap.put(controlFlowNode, 1);
        });

    }

    private ControlFlowNode findControlFlowNodeByCtElementPosition(Warning warning){
        return reachingDefinition.getGraph()
                .vertexSet()
                .stream()
                .filter(node -> node.getStatement() != null)
                .filter(node ->
                        node.getStatement().getPosition().getLine() == warning.position.getLine())
                .findFirst().get();
    }

    private void fillTaintedLines(List<CtExpression<?>> arguments, ControlFlowNode c){
        arguments.stream().forEach(e ->{
            System.out.println("(e instanceof CtAssignment): "+ (e instanceof CtAssignment));
            System.out.println("(e instanceof CtConditional): "+ (e instanceof CtConditional));
            System.out.println("(e instanceof CtBinaryOperator): "+ (e instanceof CtBinaryOperator));
            System.out.println("(e instanceof CtInvocation): "+ (e instanceof CtInvocation));
            System.out.println("(e instanceof CtField): "+ (e instanceof CtField));
            System.out.println("(e instanceof CtVariableAccess): "+ (e instanceof CtVariableAccess));
            System.out.println("(e instanceof CtVariable): "+ (e instanceof CtVariable));
            System.out.println("(e instanceof CtVariableRead): "+ (e instanceof CtVariableRead));
            System.out.println("(e instanceof CtVariableWrite): "+ (e instanceof CtVariableWrite));
            System.out.println("(e instanceof CtVariableReference): "+ (e instanceof CtVariableReference));
            System.out.println("(e instanceof CtLiteral): "+ (e instanceof CtLiteral));
            if(e instanceof CtVariableAccess){
                System.out.println("is CtVariableAccess");
                boolean isTainted = isTainted(e);
                System.out.println("isTainted: "+isTainted);
                if(isTainted) {
                    taintedLines.put(c, 1);
                }else{
                    taintedLines.put(c, 0);
                }
            }
            else if(e instanceof CtBinaryOperator){
                System.out.println("is CtBinaryOperator");
                CtBinaryOperator binaryOperator = (CtBinaryOperator)e;
                boolean isTainted = isTaintedCtBinaryOperator(binaryOperator);
                System.out.println("isTainted: "+isTainted);
                if(isTainted) {
                    taintedLines.put(c, 1);
                }else{
                    taintedLines.put(c, 0);
                }
            }
            else {
                taintedLines.put(c, 0);
            }
        });
    }

    private boolean isTaintedCtBinaryOperator(CtBinaryOperator binaryOperator){
        CtExpression<?> leftHandOperand = binaryOperator.getLeftHandOperand();
        CtExpression<?> rightHandOperand = binaryOperator.getRightHandOperand();
        if(leftHandOperand instanceof CtVariableAccess &&
                rightHandOperand instanceof CtVariableAccess) {
            boolean isLeftHandOperandTainted = isTainted(binaryOperator.getLeftHandOperand());
            System.out.println("isLeftHandOperandTainted: " + isLeftHandOperandTainted);

            boolean isRightHandOperandTainted = isTainted(binaryOperator.getRightHandOperand());
            System.out.println("isRightHandOperandTainted: " + isRightHandOperandTainted);
            return isRightHandOperandTainted || isLeftHandOperandTainted;
        }
        else if(rightHandOperand instanceof CtVariableAccess &&
                !(leftHandOperand instanceof CtVariableAccess)) {
            boolean isRightHandOperandTainted = isTainted(binaryOperator.getRightHandOperand());
            System.out.println("isRightHandOperandTainted: " + isRightHandOperandTainted);
            return isRightHandOperandTainted;
        }
        else if(!(rightHandOperand instanceof CtVariableAccess) &&
                (leftHandOperand instanceof CtVariableAccess)) {
            boolean isLeftHandOperandTainted = isTainted(binaryOperator.getLeftHandOperand());
            System.out.println("isLeftHandOperandTainted: " + isLeftHandOperandTainted);
            return isLeftHandOperandTainted;
        }
        else {
            System.out.println("The both operand are not CtVariableAccess");
            return false;
        }
    }

    /*
        É considerado tainted se não for Constante
        e não for do tipo primitivo
        e se o valor não for um literal.
        ou se tiver valor com expressao BinaryOperator tainted
    */
    private boolean isTainted(CtExpression<?> e){
        CtVariableAccess ctVariableAccess = (CtVariableAccess) e;
        boolean isConstant =
                ctVariableAccess.getVariable().getDeclaration().isFinal();
        boolean isPrimitive = ctVariableAccess.getVariable().getDeclaration().getType().isPrimitive();
        boolean isLiteral =
                ctVariableAccess.getVariable().getDeclaration().getDefaultExpression() instanceof CtLiteral;

        // verificar se o valor da varivel não é um BinaryOperator com possíveis dados tainted
        boolean isBinaryOperator =  ctVariableAccess
                .getVariable().getDeclaration()
                .getDefaultExpression() instanceof CtBinaryOperator;
        boolean isTaintedCtBinaryOperator = false;
        if(isBinaryOperator){
            CtBinaryOperator binaryOperator = (CtBinaryOperator)
                    ctVariableAccess
                    .getVariable().getDeclaration()
                    .getDefaultExpression();
            isTaintedCtBinaryOperator = isTaintedCtBinaryOperator(binaryOperator);
        }

        System.out.println("isConstant: "+isConstant);
        System.out.println("isPrimitive: "+isPrimitive);
        System.out.println("isLiteral: "+isLiteral);
        System.out.println("isTaintedCtBinaryOperator: "+isTaintedCtBinaryOperator);

        return (!isConstant && !isPrimitive && !isLiteral) || isTaintedCtBinaryOperator;
    }

    private CtInvocation getFunctionInvoked(String funcName, ControlFlowNode c){
        String[] split = funcName.split("\\.");
        String method;
        if(split.length>0) {
            method = split[split.length-1];
        }else{
            method = funcName;
        }
        CtInvocation ctInvocation = c.getStatement()
                .getElements(el -> el instanceof CtInvocation)
                .stream().map(el -> (CtInvocation) el)
                .filter(el -> el.getExecutable().getSimpleName().equals(method))
                .findAny()
                .orElse(null);

        return ctInvocation;
    }

    private void fillFunctionUseDefEmpty(){
        functionUseDef = new HashMap( );
        reachingDefinition.getGraph().vertexSet().forEach(c -> {
            Map<String, Integer> innerMap = new HashMap();
            taintedLines.put(c, 0);
            vulnerabilityMap.put(c, 0);
            //List<CtInvocation> ctInvocationSet = getCtInvocation(c);
            functionSet.forEach(funcName -> {
                innerMap.put(funcName, 0);
                if(c.getStatement() != null &&
                        c.getStatement().prettyprint().contains(funcName)) {
                    System.out.println("funcName: "+funcName);
                    CtInvocation ctInvocation = getFunctionInvoked(funcName, c);
                    List<CtExpression<?>> arguments = ctInvocation.getArguments();
                    fillTaintedLines(arguments, c);
                    innerMap.put(funcName, 1);
                    // potencialmente vulneravel todos os sítios onde foram invocados as funções presente na lista de  caracteristicas
                    vulnerabilityMap.put(c, 1);
                }
            });
            //vulnerabilityMap.put(c, 0);
            functionUseDef.put(c, innerMap);
        });
        //getVulnerablePosition();
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

        // para remoção de todos os comentários.
        ctInvocation.setComments(new ArrayList());
        String functionName = ctInvocation.prettyprint().split("\\(")[0];
        if(!functionName.isBlank()) {
            //functionSet.add(functionName);
            reachingDefinition.getLocalVariables()
                    .stream().filter(var -> arguments.stream().anyMatch(a -> a.contains(var)))
                    .forEach(var -> {
                        useSet.put(var + ":" + c.getId(), c);
                    });
        }

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
                //functionUseDef.get(c).put(functionName, 1);
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

    public void printFunctionUseDef(String filePath, String projectName){
        List<String[]> data = new TreeList<>();
        String format = "   ";
        Set<ControlFlowNode> controlFlowNodes = reachingDefinition.getGraph().vertexSet();
        String functions = functionSet.stream().collect(Collectors.joining(format));
        functions = "PROJECT_NAME"+format+"Node"+format+ VULNERABLE +format+functions;
        // functions = functions+format+NULL_POINTER_VUL;
        String[] header = functions.split(format);
        data.add(header);
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.println(functions);
        System.out.println();
        System.out.println("---------------------------------------------------------------------------------------------");
        controlFlowNodes.forEach(b -> {
            Map<String, Integer> innerMap = functionUseDef.get(b);
            //System.out.println("innerMap: "+innerMap);
            Integer vulValue = vulnerabilityMap.get(b);
            String form = ";   ";
            String line[] = {projectName+form+b.getId()+form+vulValue};
            for (Map.Entry<String, Integer> entry : innerMap.entrySet()) {
                Integer value = entry.getValue();
                line[0] = line[0]+form+value;
            }
            //line[0] = line[0]+form+vulValue;
            data.add(line[0].split(form));
            System.out.println(line[0]);
            System.out.println();
        });
        WriteCsvFileUtils.writeData(filePath, data);
    }

    public List<String> transformFile(String fileName){
        List<String[]> data = new TreeList<>();
        String format = "   ";
        Set<ControlFlowNode> controlFlowNodes = reachingDefinition.getGraph().vertexSet();
        String functions = functionSet.stream().collect(Collectors.joining(format));
        functions = "FILE_NAME"+format+"LINE_NUMBER"+format+"NODE"+format+ VULNERABLE+format+"TAINTED"+format+functions;
        // functions = functions+format+NULL_POINTER_VUL;
        String[] header = functions.split(format);
        data.add(header);
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.println(functions);
        System.out.println();
        System.out.println("---------------------------------------------------------------------------------------------");

        for(ControlFlowNode flowNode:controlFlowNodes){
            // ignorar nós sem instruções
            if(flowNode.getStatement() == null){
                continue;
            }
            Map<String, Integer> innerMap = functionUseDef.get(flowNode);
            Integer vulValue = vulnerabilityMap.get(flowNode);
            Integer tainted = taintedLines.get(flowNode);
            String form = ";   ";
            int lineNumber = flowNode.getStatement().getPosition().getLine();
            String line[] = {fileName+form+lineNumber+form+flowNode.getId()+form+vulValue+form+tainted};
            for (Map.Entry<String, Integer> entry : innerMap.entrySet()) {
                Integer value = entry.getValue();
                line[0] = line[0]+form+value;
            }
            //line[0] = line[0]+form+vulValue;
            data.add(line[0].split(form));
            System.out.println(line[0]);
            System.out.println();
        }
        return data.stream()
                .map(line -> Arrays.stream(line).collect(Collectors.joining(",")))
                .collect(Collectors.toList());
    }

    public void printDataset(){
        List<String> functionList = new ArrayList();
        functionList.add("Node");
        functionList.add("Vulnerable");
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
            list.add(vulnerabilityMap.get(b));
            list.addAll(innerMap.values());
            System.out.printf(formatStr[0], list.toArray());
            System.out.println();
        });

        System.out.println("----------------------------------------------------------------------------------------------");
    }
}
