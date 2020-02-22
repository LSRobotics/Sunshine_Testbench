package frc.robot.software;

import frc.robot.*;
import frc.robot.software.*;
import frc.robot.hardware.*;
import frc.robot.autonomous.*;

public class Core {
    public static Robot robot;
    public static boolean isDisabled = true;
    //public static boolean teleOpInit = true;

    public static void initialize(Robot main) {
        robot = main;
    }

}