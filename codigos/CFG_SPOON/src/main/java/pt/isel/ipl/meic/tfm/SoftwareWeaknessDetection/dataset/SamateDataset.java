package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.dataset;

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

//    private List<String> CI_PROJECT_LIST =
//            List.of("58918-v1.0.0", "58919-v1.0.0", "58920-v1.0.0", "58921-v1.0.0", "58922-v1.0.0", "58923-v1.0.0", "58925-v1.0.0",
//                    "58926-v1.0.0", "58928-v1.0.0", "58929-v1.0.0", "58930-v1.0.0", "58931-v1.0.0", "58932-v1.0.0", "58933-v1.0.0",
//                    "58935-v1.0.0", "58936-v1.0.0", "58939-v1.0.0", "58940-v1.0.0", "58942-v1.0.0", "58943-v1.0.0", "58944-v1.0.0",
//                    "58946-v1.0.0", "58947-v1.0.0", "58948-v1.0.0", "58675-v1.0.0", "58676-v1.0.0", "58677-v1.0.0", "58679-v1.0.0",
//                    "58680-v1.0.0", "58681-v1.0.0", "58682-v1.0.0", "58683-v1.0.0", "58684-v1.0.0", "58686-v1.0.0", "58688-v1.0.0",
//                    "58689-v1.0.0", "58690-v1.0.0", "58691-v1.0.0", "58692-v1.0.0", "58693-v1.0.0", "58694-v1.0.0", "58696-v1.0.0",
//                    "58697-v1.0.0", "58699-v1.0.0", "58700-v1.0.0", "58701-v1.0.0", "58702-v1.0.0", "58704-v1.0.0", "58705-v1.0.0",
//                    "58706-v1.0.0", "58707-v1.0.0", "58708-v1.0.0", "58709-v1.0.0", "58710-v1.0.0", "58711-v1.0.0", "58712-v1.0.0",
//                    "58714-v1.0.0", "58716-v1.0.0", "58717-v1.0.0", "58718-v1.0.0", "58719-v1.0.0", "58720-v1.0.0", "58723-v1.0.0",
//                    "58725-v1.0.0", "58727-v1.0.0", "58729-v1.0.0", "58730-v1.0.0", "58731-v1.0.0", "58732-v1.0.0", "58733-v1.0.0",
//                    "58734-v1.0.0", "58735-v1.0.0", "58736-v1.0.0", "58739-v1.0.0", "58740-v1.0.0", "58741-v1.0.0", "58742-v1.0.0",
//                    "58743-v1.0.0", "58745-v1.0.0", "58749-v1.0.0", "58750-v1.0.0", "58751-v1.0.0", "58753-v1.0.0",
//                    "58754-v1.0.0", "58755-v1.0.0", "58756-v1.0.0", "58757-v1.0.0", "58758-v1.0.0", "58759-v1.0.0", "58760-v1.0.0",
//                    "58761-v1.0.0", "58762-v1.0.0", "58765-v1.0.0", "58766-v1.0.0", "58767-v1.0.0", "58768-v1.0.0", "58770-v1.0.0",
//                    "58771-v1.0.0", "58773-v1.0.0", "58774-v1.0.0", "58777-v1.0.0", "58778-v1.0.0", "58779-v1.0.0", "58780-v1.0.0",
//                    "58781-v1.0.0", "58782-v1.0.0", "58783-v1.0.0", "58785-v1.0.0", "58786-v1.0.0", "58787-v1.0.0", "58788-v1.0.0",
//                    "58789-v1.0.0", "58790-v1.0.0", "58791-v1.0.0", "58793-v1.0.0", "58794-v1.0.0", "58795-v1.0.0", "58800-v1.0.0",
//                    "58801-v1.0.0", "58802-v1.0.0", "58804-v1.0.0", "58805-v1.0.0", "58806-v1.0.0", "58807-v1.0.0", "144660-v1.0.0",
//                    "144661-v1.0.0", "144662-v1.0.0", "144663-v1.0.0", "144664-v1.0.0", "144665-v1.0.0", "144666-v1.0.0",
//                    "144667-v1.0.0", "144668-v1.0.0", "144669-v1.0.0", "144670-v1.0.0", "144671-v1.0.0", "144672-v1.0.0", "144673-v1.0.0",
//                    "144674-v1.0.0", "144675-v1.0.0", "144676-v1.0.0", "144677-v1.0.0", "144678-v1.0.0", "144679-v1.0.0", "144680-v1.0.0",
//                    "144681-v1.0.0", "144682-v1.0.0", "144683-v1.0.0", "144684-v1.0.0", "144685-v1.0.0", "144686-v1.0.0", "144687-v1.0.0",
//                    "144688-v1.0.0", "144689-v1.0.0", "144690-v1.0.0", "144691-v1.0.0", "144692-v1.0.0", "144693-v1.0.0", "144694-v1.0.0",
//                    "144695-v1.0.0", "144696-v1.0.0", "144697-v1.0.0", "144698-v1.0.0", "144699-v1.0.0", "144700-v1.0.0",
//                    "144701-v1.0.0", "144702-v1.0.0", "144703-v1.0.0", "144704-v1.0.0", "144705-v1.0.0", "144706-v1.0.0", "144707-v1.0.0",
//                    "144708-v1.0.0", "144709-v1.0.0", "144710-v1.0.0", "144711-v1.0.0", "144712-v1.0.0", "144712-v1.0.0", "144713-v1.0.0",
//                    "144714-v1.0.0", "144715-v1.0.0", "144716-v1.0.0", "144717-v1.0.0", "144718-v1.0.0", "144719-v1.0.0", "144720-v1.0.0",
//                    "144721-v1.0.0", "144722-v1.0.0", "144723-v1.0.0", "144724-v1.0.0", "144725-v1.0.0", "144726-v1.0.0", "144727-v1.0.0",
//                    "144728-v1.0.0", "144729-v1.0.0", "144730-v1.0.0", "144731-v1.0.0", "144732-v1.0.0", "144733-v1.0.0", "144734-v1.0.0",
//                    "144735-v1.0.0", "144736-v1.0.0", "144737-v1.0.0", "144738-v1.0.0", "144739-v1.0.0", "144740-v1.0.0", "144741-v1.0.0",
//                    "144742-v1.0.0", "144743-v1.0.0", "144744-v1.0.0", "144745-v1.0.0", "144746-v1.0.0", "144747-v1.0.0", "144748-v1.0.0",
//                    "144749-v1.0.0", "144750-v1.0.0", "144751-v1.0.0", "144752-v1.0.0", "144753-v1.0.0", "144754-v1.0.0", "144755-v1.0.0",
//                    "144756-v1.0.0", "144757-v1.0.0", "144758-v1.0.0", "144659-v1.0.0", "58709-v1.0.0", "58710-v1.0.0", "58711-v1.0.0",
//                    "58639-v1.0.0", "58640-v1.0.0", "58641-v1.0.0", "58642-v1.0.0", "58644-v1.0.0", "58646-v1.0.0", "58647-v1.0.0",
//                    "58647-v1.0.0", "58648-v1.0.0", "58649-v1.0.0", "58651-v1.0.0", "58653-v1.0.0", "58654-v1.0.0", "58657-v1.0.0",
//                    "58658-v1.0.0", "58659-v1.0.0", "58661-v1.0.0", "58664-v1.0.0", "58665-v1.0.0", "58667-v1.0.0", "58668-v1.0.0",
//                    "58669-v1.0.0", "58672-v1.0.0", "58673-v1.0.0", "58674-v1.0.0", "58675-v1.0.0", "58676-v1.0.0", "58677-v1.0.0",
//                    "58602-v1.0.0", "58603-v1.0.0", "58605-v1.0.0", "58606-v1.0.0", "58607-v1.0.0", "58608-v1.0.0", "58609-v1.0.0",
//                    "58610-v1.0.0", "58611-v1.0.0", "58614-v1.0.0", "58615-v1.0.0", "58618-v1.0.0", "58619-v1.0.0", "58620-v1.0.0",
//                    "58621-v1.0.0", "58623-v1.0.0", "58625-v1.0.0", "58626-v1.0.0", "58630-v1.0.0", "58634-v1.0.0", "58637-v1.0.0");

    private String samateProjectDirectory;
    private String vulType;
    private List<String> projectList;
    public SamateDataset(String vulType) {
        this.vulType = vulType;

        if(vulType.equals("NPD")){
            samateProjectDirectory = "E:/TFM/SAMATE-DATA-NPD/";
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
                        .filter(ctMethod -> /**ctMethod.getSimpleName().startsWith("good") ||**/
                                ctMethod.getSimpleName().startsWith("bad") ||
                                        ctMethod.getSimpleName().contains("bad")  )
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
