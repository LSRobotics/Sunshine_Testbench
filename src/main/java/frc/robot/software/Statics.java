package frc.robot.software;

public class Statics {

//TODO: Update this once the robot gets built

//Environment variables
    static final public boolean DEBUG_MODE = true;
    static final public double LOW_SPD = 0.6;

//Controller
    static final public double OFFSET_MIN = 0.1,
                               OFFSET_MAX = 0.7;

//Chassis

   static final public int CHASSIS_L1 = 4,
                            CHASSIS_L2 = 3,
                            CHASSIS_R1 = 2,
                            CHASSIS_R2 = 5;

//TODO: UPDATE THIS
    static final public int US_ALIGNER_F_PING  = 6,
                            US_ALIGNER_F_ECHO  = 7,
                            US_ALIGNER_S_PING  = 8,
                            US_ALIGNER_S_ECHO  = 9;

//Shifting Gearbox
    static final public int SHIFTER_PCM = 0,
                            SHIFTER_F = 0,
                            SHIFTER_R = 1;

//TODO: Update this
    static final public int TIME_PER_360 = 1800;

//Colors
    static final public double[] TAPE_RED = {0.47,0.37,0.16},
                                 TAPE_BLUE = {0.21,0.42,0.36};
}