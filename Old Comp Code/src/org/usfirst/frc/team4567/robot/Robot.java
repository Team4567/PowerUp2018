/*----v------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team4567.robot;
// Package, imported all necessary features
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.GenericHID.Hand;
/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {
	// Autonomous selection features
	private static final String kLeft = "Left";
	private static final String kRight = "Right";
	private static final String kCenter = "Center";
	private String m_autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();
	//Drivetrain
	WPI_TalonSRX LeftC= new WPI_TalonSRX(2);
	WPI_TalonSRX RightC= new WPI_TalonSRX(1);
	VictorSP LeftP= new VictorSP(1);
	VictorSP RightP= new VictorSP(0);
	//Gearbox
	DoubleSolenoid pBL = new DoubleSolenoid(1,0);
	DoubleSolenoid pBR= new DoubleSolenoid(3,2);
	//Grouping Sides for DifferentialDrive
	SpeedControllerGroup L= new SpeedControllerGroup(LeftC,LeftP);
	SpeedControllerGroup R= new SpeedControllerGroup(RightC,RightP);
	DifferentialDrive roboDrive = new DifferentialDrive(L,R);
	//Controller
	XboxController XbC = new XboxController(0);
	Joystick leftStick = new Joystick(0);
	Joystick rightStick = new Joystick(1);
	//Scoring
	Spark in= new Spark(2);
	Spark shoot= new Spark(4);
	//Climbing
	VictorSP climb2= new VictorSP(6);
	Talon climb= new Talon(3);
	// Other Variables
	boolean AutoOn=true;
	Timer AutoTimer = new Timer();
	double tM= -0.02; //DO NOT MOVE OVER 0.25 OR UNDER -0.4, Turns. This was a precaution to adjust 90 degree turns with the new material we were driving on.
	boolean rev=false;
	double aWait; //aWait was a delay feature in case another robot wanted to perform their autonomous ahead of us to avoid collision.

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		m_chooser.addObject("Left", kLeft);
		m_chooser.addObject("Right", kRight);
		m_chooser.addDefault("Center", kCenter);
		SmartDashboard.putData("Auto choices", m_chooser);
		aWait = 0;
	}


	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	
	@Override
	public void autonomousInit() {
		m_autoSelected = m_chooser.getSelected();
		System.out.println("Auto selected: " + m_autoSelected);
		AutoTimer.reset();
		AutoTimer.start();
		AutoOn=true;
		
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		/*AUTONOMOUS
		 * 2 KEY FACTORS
		 * POSITION ON FIELD
		 * In Driver Station, there will be 3 modes you can select, referenced in code as kLeft, kRight, and kCenter. The robot uses that to select which piece of code it runs
		 * SCALE ORIENTATION
		 * The scale/Switches randomized which side was left or right each match, sending a 3 letter string of Ls and Rs right before auto started. The robot takes the first letter, gameData.charAt(0), 
		 * and picks which way it turns
		 * 
		 * AutoOn was just a safety I had because the robot wouldn't respond to teleOp Commands after running auto.
		 * */
		
		String gameData;
		gameData = DriverStation.getInstance().getGameSpecificMessage();
	if(AutoOn){
		 if(m_autoSelected==kLeft){
			 // This chunk is in each one. Sets the gearboxes to the low gear.
			 if(AutoTimer.get()<=1){
				pBL.set(DoubleSolenoid.Value.kForward);
				pBR.set(DoubleSolenoid.Value.kForward);
			}else{
				pBL.set(DoubleSolenoid.Value.kOff);
				pBR.set(DoubleSolenoid.Value.kOff);
			}
			// Without encoders, everything was determined by trial and error of a timer and the PWM Signals. Causes of error incl- Battery %, Condition of field, Jams
			 // If robot was on the same side as it's appropriate goal, it would attempt a shot. Otherwise, it just goes forward to get auto pts by crossing line.
			if(gameData.charAt(0)=='L'){
				if(AutoTimer.get()<=3+aWait&&AutoTimer.get()>0+aWait){
					roboDrive.arcadeDrive(0.75, 0);	
				} else if(AutoTimer.get()>3 && AutoTimer.get()<=3.4+tM){
				 roboDrive.arcadeDrive(0, -1);
				}else if(AutoTimer.get()>3.4+tM && AutoTimer.get()<=4.4+tM){
					roboDrive.arcadeDrive(0.65, 0);
				}else if(AutoTimer.get()>4.4+tM && AutoTimer.get()<=5.4+tM){
					shoot.set(0.7);
					roboDrive.arcadeDrive(0, 0);
				}else{
					roboDrive.arcadeDrive(0, 0);
					shoot.set(0);
				}
			}else{
				if(AutoTimer.get()<=3.15+aWait&&AutoTimer.get()>0+aWait){
					roboDrive.arcadeDrive(0.75, 0);	}
				else {
					roboDrive.arcadeDrive(0, 0);
					shoot.set(0);
					AutoOn=false;
				}
			}
			 // Repeat process starting on right side
		}else if(m_autoSelected==kRight){
			if(AutoTimer.get()<=1){
				pBL.set(DoubleSolenoid.Value.kForward);
				pBR.set(DoubleSolenoid.Value.kForward);
			}else{
				pBL.set(DoubleSolenoid.Value.kOff);
				pBR.set(DoubleSolenoid.Value.kOff);
		}
			if(gameData.charAt(0)=='R'){
				if(AutoTimer.get()<=3&&AutoTimer.get()>0){
					roboDrive.arcadeDrive(0.75, 0);	
				}else if(AutoTimer.get()>3 && AutoTimer.get()<=3.4+tM){
					roboDrive.arcadeDrive(0, 1);
				}else if(AutoTimer.get()>3.4+tM && AutoTimer.get()<=4.4+tM){
					roboDrive.arcadeDrive(0.65, 0);
				}else if(AutoTimer.get()>4.4+tM && AutoTimer.get()<=5.4+tM){
					shoot.set(0.7);
					roboDrive.arcadeDrive(0, 0);
				}else{
					roboDrive.arcadeDrive(0, 0);
					shoot.set(0);
				}
			}else{
				if(AutoTimer.get()<=3.15&&AutoTimer.get()>0){
					roboDrive.arcadeDrive(0.75, 0);	
				}else{
					roboDrive.arcadeDrive(0, 0);
					shoot.set(0);
					AutoOn=false;
				}
			}
			//Center Position- Never worked, hit other robots
		}else{
			if(AutoTimer.get()<=1){
				pBL.set(DoubleSolenoid.Value.kForward);
				pBR.set(DoubleSolenoid.Value.kForward);
			}else{
				pBL.set(DoubleSolenoid.Value.kOff);
				pBR.set(DoubleSolenoid.Value.kOff);
			}
				if(gameData.charAt(0)=='L'){
					if(AutoTimer.get()<=1.5+aWait){
						roboDrive.arcadeDrive(0.75, 0);	
					}else if(AutoTimer.get()>1.5+aWait&&AutoTimer.get()<=1.9+tM+aWait){
						roboDrive.arcadeDrive(0,1);	
					}else if(AutoTimer.get()>1.9+tM+aWait&&AutoTimer.get()<=4.5+tM+aWait){
						roboDrive.arcadeDrive(0.75,0);	
					}else if(AutoTimer.get()>4.5+tM+aWait&&AutoTimer.get()<=4.9+(2*tM)+aWait){
						roboDrive.arcadeDrive(0,-1);	
					}else if(AutoTimer.get()>4.9+(2*tM)+aWait&&AutoTimer.get()<=6.9+(2*tM)+aWait){
						roboDrive.arcadeDrive(0.75,0);	
					} else {roboDrive.arcadeDrive(0, 0);	
						AutoOn=false;
					}
				} else if(gameData.charAt(0)=='R'){
					if(AutoTimer.get()<=1.5){
						roboDrive.arcadeDrive(0.75, 0);	
					}else if(AutoTimer.get()>1.5+aWait&&AutoTimer.get()<=1.9+tM+aWait){
						roboDrive.arcadeDrive(0,-1);	
					}else if(AutoTimer.get()>1.9+tM+aWait&&AutoTimer.get()<=4.5+tM+aWait){
						roboDrive.arcadeDrive(0.75,0);	
					}else if(AutoTimer.get()>4.5+tM+aWait&&AutoTimer.get()<=4.9+(2*tM)+aWait){
						roboDrive.arcadeDrive(0,1);	
					}else if(AutoTimer.get()>4.9+(2*tM)+aWait&&AutoTimer.get()<=6.9+(2*tM)+aWait){
						roboDrive.arcadeDrive(0.75,0);	
					}else{roboDrive.arcadeDrive(0, 0);	
						AutoOn=false;}
					}
				
			}
		 }
	}
		
		
	
		
	

	/**
	 * This function is called periodically during operator control.
	 * The main controls
	 */
	@Override
	public void teleopInit() {
		
	}
	public void teleopPeriodic() {
		//Joystick control
		// Rev allows for the relative forward movement to be either side of the robot, shooting or retrieving
		/* Xbox Controller Buttons can give off a boolean in 3 modes
		 * getBUTTON()- Whenever it is pressed, true
		 * getBUTTONPressed()- once the button is pressed, it is true for 1 moment. Holding the button doesn't work. Effective to not have switch buttons not alternate mult times in 1 press
		 * getBUTTONReleased()- when you let go, it does this. Haven't found a need for this yet
		 * */
		if (XbC.getStartButtonPressed()) {
		    rev = !rev;
		}
		if (rev) {
		    roboDrive.arcadeDrive(leftStick.getY(),-1*leftStick.getX());
		} else {
		    roboDrive.arcadeDrive(-1*leftStick.getY(),-1*leftStick.getX());
		}
		//Strap Climber
		if(XbC.getXButton()){
			climb.set(1);
		} else if(XbC.getBButton()){
			climb.set(-1);
		}else {
			climb.set(0);
		}
		if(XbC.getAButton()){
			climb2.set(-0.15);
		} else if(XbC.getYButton()){
			climb2.set(0.25);
		}else {
			climb2.set(0);
		}
		//Gearbox Pneumatics
		if(XbC.getTriggerAxis(Hand.kLeft)>0.5){
			pBL.set(DoubleSolenoid.Value.kForward);
			pBR.set(DoubleSolenoid.Value.kForward);
		} else if (XbC.getBumper(Hand.kLeft)) {
			pBL.set(DoubleSolenoid.Value.kReverse);
			pBR.set(DoubleSolenoid.Value.kReverse);
		} else {
			pBL.set(DoubleSolenoid.Value.kOff);
			pBR.set(DoubleSolenoid.Value.kOff);
		}
		//Shooting Wheels
		if(XbC.getBumper(Hand.kRight)){
			in.set(-1);
			shoot.set(-0.30);
		}else{
			in.set(XbC.getTriggerAxis(Hand.kRight));
			shoot.set(0.75*XbC.getTriggerAxis(Hand.kRight));
		}
		
	
}}
