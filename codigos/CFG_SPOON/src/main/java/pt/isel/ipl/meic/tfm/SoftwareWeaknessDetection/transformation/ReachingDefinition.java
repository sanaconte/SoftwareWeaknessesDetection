package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation;

import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.ControlFlowNode;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;

import java.util.*;
import java.util.stream.Collectors;

public class ReachingDefinition {

    private Map<ControlFlowNode, Set<String>> in ;
    private Map<ControlFlowNode, Set<String>> out;
    private Map<ControlFlowNode, Set<String>> gen;
    private Map<ControlFlowNode, Set<String>> kill;

    private Set<String> localVariables;
    private  ControlFlowGraph graph;

    private Set<CtVariable> variables = new HashSet<>();

    CtElement functionCtElement;


    public ReachingDefinition(ControlFlowGraph graph, CtElement functionCtElement){
        this.graph = graph;
        in = new HashMap();
        out = new HashMap();
        gen =  new HashMap();
        kill = new HashMap();
        this.functionCtElement = functionCtElement;
        fillLocalVariables();
        fillGen();
        fillKill();
        computeAlgorithm();
    }

    public Set<CtVariable> getVariables() {
        return variables;
    }

    public CtElement getFunctionCtElement() {
        return functionCtElement;
    }

    public ControlFlowGraph getGraph() {
        return graph;
    }

    public Set<String> getLocalVariables() {
        return localVariables;
    }

    public Map<ControlFlowNode, Set<String>> getIn() {
        return in;
    }

    public Map<ControlFlowNode, Set<String>> getOut() {
        return out;
    }

    private void fillLocalVariables(){
        Set<ControlFlowNode> controlFlowNodes = graph.vertexSet();
        localVariables =
                controlFlowNodes
                        .stream()
                        .filter(c -> c.getStatement() instanceof CtLocalVariable)
                        .map(c -> (CtLocalVariable)c.getStatement())
                        .map(c -> c.getReference().clone().toString())
                        .collect(Collectors.toSet());
        variables = controlFlowNodes
                .stream()
                .filter(c -> c.getStatement() instanceof CtVariable)
                .map(c -> (CtVariable)c.getStatement())
                .collect(Collectors.toSet());
    }

    private void fillGen(){
        Set<ControlFlowNode> controlFlowNodes = graph.vertexSet();
        controlFlowNodes.forEach(c -> {
            if(c.getStatement() instanceof CtLocalVariable){
                CtLocalVariable ctLocalVariable = (CtLocalVariable) c.getStatement();
                if(ctLocalVariable.getAssignment() != null){
                    String varName = ctLocalVariable.getReference().clone().toString();
                    addElement(varName, c);
                }else{
                    gen.put(c, new HashSet<>());
                }

            }
            else if(c.getStatement() instanceof CtAssignment){
                CtAssignment ctAssignment = (CtAssignment) c.getStatement();
                String varName = ctAssignment.getAssigned().toString();
                addElement(varName, c);
            }
            else if(c.getStatement() != null) {
                List<CtElement> els =  c.getStatement()
                        .getElements(el -> el instanceof CtAssignment);
                // considerar os Nodes que têm vários elementos ctAssignment
                if(!els.isEmpty()){
                    els.forEach(ctElement -> {
                        CtAssignment ctAssignment = (CtAssignment) ctElement;
                        String varName = ctAssignment.getAssigned().toString();
                        addElement(varName, c);
                    });
                }else{
                    gen.put(c, new HashSet<>());
                }
            }
            else{
                gen.put(c, new HashSet<>());
            }
        });
    }

    private void addElement(String varName, ControlFlowNode c) {
        Set<String> hash = new HashSet();
        hash.add(varName + ":" + c.getId());
        gen.put(c, hash);
    }

