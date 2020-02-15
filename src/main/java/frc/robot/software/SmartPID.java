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
            
        }

        return result;

    }
}