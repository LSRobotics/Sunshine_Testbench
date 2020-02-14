package frc.robot.autonomous;

import edu.wpi.first.wpilibj.controller.PIDController;
import frc.robot.hardware.*;

public class AutonPixyAlign extends AutonBase {
    
    double target;
    PIDController pid = new PIDController(1.5, 0, 0);

    public AutonPixyAlign (double target) {
        super();
        this.target = target;
    }

    public AutonPixyAlign (double target, Gamepad killGp, Gamepad.Key killKey) {
        super(killGp,killKey);
    }
    
    @Override
    public void preRun() {
        pid.setSetpoint(target);
    }

    @Override
    public void duringRun() {


        Chassis.driveRaw(0,-pid.calculate(PixyCam.getTargetLocation()));
        robot.postData();
    }

    @Override
    public boolean isActionDone() {
        return false;
        //return pid.atSetpoint();
    }
}