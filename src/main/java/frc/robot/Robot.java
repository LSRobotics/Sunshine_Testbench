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
import io.github.pseudoresonance.pixy2api.Pixy2;
import io.github.pseudoresonance.pixy2api.Pixy2.LinkType;
import frc.robot.autonomous.AutonBase;
import frc.robot.autonomous.AutonGyroTurn;
//Internal
import frc.robot.hardware.*;
import frc.robot.hardware.NavX;
import frc.robot.hardware.Gamepad.Key;
import frc.robot.hardware.Solenoid.Status;
import frc.robot.software.*;

public class Robot extends TimedRobot {

  // Shared (Make sure these are "public" so that Core can take them in, which
  // allows global access to happen)
  public Gamepad gp1, gp2;

  public static double driveSpeed = 1.0;

  private final Pixy2 _pixy2 = Pixy2.createInstance(LinkType.SPI);

  // Drive mode GUI variables and setup
  public static final String kDefaultDrive = "Default (Right Stick)";
  public static final String kCustomDrive = "Right Stick Drive";
  public static final String kCustomDrive1 = "Left Stick Drive";
  public static final String kCustomDrive2 = "Both Stick Drive";
  
  public String m_driveSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  public static boolean isBlueLine, isRedLine, isWhiteLine;

  public RGBSensor colorSensor = new RGBSensor();
  public double[] color = {};

  public PIDController gyroPID;
  public double lastTargetAngle = 0;

  @Override
  public void robotInit() {

    NavX.initialize();
    NavX.navx.zeroYaw();

    _pixy2.init(0);

    gyroPID = new PIDController(.045, .85, .005); // variables you test
    gyroPID.setSetpoint(0);

    // Drive mode GUI setup
    m_chooser.setDefaultOption("Default (Right Stick)", kDefaultDrive);
    m_chooser.addOption("Right Stick Drive", kCustomDrive);
    m_chooser.addOption("Left Strick Drive", kCustomDrive1);
    m_chooser.addOption("Both Strick Drive", kCustomDrive2);
    SmartDashboard.putData("Drive choices", m_chooser);
    System.out.println("Drive Selected: " + m_driveSelected);

    Core.initialize(this);

    Chassis.initialize();

    gp1 = new Gamepad(0);
    gp2 = new Gamepad(1);

    Camera.initialize();

  }

  @Override
  public void disabledPeriodic() {
    updateColorSensor();
    postData();
  }

  @Override
  public void robotPeriodic() {
  }

  @Override
  public void autonomousInit() {
    m_driveSelected = m_chooser.getSelected();
  }

  @Override
  public void teleopInit() {
    m_driveSelected = m_chooser.getSelected();
  }

  @Override
  public void autonomousPeriodic() {
    // TODO: ACTUALLY DO SOME KIND OF AUTON
    teleopPeriodic();
  }

  @Override
  public void teleopPeriodic() {

    gp1.fetchData();

    updateBottom();
    updateTop();
    Pixy();

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
      Chassis.drive(Utils.mapAnalog(gp1.getValue(yKey)), -gp1.getValue(xKey));
    }

    // rotates robot to Setpoint Angle using PID
    if (gp1.isKeyToggled(Key.A)) {
      new AutonGyroTurn(0, gp1, Key.DPAD_DOWN).run();
    }
    // Line Drive (Drive forward until a red/blue tape line is detected)
    else if (gp1.isKeyToggled(Key.B)) {
      if (!isBlueLine && !isRedLine) {
        Chassis.driveRaw(-0.175, 0);
        while (true) {

          updateColorSensor();

          if (gp1.getRawReading(Key.DPAD_DOWN) != 0 || (isBlueLine || isRedLine)) {
            break;
          }

        }
        Chassis.stop();
      }
    }

    // resets angle to zero
    if (gp1.isKeyToggled(Key.Y)) {
      NavX.navx.zeroYaw();
    }

