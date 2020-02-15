package frc.robot.software;

import java.util.ArrayList;

import edu.wpi.first.wpilibj.controller.PIDController;

public class SmartPID extends PIDController {

    ArrayList<Double> history = new ArrayList<Double>();
    Timer timer = new Timer("SmartPID Timer");
    boolean isActionDone = false;

    public SmartPID(double Kp, double Ki, double Kd) {
        super(Kp,Ki,Kd);
        timer.start();
    }

    public double next(double reading) {
        
        double result = calculate(reading);

        if(timer.getElaspedTimeInMs() > 100) {
            history.add(result);
            timer.start();
        }

        if(history.size() != 0 && history.size() % 5 == 0) {

            double avg = 0;

            for(int i = history.size() - 6; i < history.size(); ++i) {
                avg += Math.abs(history.get(i)) * (1/5);
            }

            if(avg < 0.1) {
                isActionDone = true;
            }
        }

        return result;

    }

    public boolean isActionDone() {
        return isActionDone;
    }
}