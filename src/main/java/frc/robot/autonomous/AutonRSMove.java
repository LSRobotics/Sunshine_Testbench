package frc.robot.autonomous;

import frc.robot.software.*;
import frc.robot.hardware.*;
import frc.robot.hardware.Gamepad.Key;

public class AutonRSMove extends AutonBase{
    
    RangeSensor sensor;
    double targetDistance;
    SmartPID pid;

    public AutonRSMove(RangeSensor sensor, double targetDistance) {
        super();
        this.sensor = sensor;
        this.targetDistance = targetDistance;
    }

    public AutonRSMove(RangeSensor sensor, double targetDistance, Gamepad interrputGamepad, Key interrputKey) {
        super(interrputGamepad,interrputKey);
        this.sensor = sensor;
        this.targetDistance = targetDistance;
    }
    
    @Override
    public void preRun() {
        pid = new SmartPID(1.75, 0, 0.6);
        pid.setSetpoint(targetDistance);
    }

    @Override
    public void duringRun() {
        Chassis.driveRaw(pid.next(sensor.getRangeInches()),0);
    }

    @Override
    public boolean isActionDone() {
        return pid.isActionDone();
    }

    @Override
    public void postRun() {
        Chassis.stop();
    }

}