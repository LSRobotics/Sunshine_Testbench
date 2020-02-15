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
        //pid = new SmartPID(1.75, 0, 0.6);
        pid =  new SmartPID(.045, .85, .01);
        pid.setSetpoint(targetAngle);
    }

    @Override
    public void duringRun() {
        //Chassis.driveRaw(0,pid.next(NavX.navx.getYaw()));
        Chassis.driveRaw(0,pid.next(NavX.navx.getYaw())* 0.2);
    }

    @Override
    public void postRun() {
        Chassis.stop();
    }

    @Override
    public boolean isActionDone() {
        return pid.atSetpoint();
        //return pid.isActionDone();
    }
}