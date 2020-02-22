/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import java.util.ArrayList;

//WPILib
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.controller.PIDController;
import frc.robot.autonomous.*;
//Internal
import frc.robot.constants.*;
import frc.robot.hardware.*;
import frc.robot.hardware.Gamepad.Key;
import frc.robot.hardware.Solenoid.Status;
import frc.robot.software.*;
import frc.robot.hardware.RangeSensor.Type;
import frc.robot.software.AutonChooser;

public class Robot extends TimedRobot {

  // Shared (Make sure these are "public" so that Core can take them in, which
  // allows global access to happen)
  public Gamepad gp1, gp2;

  public static double driveSpeed = 1.0;
  public static boolean isLinearAutonOK = false;

  // Drive mode GUI variables and setup
  public static final String kDefaultDrive = "Default (Right Stick)";
  public static final String kCustomDrive = "Right Stick Drive";
  public static final String kCustomDrive1 = "Left Stick Drive";
  public static final String kCustomDrive2 = "Both Stick Drive";
  
  public String m_driveSelected, AutonSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  public static boolean isBlueLine, isRedLine, isWhiteLine, isYellowCP, isRedCP, isGreenCP, isBlueCP; //CP = control panel

  public RGBSensor colorSensor = new RGBSensor();
  public double[] color = {};

  public PIDController ultrasonicPID;
  public double lastTargetAngle = 0;

  @Override
  public void robotInit() {

    NavX.initialize();
    NavX.navx.zeroYaw();

    ultrasonicPID = new PIDController(.045, .85, .005);

    // Drive mode GUI setup
    m_chooser.setDefaultOption("Default (Right Stick)", kDefaultDrive);
    m_chooser.addOption("Right Stick Drive", kCustomDrive);
    m_chooser.addOption("Left Strick Drive", kCustomDrive1);
    m_chooser.addOption("Both Strick Drive", kCustomDrive2);
    SmartDashboard.putData("Drive choices", m_chooser);
    System.out.println("Drive Selected: " + m_driveSelected);

    Chassis.initialize();
    Chassis.setSpeedCurve(SpeedCurve.SQUARED);

    gp1 = new Gamepad(0);
    gp2 = new Gamepad(1);

    PixyCam.initialize();

    Core.initialize(this);

    Camera.initialize();

    AutonChooser.init();

  }

  @Override
  public void disabledPeriodic() {
    updateColorSensor();
    postData();
    isLinearAutonOK = false;
    Core.isDisabled = true;
  }

  @Override
  public void robotPeriodic() {
  
  }

  @Override
  public void autonomousInit() {
    Core.isDisabled = false;
  }

  @Override
  public void teleopInit() {
    m_driveSelected = m_chooser.getSelected();
    Core.isDisabled = false;
  }

  @Override
  public void autonomousPeriodic() {
    NavX.navx.zeroYaw();
    if(!isLinearAutonOK) {
      isLinearAutonOK = true;
      AutonChooser.getSelected().run();
    }
  }

  @Override 
  public void testPeriodic() {
    if(PixyCam.getTargetLocation() < -0.05) {
      Chassis.drive(0,-0.2);
    }
    else if(PixyCam.getTargetLocation() > 0.05) {
      Chassis.drive(0,0.2);
    }
    else {
      Chassis.stop();
    }
  }

  @Override
  public void teleopPeriodic() {

    gp1.fetchData();

    updateBottom();
    updateTop();

    postData();

  }

