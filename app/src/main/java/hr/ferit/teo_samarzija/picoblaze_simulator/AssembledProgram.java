package hr.ferit.teo_samarzija.picoblaze_simulator;

public class AssembledProgram {
  public int[] instructions, lineNumbers;
  public String assemblyCode;
  public String terminalOutputDuringAssembly;
  private AssembledProgram() {
    instructions = new int[4096];
    lineNumbers = new int[4096];
    assemblyCode = new String(";Insert assembly here...");
    terminalOutputDuringAssembly = "";
  }

  private static AssembledProgram instance;

  public static AssembledProgram getInstance() {
    if (instance == null)
      instance = new AssembledProgram();
    return instance;
  }
}