    private void fillKill(){
        List<String> list = gen.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
        for (Map.Entry<ControlFlowNode, Set<String>> entry : gen.entrySet()) {
            ControlFlowNode cont = entry.getKey();
            if(cont.getStatement() instanceof CtLocalVariable){
                CtLocalVariable ctLocalVariable = (CtLocalVariable)cont.getStatement();
                if(ctLocalVariable.getAssignment() != null){
                    String varName = ctLocalVariable.getReference().clone().toString();
                    List<String> listWithoutActual =
                            list.stream().filter(str -> !str.equals(varName+cont.getId())).collect(Collectors.toList());
                    Set<String> kills = getKills(listWithoutActual, varName);
                    kill.put(entry.getKey(), kills);
                }else{
                    kill.put(cont, new HashSet());
                }


            }else if(cont.getStatement() instanceof CtAssignment){
                CtAssignment ctAssignment = (CtAssignment)cont.getStatement();
                String varName = ctAssignment.getAssigned().toString();
                List<String> listWithoutActual =
                        list.stream().filter(str -> !str.equals(varName+cont.getId())).collect(Collectors.toList());
                Set<String> kills = getKills(listWithoutActual, varName);
                kill.put(cont, kills);
            }
            else{
                kill.put(cont, new HashSet());
            }
        }
    }

    private static Set<String> getKills(List<String> gen, String varName){
        return  gen.stream().filter(m -> m.startsWith(varName)).collect(Collectors.toSet());
    }

    private void computeAlgorithm(){
        Set<ControlFlowNode> controlFlowNodes = this.graph.vertexSet();
        //preencher in a vazio e out com gen
        controlFlowNodes.forEach(b -> {
            in.put(b, new HashSet());
            out.put(b, gen.get(b));
        });

        boolean[] change = {true};
        while(change[0]) {
            change[0] = false;
            controlFlowNodes.forEach(b -> {

                //get predecessors of B OUT[p];
                // IN[B] = ∪p a predecessor of B OUT[p];
                List<ControlFlowNode> prev = b.prev();
                Set<String> predecessors =
                        prev.stream().map(c -> out.get(c)).flatMap(set -> set.stream()).collect(Collectors.toSet());

                //IN[b] = ∪p a predecessor of B OUT[p];
                in.put(b, predecessors);

                Set<String> oldout = out.get(b);
                Set<String> genList = gen.get(b);
                Set<String> inList = in.get(b);
                Set<String> killList = kill.get(b);

                //IN[b] ─ KILL[b]
                Set<String> inMinusKill =
                        inList.stream().filter(el -> !killList.contains(el)).collect(Collectors.toSet());
                inMinusKill.addAll(genList);

                //OUT[B]= GEN[B]∪ (IN[B] ─ KILL[B]);
                out.put(b, inMinusKill);
                Set<String> outList = out.get(b);
                if(!outList.equals(oldout)){
                    change[0] = true;
                }
            });
        }
    }

    private String printSet(Set<String> set){
        return set.stream().collect(Collectors.joining(",","{","}"));
    }

    public void prettyPrint(){
        Set<ControlFlowNode> controlFlowNodes = this.graph.vertexSet();
        //prints the list objects in tabular format
        System.out.println("---------------------------------------------------------------------------------------------");

        System.out.printf("%5s %20s %20s %20s %20s", "Node", "GEN", "KILL", "IN", "OUT");
        System.out.println();
        System.out.println("---------------------------------------------------------------------------------------------");
        controlFlowNodes.forEach(b -> {
            Set<String> genList = gen.get(b);
            Set<String> killList = kill.get(b);
            Set<String> inList = in.get(b);
            Set<String> outList = out.get(b);
            String genStr = printSet(genList);
            String killStr = printSet(killList);
            String inStr = printSet(inList);
            String outStr = printSet(outList);
            String blockId = b.getId() + "";
            System.out.format("%5s %20s %20s %20s %20s", blockId, genStr, killStr, inStr, outStr);
            System.out.println();
        });

        System.out.println("----------------------------------------------------------------------------------------------");
    }

}
