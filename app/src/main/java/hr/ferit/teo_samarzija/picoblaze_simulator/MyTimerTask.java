package hr.ferit.teo_samarzija.picoblaze_simulator;

import java.util.TimerTask;

public class MyTimerTask extends TimerTask {
    public void run() {
        Simulator.getInstance().simulateOneInstruction();
    }
}
