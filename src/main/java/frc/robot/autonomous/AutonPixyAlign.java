package frc.robot.autonomous;

import edu.wpi.first.wpilibj.controller.PIDController;
import frc.robot.hardware.*;

public class AutonPixyAlign extends AutonBase {
    
    double target;
    PIDController pid = new PIDController(.045, .85, .005);

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

        Chassis.driveRaw(0,pid.calculate(robot.pixyCam.getRangeInches()) * 0.5);
        robot.postData();
    }

    @Override
    public boolean isActionDone() {
        return pid.atSetpoint();
    }
}