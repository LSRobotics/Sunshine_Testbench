package frc.robot.hardware;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Ultrasonic;

public class RangeSensor {

    public enum Type {
        ANALOG_US_MAXBOTIX,
        DIO_US_HC_SR04,
        ANALOG_IR_GP2Y0A710K0F,
        PIXY_CAM
    }

    private boolean isInitialized = false;
    private Ultrasonic dio;
    public AnalogInput analog;
    private Type type;


    public RangeSensor(int port1, int port2, Type type) {

        this.type = type;

        switch(type) {
            case DIO_US_HC_SR04:
                dio = new Ultrasonic(port1,port2,Ultrasonic.Unit.kInches);
                break;
            case ANALOG_US_MAXBOTIX:
            case ANALOG_IR_GP2Y0A710K0F:
            case PIXY_CAM:
                analog = new AnalogInput(port1);
                break;
        }
    }

    public RangeSensor(int port1, Type type) {
        this(port1, 0, type);
    }

    public double getRangeInches() {

        if(!isInitialized && type == Type.DIO_US_HC_SR04) {
            isInitialized = true;
            dio.setAutomaticMode(true);
        }

        switch(type) {
            case ANALOG_US_MAXBOTIX:
                return analog.getValue() * 0.125;
            case DIO_US_HC_SR04:
                return dio.getRangeInches();
            case ANALOG_IR_GP2Y0A710K0F:
                return 430.75 * Math.pow(analog.getAverageVoltage(), -3.7031) + 23.645; //Stole from FRC 2017 haha
            case PIXY_CAM:
                return (analog.getAverageVoltage() / 3.3 * 2) - 1;
            default: return 0;
        }
    }
}