package frc.robot.software;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import frc.robot.hardware.*;
import frc.robot.autonomous.*;

/**
 * Created by TylerLiu on 2017/03/04.
 */
public class AutonChooser {

    public static SendableChooser<AutonGroup> chooser;

    public static void init() {
        
        //TODO: Add stuff to here
        
        chooser = new SendableChooser<>();

        chooser.setDefaultOption("Default Auton", new AutonGroup(new AutonRSMove(Chassis.sensorIR, 90),
                                                       new AutonPixyAlign(0)
                                                       //new AutonBall()
                                                       ));
        
        SmartDashboard.putData(chooser);
    }

    public static AutonGroup getSelected() {
        return chooser.getSelected();
    }
}