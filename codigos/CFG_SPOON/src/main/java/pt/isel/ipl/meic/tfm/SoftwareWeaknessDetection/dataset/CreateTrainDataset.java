package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.dataset;

public class CreateTrainDataset {


    private static final String SLD = "samate";
    private static final String NPD_VUL = "NPD";
    private static final String CI_VUL = "CI";

    private static void createSet(){

        DatasetFactory.getDataset(SLD, NPD_VUL).createSet();

    }

    public static void main(String[] args) {
        createSet();
    }
}
