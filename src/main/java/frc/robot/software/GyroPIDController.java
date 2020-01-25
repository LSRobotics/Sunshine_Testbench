package frc.robot.software;

import edu.wpi.first.wpilibj.controller.PIDController;

public class GyroPIDController extends PIDController {

    public GyroPIDController(double kP, double kI, double kD) {
        super(kP, kI, kD);
    }

    public void calculate() {
       // super.calculate();
    }

}