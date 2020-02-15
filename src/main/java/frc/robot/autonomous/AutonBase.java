package frc.robot.autonomous;

import frc.robot.hardware.*;
import frc.robot.hardware.Gamepad.Key;
import frc.robot.*;
import frc.robot.software.*;

public class AutonBase {
    
    Gamepad interruptGamepad;
    Gamepad.Key interruptKey;
    Robot robot;


    public AutonBase(Gamepad interruptGamepad, Gamepad.Key interruptKey) {
        this.interruptGamepad = interruptGamepad;
        this.interruptKey = interruptKey;
        //isAutonPeriod = false;
        robot = Core.robot;
    }

    public AutonBase() {
        this(Core.robot.gp1, Key.DPAD_DOWN);
        robot = Core.robot;
    }

    final public boolean run() {

        preRun();
    
        while(true) {

            interruptGamepad.fetchData();

            duringRun();

            if(!isGamepadGood()) {
                postRun();
                return false;
            } 
            else if (isActionDone()) {
                break;
            }
 
        }
        
        postRun();
        
        return true;
    }

    public void preRun() {
        
    }

    public void duringRun() {

    }

    public boolean isGamepadGood() {
        return interruptGamepad.getRawReading(interruptKey) == 0;
    }
 
    public boolean isActionDone() {
        return true;
    }


    public void postRun() {

    }
    
}