package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.webkit.WebView;

import java.lang.reflect.Array;
import java.util.Timer;

public class Simulator {
    public String terminalOutput;
    public String terminalInput;
    public byte[] output;
    public byte[] memory;
    public byte[][] registers;
    public boolean regbank;
    public int PC;
    Timer myTimer;

    public WebView referenceToTheWebViewInSimulation;
    private Simulator() {
        terminalOutput = AssembledProgram.getInstance().terminalOutputDuringAssembly;
        output = new byte[256];
        terminalInput = "";
        memory = new byte[256];
        registers = new byte[2][];
        registers[0]=new byte[16];
        registers[1]=new byte[16];
        regbank=false;
        PC=0;
        myTimer = new Timer();
    }

    public void simulateOneInstruction() {

    }

    private static Simulator instance;

    public static Simulator getInstance() {
        if (instance == null)
            instance = new Simulator();
        return instance;
    }
}
