package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.dataset;

public class DatasetFactory {

    private static final String SAMATE = "samate";
    private static final String NVD = "nvd";
    public static IDataSet getDataset(String selectedDataset, String vulType){

        return switch (selectedDataset){
            case SAMATE -> new SamateDataset(vulType);
            case NVD -> new NvdDatabase();
            default -> throw new RuntimeException("No such dataset");
        };

    }
}
