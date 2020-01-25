/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

//WPILib
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import com.revrobotics.ColorSensorV3;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.controller.PIDController;
//Internal
import frc.robot.hardware.*;
import frc.robot.hardware.NavX;
import frc.robot.hardware.Gamepad.Key;
import frc.robot.hardware.MotorNG.Model;
import frc.robot.hardware.Solenoid.Status;
import frc.robot.software.*;

public class Robot extends TimedRobot {

  //Shared (Make sure these are "public" so that Core can take them in, which allows global access to happen)
  public Gamepad gp1,gp2;
  //Private

  public static double driveSpeed = 1.0;

  //Drive mode GUI variables and setup
  public static final String kDefaultDrive = "Default";
  public static final String kCustomDrive = "Right Stick Drive";
  public static final String kCustomDrive1 = "Left Stick Drive";
  public static final String kCustomDrive2 = "Both Stick Drive";
  public String m_driveSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  public PIDController gyroPID;

  @Override
  public void robotInit() {


    NavX.initialize();
    NavX.navx.getAngle();

    gyroPID = new PIDController(2, 10, 1); //variables you test
    gyroPID.setSetpoint(90);


    //Drive mode GUI setup
    m_chooser.setDefaultOption("Default", kDefaultDrive);
    m_chooser.addOption("Right Stick Drive", kCustomDrive);
    m_chooser.addOption("Left Strick Drive", kCustomDrive1);
    m_chooser.addOption("Both Strick Drive", kCustomDrive2);
    SmartDashboard.putData("Drive choices", m_chooser);
    System.out.println("Drive Selected: " + m_driveSelected);

    postData();

    Core.initialize(this);

    Chassis.initialize();
    gp1 = new Gamepad(0);
    gp2 = new Gamepad(1);

    Camera.initialize();
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
    //TODO: ACTUALLY DO SOME KIND OF AUTON
    teleopPeriodic();
  }


  @Override
  public void teleopPeriodic() {
    
    gp1.fetchData();

    updateBottom();
    updateTop();

    postData();

  }

   //All code for driving
   public void updateBottom() {

    //Gearbox
    if(gp1.isKeyToggled(Key.DPAD_UP)) {
      Chassis.shift();
    }

    //raise drive speed
    if(gp1.isKeyToggled(Key.RB)) {
      if(driveSpeed + 0.25 <= 1.0) {
        driveSpeed += 0.25;
        Chassis.setSpeedFactor(driveSpeed);
      }
    }
    //lower drive speed
    else if(gp1.isKeyToggled(Key.LB)) {
      if(driveSpeed - 0.25 >= 0) {
        driveSpeed -= 0.25;
        Chassis.setSpeedFactor(driveSpeed);
      }
    }
    
    // Assistive Autonomous
    if (gp1.isKeyToggled(Key.DPAD_LEFT)) {
      AutoPilot.turnRobotByTime(true);
    } 
    else if (gp1.isKeyToggled(Key.DPAD_RIGHT)) {
      AutoPilot.turnRobotByTime(false);
    }
    // Drive control 
    else {
      double x = 0,y = 0;
      switch(m_driveSelected){
        //Right Stick Drive
        case kCustomDrive:
           y = Utils.mapAnalog(gp1.getValue(Key.J_RIGHT_Y));
           x = Utils.mapAnalog(gp1.getValue(Key.J_RIGHT_X));
          break;
        //Left Stick Drive
        case kCustomDrive1:
           y = Utils.mapAnalog(gp1.getValue(Key.J_LEFT_Y));
           x = Utils.mapAnalog(gp1.getValue(Key.J_LEFT_X));
          break;
        //Both Stick Drive
        case kCustomDrive2:
           y = Utils.mapAnalog(gp1.getValue(Key.J_LEFT_Y));
           x = Utils.mapAnalog(gp1.getValue(Key.J_RIGHT_X));
          break;
        //Default is right stick drive
        case kDefaultDrive:
          y = Utils.mapAnalog(gp1.getValue(Key.J_RIGHT_Y));
          x = Utils.mapAnalog(gp1.getValue(Key.J_RIGHT_X));
          break;

      }
      Chassis.drive(y,x);
    }

    if (gp1.isKeyToggled(Key.A)){
      while(true) {
      Chassis.drive(gyroPID.calculate(NavX.navx.getAngle()), -gyroPID.calculate(NavX.navx.getAngle()));
      
      postData();
      if(gp1.isKeyHeld(Key.DPAD_DOWN)) {
        Chassis.stop();
        break;
      }
      }
    }
  }


  public void updateTop() {
    
  }
  
  public void postData() {
    //SmartDashboard.putNumber("Front Ultrasonic", Chassis.frontAligner.getRangeMM());
    //SmartDashboard.putNumber("Side Ultrasonic", Chassis.backAligner.getRangeMM());
    SmartDashboard.putNumber("P value: ", gyroPID.getP());
    SmartDashboard.putNumber("I value: ", gyroPID.getI());
    SmartDashboard.putNumber("D value: ", gyroPID.getD());
    SmartDashboard.putNumber("PID calculate", gyroPID.calculate(NavX.navx.getAngle()));
    SmartDashboard.putString("Current Gear", (Chassis.shifter.status == Status.FORWARD? "High" : "Low"));
  }

  @Override
  public void testPeriodic() {
  }
}
