package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
public class TransformFile {
    private static String myJsonString = "{\n" +
            "  \"version\": \"2.1.0\",\n" +
            "  \"$schema\": \"https://json.schemastore.org/sarif-2.1.0.json\",\n" +
            "  \"runs\": [\n" +
            "    {\n" +
            "      \"properties\": {\n" +
            "        \"id\": 58947,\n" +
            "        \"version\": \"1.0.0\",\n" +
            "        \"type\": \"source code\",\n" +
            "        \"status\": \"deprecated\",\n" +
            "        \"submissionDate\": \"2011-04-08\",\n" +
            "        \"language\": \"java\",\n" +
            "        \"description\": \"CWE: 78 OS Command Injection\\n BadSource: listen_tcp Read data using a listening tcp connection\\n GoodSource: A hardcoded string\\n BadSink: exec dynamic command execution with Runtime.getRuntime().exec()\\n Flow Variant: 68 Data flow: data passed as a member variable in the \\\"a\\\" class, which is used by a method in another class in the same package\",\n" +
            "        \"state\": \"mixed\"\n" +
            "      },\n" +
            "      \"tool\": {\n" +
            "        \"driver\": {\n" +
            "          \"name\": \"SARD - SAMATE\",\n" +
            "          \"fullName\": \"Software Assurance Reference Dataset Project\",\n" +
            "          \"informationUri\": \"https://samate.nist.gov/SARD/\",\n" +
            "          \"version\": \"5.0.0\",\n" +
            "          \"organization\": \"NIST\",\n" +
            "          \"supportedTaxonomies\": [\n" +
            "            {\n" +
            "              \"name\": \"CWE\",\n" +
            "              \"index\": 0\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      },\n" +
            "      \"artifacts\": [\n" +
            "        {\n" +
            "          \"location\": {\n" +
            "            \"uri\": \"src/main/java/testcases/CWE78_OS_Command_Injection/CWE78_OS_Command_Injection__listen_tcp_68a.java\"\n" +
            "          },\n" +
            "          \"length\": 4270,\n" +
            "          \"sourceLanguage\": \"java\",\n" +
            "          \"hashes\": {\n" +
            "            \"sha-256\": \"bfd0e37c199613f965bd5ad7efe7e5a7894c81135e87c6834078eb43985fb837\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"location\": {\n" +
            "            \"uri\": \"src/main/java/testcases/CWE78_OS_Command_Injection/CWE78_OS_Command_Injection__listen_tcp_68b.java\"\n" +
            "          },\n" +
            "          \"length\": 2123,\n" +
            "          \"sourceLanguage\": \"java\",\n" +
            "          \"hashes\": {\n" +
            "            \"sha-256\": \"51f066de37b0ae7f8224e083c105d4f61bce06ab17c7f984f3006eed3ca9001f\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"location\": {\n" +
            "            \"uri\": \"src/main/java/testcasesupport/AbstractTestCase.java\"\n" +
            "          },\n" +
            "          \"length\": 1903,\n" +
            "          \"sourceLanguage\": \"java\",\n" +
            "          \"hashes\": {\n" +
            "            \"sha-256\": \"8f6a1cb2ef57786dd2d3310dc1d16c4176b44756bd9be62033da8070fa977689\"\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"location\": {\n" +
            "            \"uri\": \"src/main/java/testcasesupport/IO.java\"\n" +
            "          },\n" +
            "          \"length\": 2654,\n" +
            "          \"sourceLanguage\": \"java\",\n" +
            "          \"hashes\": {\n" +
            "            \"sha-256\": \"ff5fbe9cf7612c0b7ee83cceacba60324626151be57dfe948f18c407834cd55d\"\n" +
            "          }\n" +
            "        }\n" +
            "      ],\n" +
            "      \"taxonomies\": [\n" +
            "        {\n" +
            "          \"name\": \"CWE\",\n" +
            "          \"version\": \"4.6\",\n" +
            "          \"informationUri\": \"https://cwe.mitre.org/data/published/cwe_v4.6.pdf\",\n" +
            "          \"downloadUri\": \"https://cwe.mitre.org/data/xml/cwec_v4.6.xml.zip\",\n" +
            "          \"organization\": \"MITRE\",\n" +
            "          \"shortDescription\": {\n" +
            "            \"text\": \"The MITRE Common Weakness Enumeration\"\n" +
            "          },\n" +
            "          \"isComprehensive\": false,\n" +
            "          \"taxa\": [\n" +
            "            {\n" +
            "              \"id\": \"78\",\n" +
            "              \"name\": \"Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection')\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"results\": [\n" +
            "        {\n" +
            "          \"ruleId\": \"CWE-78\",\n" +
            "          \"message\": {\n" +
            "            \"text\": \"Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection').\"\n" +
            "          },\n" +
            "          \"locations\": [\n" +
            "            {\n" +
            "              \"physicalLocation\": {\n" +
            "                \"artifactLocation\": {\n" +
            "                  \"uri\": \"src/main/java/testcases/CWE78_OS_Command_Injection/CWE78_OS_Command_Injection__listen_tcp_68b.java\",\n" +
            "                  \"index\": 1\n" +
            "                },\n" +
            "                \"region\": {\n" +
            "                  \"startLine\": 44\n" +
            "                }\n" +
            "              }\n" +
            "            }\n" +
            "          ],\n" +
            "          \"taxa\": [\n" +
            "            {\n" +
            "              \"toolComponent\": {\n" +
            "                \"name\": \"CWE\",\n" +
            "                \"index\": 0\n" +
            "              },\n" +
            "              \"id\": \"78\",\n" +
            "              \"index\": 0\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}\n";


    public static void main(String[] args) {
        ObjectMapper om = new ObjectMapper();
        Root root;

        {
            try {
                root = om.readValue(myJsonString, Root.class);
                int start = root.getRuns().get(0).getResults().get(0).locations.get(0).getPhysicalLocation().getRegion().getStartLine();
                System.out.println("start: "+start);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
