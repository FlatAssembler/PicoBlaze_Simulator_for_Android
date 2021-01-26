package hr.ferit.teo_samarzija.picoblaze_simulator;

public class GlobalVariables {
    public int[] instructions, lineNumbers;
    public String assemblyCode;
    private GlobalVariables() {
        instructions = new int[4096];
        lineNumbers = new int[4096];
        assemblyCode=new String(";Insert assembly here...");
    }

    private static GlobalVariables instance;

    public static GlobalVariables getInstance() {
        if (instance==null)
            instance=new GlobalVariables();
        return instance;
    }
}
