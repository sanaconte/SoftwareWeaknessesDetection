package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.dataset;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.NaiveExceptionControlFlowStrategy;
import org.apache.commons.io.IOUtils;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.transformation.*;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.utils.CsvUtil;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils.Location2;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils.Root;
import pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.utils.SamateProjectListConst;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SamateDataset implements IDataSet {

    private String samateProjectDirectory;
    private String vulType;
    private List<String> projectList;
    public SamateDataset(String vulType) {
        this.vulType = vulType;

        if(vulType.equals("NPD")){
            samateProjectDirectory = "E:/TFM/SAMATE-NPD/";
            projectList = SamateProjectListConst.NPD_PROJECT_LIST;
        }
        else if(vulType.equals("CI")){
            samateProjectDirectory = "E:/TFM/SAMATE-CI/";
            projectList = SamateProjectListConst.CI_PROJECT_LIST;
        }
        else{
            throw new RuntimeException("Vulnerability type: "+vulType+" Not valid!");
        }
    }

    @Override
    public void createSet() {
        transformFiles();
    }


    private void transformFiles(){

        InputStream inputStream = null;
        String datasetFileName = "samate-"+vulType+"-dataset.csv";
        try {
            List<List<String>> listaMatrizes = forEachProject2(projectList);
            CsvUtil.createDataset(listaMatrizes, datasetFileName);
            //CsvUtil.printDataset(listaMatrizes);

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

    private   List<CtElement> getMethodsFromProject(String uri){
//        Launcher launcher = new Launcher();
//        //55165-v1.0.0
//        //String project =  projectName //"55165-v1.0.0";
//        //String val =  "E:\\TFM\\SAMATE-DATA\\"+project;
//        launcher.addInputResource(projectDirectory+"/src/main/java");
//        launcher.getEnvironment().setNoClasspath(true);
//        CtModel model = launcher.buildModel();
//        return  model.getElements(el -> el instanceof CtMethod)
//                .stream()
//                .map(ctEl -> (CtMethod)ctEl)
//                .filter(method -> method.getBody() !=null)
//                .filter(method -> method.getSimpleName().contains("bad"))
//                .collect(Collectors.toList());

        FileInputStream vulFile = null;
        try {
            vulFile = new FileInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String content = null;
        try {
            content = IOUtils.toString(vulFile, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return
                Launcher
                        .parseClass(content)
                        .getElements(el -> el instanceof CtMethod)
                        .stream()
                        .map(ctEl -> (CtMethod)ctEl)
//                        .filter(ctMethod -> /**ctMethod.getSimpleName().startsWith("good") ||**/
//                                ctMethod.getSimpleName().startsWith("bad") ||
//                                        ctMethod.getSimpleName().contains("bad")  )
                        .filter(ctMethod -> ctMethod.getBody() != null)
                        .collect(Collectors.toList());

    }

    private List<List<String>> forEachProject2(List<String> projectList) throws IOException {
        List<List<String>> listaMatrizes = new ArrayList<>();
        for (String project: projectList) {

            String val = samateProjectDirectory +project+"/manifest.sarif";

            FileInputStream fis =
                    new FileInputStream(val);
            String data = IOUtils.toString(fis, "UTF-8");

            ObjectMapper om = new ObjectMapper();
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Root root = om.readValue(data, Root.class);

            forEachVulnerableLocation(listaMatrizes, project, root);
        }
        return listaMatrizes;
    }

    private void forEachVulnerableLocation(List<List<String>> listaMatrizes, String project, Root root) throws IOException {
        Map<String, List<Location2>> collect = root.getRuns().get(0).getResults()
                .get(0).getLocations().stream()
                .collect(Collectors.groupingBy(loc -> new File(loc.getPhysicalLocation().getArtifactLocation().getUri()).getName()));

        Map<String, List<Integer>> vulnerabilityMap = collect
                .entrySet()
                .stream()
                .collect(Collectors.toConcurrentMap(
                        Map.Entry::getKey,
                        list -> list.getValue()
                                .stream()
                                .map(loc -> loc.getPhysicalLocation().getRegion().getStartLine())
                                .collect(Collectors.toList())
                ));
        String uri = samateProjectDirectory +project;

        List<CtElement> methodList = Transform.filterMethodByName(uri, Set.of("bad", "good"));
        methodList.forEach(ctElem -> {

            ControlFlowBuilder builder = new ControlFlowBuilder();

            EnumSet<NaiveExceptionControlFlowStrategy.Options> options;
            options = EnumSet.of(NaiveExceptionControlFlowStrategy.Options.ReturnWithoutFinalizers);
            builder.setExceptionControlFlowStrategy(new NaiveExceptionControlFlowStrategy(options));

            ControlFlowGraph graph =
                    builder.build(ctElem);
            graph.simplifyBlockNodes();
            graph.simplify();
            //System.out.println(graph.toGraphVisText());
            ReachingDefinition rd = new ReachingDefinition(graph, ctElem);
            //UseDefinitionChain useDefinition = new UseDefinitionChain(rd, vulnerableLines);
            UseDefinition useDefinition = new UseDefinition(rd);
            String projectMethodName = project+"/src/main/java/"+ctElem.getPosition().getFile().getName();
            FeaturesExtraction featuresExtraction = new FeaturesExtraction(rd, useDefinition, projectMethodName, vulnerabilityMap);
            featuresExtraction.executeExtraction();
            List<String> transform =
                    featuresExtraction.getResultExtraction();
            listaMatrizes.add(transform);

        });
    }


}