    // full integration of sensors for shot line-up
    if (gp1.isKeyToggled(Key.X)) {
      double targetAngle = 0;

      if (!isBlueLine && !isRedLine) {
        Chassis.driveRaw(-0.175, 0);
        while (true) {

          updateColorSensor();

          if (gp1.getRawReading(Key.DPAD_DOWN) == 1 || (isBlueLine || isRedLine)) {
            break;
          }

        }
        Chassis.stop();
      }

      AutoPilot.sleep(250,gp1,Key.DPAD_DOWN);

      //find and set target angle
      targetAngle = Math.toDegrees(Math.atan2(94.66-(30+Chassis.sideAligner.getRangeInches()), 206.57-6));

      // x from trench line= 206.57in
      // y from trench line= 94.66in

      gyroPID.setSetpoint(-targetAngle);
      lastTargetAngle = -targetAngle;
    
        Timer t = new Timer();

        t.start();

        while(t.getElaspedTimeInMs() < 250) {

          gp1.fetchData();

          if(gp1.isKeyHeld(Key.DPAD_DOWN)) {
            break;
          }
        }

      while (true) {
        Chassis.drive(0, -gyroPID.calculate(NavX.navx.getYaw()) * 0.15);

        if (gp1.getRawReading(Key.DPAD_DOWN) != 0 || gyroPID.atSetpoint()) {
          break;
        }
      }

      if (gp1.isKeyToggled(Key.DPAD_RIGHT)) {
        double fieldX = 206.57; 

        //finds line
        //rotates 90 degrees
        //gets ultrasonic distance from side to calculate distance
        //drives along line for distance (= fieldX-Chassis.sideAligner.getRangeInches()) to goal
        //rotates back to 0 degrees, facing goal
        
      }
    }
  }

  public void updateColorSensor() {
    color = colorSensor.getColor();

    isBlueLine = Utils.isColorMatch(color, Statics.TAPE_BLUE, 0.06);
    isRedLine = Utils.isColorMatch(color, Statics.TAPE_RED, 0.06);
    isWhiteLine = Utils.isColorMatch(color, Statics.TAPE_WHITE, 0.02);
    
  }

  public void Pixy() {
    var wait = false;
    var signature = 0;
    var blocksToReturn = 1;
    var colorTracker = _pixy2.getCCC();
    var blockCount = colorTracker.getBlocks(wait, signature, blocksToReturn);
    if (blockCount > 0) //blocks were found for the specified signature
    {
      var block = colorTracker.getBlocks().get(0);
      SmartDashboard.putNumber("Signature ID", block.getSignature());
      SmartDashboard.putNumber("X", block.getX());
      SmartDashboard.putNumber("Y", block.getY());
      SmartDashboard.putNumber("Width", block.getWidth());
      SmartDashboard.putNumber("Height", block.getHeight());
    }
  }

  public void updateTop() {
    updateColorSensor();
  }

  public void postData() {

    SmartDashboard.putNumber("Front Ultrasonic", Chassis.frontAligner.getRangeInches());
    SmartDashboard.putNumber("Side Ultrasonic", Chassis.sideAligner.getRangeInches());
    SmartDashboard.putNumber("PID calculate", gyroPID.calculate(NavX.navx.getAngle()));
    SmartDashboard.putString("Current Gear", (Chassis.shifter.status == Status.FORWARD ? "Low" : "High"));
    SmartDashboard.putNumber("Angle", NavX.navx.getYaw());
    SmartDashboard.putString("Color Sensor (R,G,B)", color[0] + ", " + color[1] + ", " + color[2]);
    SmartDashboard.putBoolean("Is Blue Line Detected", isBlueLine);
    SmartDashboard.putBoolean("Is Red Line Detected", isRedLine);
    //SmartDashboard.putBoolean("Is White Line Detected", isWhiteLine);
    SmartDashboard.putNumber("target Angle", lastTargetAngle);
  }

  @Override
  public void testPeriodic() {
  }
}