  // All code for driving
  public void updateBottom() {

    // Gearbox
    if (gp1.isKeyToggled(Key.DPAD_UP)) {
      Chassis.shift();
    }

    // raise drive speed
    if (gp1.isKeyToggled(Key.RB)) {
      if (driveSpeed + 0.25 <= 1.0) {
        driveSpeed += 0.25;
        Chassis.setSpeedFactor(driveSpeed);
      }
    }
    // lower drive speed
    else if (gp1.isKeyToggled(Key.LB)) {
      if (driveSpeed - 0.25 >= 0) {
        driveSpeed -= 0.25;
        Chassis.setSpeedFactor(driveSpeed);
      }
    }

    // Drive control
    else {
      Gamepad.Key yKey = Key.J_RIGHT_Y;
      Gamepad.Key xKey = Key.J_RIGHT_X;

      switch (m_driveSelected) {
      // Right Stick Drive
      case kCustomDrive:
        yKey = Key.J_RIGHT_Y;
        xKey = Key.J_RIGHT_X;
        break;
      // Left Stick Drive
      case kCustomDrive1:
        yKey = Key.J_LEFT_Y;
        xKey = Key.J_LEFT_X;
        break;
      // Both Stick Drive
      case kCustomDrive2:
        yKey = Key.J_LEFT_Y;
        xKey = Key.J_RIGHT_X;
        break;
      // Default is right stick drive
      case kDefaultDrive:
        yKey = Key.J_RIGHT_Y;
        xKey = Key.J_RIGHT_X;
        break;

      }
      Chassis.drive(-Utils.mapAnalog(gp1.getValue(yKey)), gp1.getValue(xKey));
    }

    // rotates robot to Setpoint Angle using PID
    if (gp1.isKeyToggled(Key.A)) {
      new AutonGyroTurn(0, gp1, Key.DPAD_DOWN).run();
    }

    // Line Drive (Drive forward until a red/blue tape line is detected)
    else if (gp1.isKeyToggled(Key.B)){
      new AutonDetectLine(color, gp1, Key.DPAD_DOWN).run();
    }

    // resets angle to zero
    if (gp1.isKeyToggled(Key.Y)) {
      NavX.navx.zeroYaw();
    }

    // full integration of sensors for shot line-up
    if (gp1.isKeyToggled(Key.X)) {
      double targetAngle = 0;

      new AutonDetectLine(color, gp1, Key.DPAD_DOWN).run();

      AutoPilot.sleep(150,gp1,Key.DPAD_DOWN);

      new AutonPixyAlign(0,gp1,Key.DPAD_DOWN).run();
      }

      //needs to be fixed
      if (gp1.isKeyToggled(Key.DPAD_RIGHT)) {
        Statics.autonToCenter.run();
      }

      if (gp1.isKeyToggled(Key.DPAD_LEFT)) {
        ultrasonicPID.setSetpoint(50);
        while (true) {
          
          postData();
          Chassis.driveRaw(ultrasonicPID.calculate(Chassis.frontAligner.getRangeInches()) * .15, 0);

          if(gp1.getRawReading(Key.DPAD_DOWN) != 0) {
            break;
          }
        }
      }

      if (gp1.isKeyToggled(Key.J_LEFT_DOWN)) {
        new AutonPixyAlign(0,gp1,Key.DPAD_DOWN).run();
      }
      
      if (gp1.isKeyToggled(Key.J_RIGHT_DOWN)) {
        PixyCam.switchLED(!PixyCam.isLedOn);
      }
    }

  public void updateColorSensor() {
    color = colorSensor.getColor();

    isBlueLine = Utils.isColorMatch(color, Statics.TAPE_BLUE, 0.06);
    isRedLine = Utils.isColorMatch(color, Statics.TAPE_RED, 0.06);
    isWhiteLine = Utils.isColorMatch(color, Statics.TAPE_WHITE, 0.02);
    isYellowCP = Utils.isColorMatch(color, Statics.CONTROLPANEL_YELLOW, 0.02);
    isRedCP = Utils.isColorMatch(color, Statics.CONTROLPANEL_RED, 0.02);
    isGreenCP = Utils.isColorMatch(color, Statics.CONTROLPANEL_GREEN, 0.02);
    isBlueCP = Utils.isColorMatch(color, Statics.CONTROLPANEL_BLUE, 0.02);

  }



  public void updateTop() {
    updateColorSensor();
  }

  public void postData() {

    SmartDashboard.putNumber("Front Ultrasonic", Chassis.frontAligner.getRangeInches());
    SmartDashboard.putNumber("Side Ultrasonic", Chassis.sideAligner.getRangeInches());
    SmartDashboard.putNumber("IR Sensor", Chassis.sensorIR.getRangeInches());
    SmartDashboard.putString("Current Gear", (Chassis.shifter.status == Status.FORWARD ? "Low" : "High"));
    SmartDashboard.putNumber("Angle", NavX.navx.getYaw());
    SmartDashboard.putString("Color Sensor (R,G,B)", color[0] + ", " + color[1] + ", " + color[2]);
    SmartDashboard.putNumber("target Angle", lastTargetAngle);
    SmartDashboard.putNumber("ultrasonic PID", ultrasonicPID.calculate(Chassis.frontAligner.getRangeInches()));
    SmartDashboard.putNumber("PIXY CAM", PixyCam.getTargetLocation());
    SmartDashboard.putNumber("Front Voltage",Chassis.frontAligner.analog.getVoltage());
    SmartDashboard.putNumber("Front value",Chassis.frontAligner.analog.getValue());
    SmartDashboard.putNumber("LED Voltage",PixyCam.led.getVoltage());

    //color sensor booleans
    SmartDashboard.putBoolean("Is Blue Line Detected", isBlueLine);
    SmartDashboard.putBoolean("Is Red Line Detected", isRedLine);
    SmartDashboard.putBoolean("Is White Line Detected", isWhiteLine);
    SmartDashboard.putBoolean("Yellow", isYellowCP);
    SmartDashboard.putBoolean("Red", isRedCP);
    SmartDashboard.putBoolean("Green", isGreenCP);
    SmartDashboard.putBoolean("Blue", isBlueCP);
  }
}