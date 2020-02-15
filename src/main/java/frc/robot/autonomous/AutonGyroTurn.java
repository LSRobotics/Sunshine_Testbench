package frc.robot.autonomous;

import frc.robot.hardware.*;
import frc.robot.software.SmartPID;

public class AutonGyroTurn extends AutonBase {
    
    double targetAngle;
    SmartPID pid;

    public AutonGyroTurn (double targetAngle) {
        super();
        this.targetAngle = targetAngle;
    }

    public AutonGyroTurn (double targetAngle, Gamepad killGp, Gamepad.Key killKey) {
        super(killGp,killKey);

        this.targetAngle = targetAngle;
    }
    
    @Override
    public void preRun() {
        pid =  new SmartPID(.045, .85, .005);
        pid.setSetpoint(targetAngle);
    }

    @Override
    public void duringRun() {
        Chassis.driveRaw(0,pid.next(NavX.navx.getYaw())* 0.2);
    }

    @Override
    public boolean isActionDone() {

        return pid.isActionDone();
    }
}