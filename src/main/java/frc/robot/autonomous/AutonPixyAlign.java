package frc.robot.autonomous;

import frc.robot.hardware.*;
import frc.robot.software.SmartPID;

public class AutonPixyAlign extends AutonBase {
    
    double target;
    SmartPID pid;

    public AutonPixyAlign (double target) {
        super();
        this.target = target;
    }

    public AutonPixyAlign (double target, Gamepad killGp, Gamepad.Key killKey) {
        super(killGp,killKey);
    }
    
    @Override
    public void preRun() {
        pid = new SmartPID(1.5, 0, 0);
        pid.setSetpoint(target);
    }

    @Override
    public void duringRun() {
        Chassis.driveRaw(0,-pid.next(PixyCam.getTargetLocation()));
        robot.postData();
    }


    @Override
    public boolean isGamepadGood() {
        return !interruptGamepad.isGamepadChanged();
    }

    @Override
    public boolean isActionDone() {
        return isActionDone();
    }
}