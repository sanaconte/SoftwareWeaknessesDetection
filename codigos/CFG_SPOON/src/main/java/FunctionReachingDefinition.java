import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.ControlFlowNode;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;

import java.util.*;
import java.util.stream.Collectors;

public class FunctionReachingDefinition {

    private Map<ControlFlowNode, Set<String>> in ;
    private Map<ControlFlowNode, Set<String>> out;
    private Map<ControlFlowNode, Set<String>> gen;
    private Map<ControlFlowNode, Set<String>> kill;

    private Set<String> localVariables;
    private ControlFlowGraph graph;

    public FunctionReachingDefinition(ControlFlowGraph graph) {
        this.graph = graph;
        in = new HashMap();
        out = new HashMap();
        gen =  new HashMap();
        kill = new HashMap();
        fillLocalVariables();
        fillGen();
        fillKill();
        computeAlgorithm();
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
    }

    private void fillGen(){
        Set<ControlFlowNode> controlFlowNodes = graph.vertexSet();
        controlFlowNodes.forEach(c -> {
            if(c.getStatement() instanceof CtInvocation){
                CtInvocation ctInvocation = (CtInvocation) c.getStatement();
                List<CtExpression> expressions = (List<CtExpression>)ctInvocation.getArguments();
                List<String> arguments = expressions.stream().map(exp -> exp.toString()).collect(Collectors.toList());

                localVariables
                        .stream().filter(var -> arguments.stream().anyMatch(a -> a.contains(var)))
                        .forEach(var -> {
                            Set<String> hash = new HashSet();
                            hash.add(var+":"+c.getId());
                            gen.put(c, hash);
                        });
            }
            else {
                gen.put(c, new HashSet<>());
            }
        });
    }

    private void fillKill(){
        List<String> list = gen.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
        for (Map.Entry<ControlFlowNode, Set<String>> entry : gen.entrySet()) {
            ControlFlowNode cont = entry.getKey();

            if(cont.getStatement() instanceof CtInvocation){

                CtInvocation ctInvocation = (CtInvocation) cont.getStatement();
                List<CtExpression> expressions = (List<CtExpression>)ctInvocation.getArguments();
                List<String> arguments = expressions.stream().map(exp -> exp.toString()).collect(Collectors.toList());
                localVariables
                        .stream().filter(var -> arguments.stream().anyMatch(a -> a.contains(var)))
                        .forEach(var -> {
                            List<String> listWithoutActual =
                                    list.stream().filter(str -> !str.equals(var+":"+cont.getId())).collect(Collectors.toList());
                            Set<String> kills = getKills(listWithoutActual, var);
                            kill.put(cont, kills);
                        });
            }
            else{
                kill.put(cont, new HashSet());
            }
        }
    }

    private static Set<String> getKills(List<String> gen, String varName){
        return  gen.stream().filter(m -> m.startsWith(varName)).collect(Collectors.toSet());
    }

    public static void iterateUsingEntrySet(Map<ControlFlowNode, Set<String>> map) {

        for (Map.Entry<ControlFlowNode, Set<String>> entry : map.entrySet()) {
            String value = entry.getValue().stream().collect(Collectors.joining(",","{","}"));
            System.out.println(entry.getKey().getId() + ":" + value);
        }
    }

    private void computeAlgorithm(){
        System.out.println("gen:");
        iterateUsingEntrySet(gen);
        System.out.println("kill:");
        iterateUsingEntrySet(kill);
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

        System.out.printf("%5s %20s %20s %20s %20s", "BLOCK", "GEN", "KILL", "IN", "OUT");
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
