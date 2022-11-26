import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.ControlFlowNode;
import fr.inria.controlflow.NaiveExceptionControlFlowStrategy;
import fr.inria.spoon.dataflow.scanners.CheckersScanner;
import org.apache.commons.io.IOUtils;
import spoon.Launcher;
import spoon.experimental.SpoonifierVisitor;
import spoon.processing.AbstractProcessor;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtIfImpl;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.stream.Collectors;

import static fr.inria.controlflow.BranchKind.BRANCH;

public class ExampleCFG {

    public static void main(String[] args) {

        String userDirectory = FileSystems.getDefault()
                .getPath("")
                .toAbsolutePath()
                .toString();

        System.out.println(userDirectory);

        InputStream inputStream = null;
        try {

            FileInputStream fis =
                    new FileInputStream(userDirectory+"/src/main/resources/CWE476_NULL_Pointer_Dereference__String_03.java");
            String data = IOUtils.toString(fis, "UTF-8");

            SpoonifierVisitor v = new SpoonifierVisitor(true);
            CtElement ctElement = Launcher
                    .parseClass(data)
                    .getElements( el -> el instanceof CtMethod ).get(0);
            //ctElement.accept(v);
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

        /*
            List<CtAssignment> elements =
                    ctElement.getElements(new TypeFilter(CtAssignment.class));

            Map<ControlFlowNode, List<String>> in = new HashMap();
            Map<ControlFlowNode, List<String>> out = new HashMap();
            Map<ControlFlowNode, List<String>> gen =  new HashMap();
            Map<ControlFlowNode, List<String>> kill = new HashMap();

            graph.statements().forEach(e -> {
                in.put(e, new ArrayList<String>());
            });

            */

            /**
            Iterator var2 = graph.vertexSet().iterator();
            int i = 0;
            while(var2.hasNext()) {
                ControlFlowNode n = (ControlFlowNode)var2.next();
                System.out.println("id: "+ i +" node: "+n);
                i++;
            }
            */
           // System.out.println(graph);
            Map<ControlFlowNode, Set<String>> in = new HashMap();
            Map<ControlFlowNode, Set<String>> out = new HashMap();
            Map<ControlFlowNode, Set<String>> gen =  new HashMap();
            Map<ControlFlowNode, Set<String>> kill = new HashMap();
           // graph.simplifyBlockNodes();
            //System.out.println(graph.toGraphVisText());
            Set<ControlFlowNode> controlFlowNodes = graph.vertexSet();
            //preencher gen
            controlFlowNodes.forEach(c -> {
                if(c.getStatement() instanceof CtLocalVariable){
                    CtLocalVariable ctLocalVariable = (CtLocalVariable)c.getStatement();
                    String varName = ctLocalVariable.getReference().clone().toString();
                    Set<String> hash = new HashSet();
                    hash.add(varName+c.getId());
                    gen.put(c, hash);
                }
                else if(c.getStatement() instanceof CtAssignment){
                    CtAssignment ctAssignment = (CtAssignment)c.getStatement();
                    String varName = ctAssignment.getAssigned().toString();
                    Set<String> hash = new HashSet();
                    hash.add(varName+c.getId());
                    gen.put(c, hash);
                }
                else {
                    gen.put(c, new HashSet<>());
                }
            });

            //preencher kill
            List<String> list = gen.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
            for (Map.Entry<ControlFlowNode, Set<String>> entry : gen.entrySet()) {
                ControlFlowNode cont = entry.getKey();
                if(cont.getStatement() instanceof CtLocalVariable){
                    CtLocalVariable ctLocalVariable = (CtLocalVariable)cont.getStatement();
                    String varName = ctLocalVariable.getReference().clone().toString();
                    List<String> listWithoutActual =
                            list.stream().filter(str -> !str.equals(varName+cont.getId())).collect(Collectors.toList());
                    Set<String> kills = getKills(listWithoutActual, varName);
                    kill.put(entry.getKey(), kills);

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

            //preencher in a vazio e out com gen
            controlFlowNodes.forEach(b -> {
              in.put(b, new HashSet());
              out.put(b, gen.get(b));
            });

            boolean[] change = {true};
            while(change[0]) {
                change[0] = false;
                controlFlowNodes.forEach(b -> {
                    List<ControlFlowNode> prev = b.prev();
                    Set<String> predecessors =
                            prev.stream().map(c -> out.get(c)).flatMap(set -> set.stream()).collect(Collectors.toSet());

                    //IN[i] = ∪p a predecessor of i OUT[p];
                    in.put(b, predecessors);

                    Set<String> oldout = out.get(b);
                    Set<String> genList = gen.get(b);
                    Set<String> inList = in.get(b);
                    Set<String> killList = kill.get(b);

                    //IN[b] ─ KILL[b]
                    Set<String> inMinusKill =
                            inList.stream().filter(el -> !killList.contains(el)).collect(Collectors.toSet());
                    inMinusKill.addAll(genList);
                   // System.out.println("genList: "+genList);
                    out.put(b, inMinusKill);
                    Set<String> outList = out.get(b);
                    if(!outList.equals(oldout)){
                        change[0] = true;
                    }
                });
            }

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
                String genStr = toString(genList);
                String killStr = toString(killList);
                String inStr = toString(inList);
                String outStr = toString(outList);
                String blockId = b.getId() + "";
                System.out.format("%5s %20s %20s %20s %20s", blockId, genStr, killStr, inStr, outStr);
                System.out.println();
            });

            System.out.println("----------------------------------------------------------------------------------------------");

           /* iterateUsingEntrySet(gen);
            System.out.println("kill:");
            iterateUsingEntrySet(kill);
            System.out.println("in:");
            iterateUsingEntrySet(in);
            System.out.println("out:");
            iterateUsingEntrySet(out); */

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static String toString(Set<String> set){
        return set.stream().collect(Collectors.joining(",","{","}"));
    }

    //List<String> list = map.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
    public static Set<String> getKills(List<String> gen, String varName){
        return  gen.stream().filter(m -> m.startsWith(varName)).collect(Collectors.toSet());
    }

    public static void iterateUsingEntrySet(Map<ControlFlowNode, Set<String>> map) {

        for (Map.Entry<ControlFlowNode, Set<String>> entry : map.entrySet()) {
            String value = entry.getValue().stream().collect(Collectors.joining(",","{","}"));
            System.out.println(entry.getKey().getId() + ":" + value);
        }
    }

}
