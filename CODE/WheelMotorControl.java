

import java.util.Scanner;

import com.pi4j.io.gpio.*;

// The two PWM pins are GPIO26 and GPIO23, pin numbers 32 and 33 respectively.
// The current test code should spin a motor at 50% for 5 seconds, 25% for 5 seconds, then turn off.
public class WheelMotorControl {
	
	public static class RunBump extends Thread {
        public void run() {
        	BumpHandler bump = new BumpHandler();
        	while(true) {
            	if (bump.getBump() == true) {
                	ende_pin.setPwm(0);
                	break;
                }
            }
        }
    }
	
	//Note: max motor velocity reduced to 90% to account for max current draw of ESCs
	//With the switch to treads, the below max velocity may no longer be accurate.
	//private final double MAX_MOTOR_VELOCITY = 19.77*0.9;  // in m/s
	//private final int MAX_TURN_RADIUS = 20000;			  // in mm
	//private final int DRIVE_STRAIGHT = 100000;		  	  // in mm
	//private final double WHEEL_SEPARATION = 0.2;	 	  // in m
	
	//private int robotRadius;
	//private double robotSpeed;
	
	public static GpioController gpio;
	public static GpioPinPwmOutput ende_pin;
	public static GpioPinPwmOutput ldrive_pin;
	public static GpioPinPwmOutput rdrive_pin;
	public static GpioPinDigitalOutput rdir_pin;
	public static GpioPinDigitalOutput ldir_pin;
	public static GpioPinDigitalOutput muxpin;  //Must be HIGH if end effector on
	
	private static boolean end_effector_on = false;
	
	//Just for testing purposes
	public static void main(String[] args) throws Exception {
		WheelMotorControl handler = new WheelMotorControl();
		/*gpio = GpioFactory.getInstance();
		
		ende_pin = gpio.provisionPwmOutputPin(RaspiPin.GPIO_26); //PWM0
		ldrive_pin = gpio.provisionPwmOutputPin(RaspiPin.GPIO_23); //PWM1
		rdir_pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "Dir1", PinState.LOW);
		ldir_pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "Dir2", PinState.LOW);
		muxpin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "Mux", PinState.LOW);
		
		com.pi4j.wiringpi.Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS);
        com.pi4j.wiringpi.Gpio.pwmSetRange(2000);
        com.pi4j.wiringpi.Gpio.pwmSetClock(192);
        
        Thread.sleep(1000);*/
        
        
        /*// Drive and bump
        ende_pin.setPwm(160);
        System.out.println("Set to neutral. Press enter to go...");
        System.in.read();
        ende_pin.setPwm(155);
        
        new RunBump().start();
        System.out.println("Bump the sensor or press enter to stop!");
        System.in.read();
        ende_pin.setPwm(0);*/
        
        // For trying forward/backward with new motors
        int pwm1 = 0;
        int pwm2 = 0;
        String input;
        System.out.println("Setting throttle to neutral (0/100) and forward. Enter desired number, or 1 to exit. Or e to toggle the end effector. Please only enter integers.");
        //ende_pin.setPwm(pwm1);
        //ldrive_pin.setPwm(pwm2);
        //rdir_pin.setState(PinState.HIGH);
        //ldir_pin.setState(PinState.HIGH);
        
        Scanner sc = new Scanner(System.in);
        
        input = sc.next();
        if (input.equals("e")) {
    		if (end_effector_on) {
    			handler.endEffectorStop();
    			//ende_pin.setPwm(160);
    			//end_effector_on = false;
    		}
    		else {
    			handler.endEffectorStart();
    			//ende_pin.setPwm(152);
    			//end_effector_on = true;
    		}
    	} else {
    		pwm1 = Integer.parseInt(input);
    		pwm2 = pwm1;
    	}
    	
        while (pwm1 != 1){
        	System.out.println("Drive speed:");
        	System.out.println(pwm1);
        	rdrive_pin.setPwm(pwm1);
        	ldrive_pin.setPwm(pwm2*20);
        	System.out.println("Change speed? Enter a number from 0 to 100. Toggle end effector? Enter e. Exit? Enter 1.");
        	input = sc.next();
        	if (input.equals("e")) {
        		if (end_effector_on) {
        			handler.endEffectorStop();
        			//ende_pin.setPwm(160);
        			//end_effector_on = false;
        		}
        		else {
        			handler.endEffectorStart();
        			//ende_pin.setPwm(152);
        			//end_effector_on = true;
        		}
        	} else {
        		pwm1 = Integer.parseInt(input);
        		pwm2 = pwm1;
        	}
        }
        
        System.out.println("Setting ESC input to 0 and exiting.\n");
        ende_pin.setPwm(0);
        ldrive_pin.setPwm(0);
        rdrive_pin.setPwm(0);
        rdir_pin.setState(PinState.LOW);
        ldir_pin.setState(PinState.LOW);
        
        sc.close();
        
        /*// For trying forward/backward
        int pwm = 160;
        System.out.println("Setting throttle to neutral. Enter desired number, or 1 to exit. Please only enter integers.");
        ende_pin.setPwm(pwm);
        Scanner sc = new Scanner(System.in);
        pwm = Integer.parseInt(sc.next());
        while (pwm != 1){
        	System.out.println("Changing speed:");
        	System.out.println(pwm);
        	ende_pin.setPwm(pwm);
        	System.out.println("Increase more? Enter a number. Exit? Enter 1.");
        	pwm = Integer.parseInt(sc.next());
        }
        System.out.println("Setting ESC input to 0 and exiting.\n");
        ende_pin.setPwm(0);
        
        sc.close();*/
        
        /*//Backwards speed test
        int pwm = 160;
        System.out.println("Setting throttle to neutral for backwards test. When you hear the beeps, enter 's' to speed up, enter to exit.");
        ende_pin.setPwm(pwm);
        Scanner sc=new Scanner(System.in);
        String input = sc.next();
        while (input.equals("s") && pwm > 120){
        	System.out.println("Increasing speed by 5: ");
        	pwm = pwm - 5;
        	System.out.println(pwm);
        	ende_pin.setPwm(pwm);
        	System.out.println("Increase more? Enter 's'");
        	input = sc.next();
        }
        System.out.println("Setting ESC input to 0 and exiting.\n");
        ende_pin.setPwm(0);
        
        sc.close();*/
        
        /*//Forewards speed test
        int pwm = 200;
        System.out.println("Setting throttle to neutral for forewards test. When you hear the beeps, enter 's' to speed up, enter to exit.");
        ende_pin.setPwm(pwm);
        while ((char)System.in.read()=='s' && pwm > 100){
        	System.out.println("Increasing speed by 5% of max.\n");
        	pwm = pwm + 5;
        	ende_pin.setPwm(pwm);
        }
        System.out.println("Input is already at max. Setting ESC input to 0 and exiting.\n");
        ende_pin.setPwm(0);*/
        
        
        /*// For setting the ESC throttle range
        ende_pin.setPwm(180);
        System.out.println("Max signal?");
        System.in.read();
        
        ende_pin.setPwm(240);
        
        System.out.println("Min signal?");
        System.in.read();
        
        ende_pin.setPwm(120);
        
        System.out.println("Stop?");
        System.in.read();
        
        ende_pin.setPwm(0);*/
        
        
        
		//ende_pin.setPwm(240);
		//ldrive_pin.setPwm(240);
		//System.out.println("Connect your power");
	    //System.in.read();
		
		//ende_pin.setPwm(120);
		//ldrive_pin.setPwm(120);
		//Thread.sleep(1000);
		//ende_pin.setPwm(0);
		//ldrive_pin.setPwm(0);
		
		//System.out.println("Arming");
	    //System.in.read();
	    //Thread.sleep(1000);
	    
	    //ende_pin.setPwm(140);
	    //ldrive_pin.setPwm(120);
	    //Thread.sleep(1500);
	    //ende_pin.setPwm(0);
	    //ldrive_pin.setPwm(0);
	    
		gpio.shutdown();
		
	}
	
	public int endEffectorStart() {
		ende_pin.setPwm(152);  //Minimum reverse
		return 0;
	}
	
	public int endEffectorStop() {
		ende_pin.setPwm(160);  //Neutral
		return 0;
	}
	
	// The end effector must be armed again after calling this function
	public int endEffectorEStop() {
		ende_pin.setPwm(0);
		return 0;
	}
	
	
	public WheelMotorControl() throws Exception {
		gpio = GpioFactory.getInstance();
		
		ende_pin = gpio.provisionPwmOutputPin(RaspiPin.GPIO_26); //PWM0
		ldrive_pin = gpio.provisionPwmOutputPin(RaspiPin.GPIO_23); //PWM1
		rdrive_pin = gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_03);
		ldir_pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "DirL", PinState.LOW);
		rdir_pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "DirR", PinState.LOW);
		
		com.pi4j.wiringpi.Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS);
        com.pi4j.wiringpi.Gpio.pwmSetRange(2000);
        com.pi4j.wiringpi.Gpio.pwmSetClock(192);
        
        rdrive_pin.setPwmRange(100);
        
        Thread.sleep(1000);
        
        //muxpin.setState(PinState.HIGH);
        ende_pin.setPwm(160);
        
        System.out.println("Turn on the ESC NOW. Press enter AFTER the beeps are done.");
        System.in.read();
        //muxpin.setState(PinState.LOW);
		
		// Make sure motors are turned off to start
		ldrive_pin.setPwm(0);
		rdrive_pin.setPwm(0);
		rdir_pin.setState(PinState.HIGH);
        ldir_pin.setState(PinState.HIGH);
		
		// Initialize values to stopped, driving straight
		//robotSpeed = 0;
		//robotRadius = DRIVE_STRAIGHT;
	}
	
	public void destruct() {
		ende_pin.setPwm(0);
		ldrive_pin.setPwm(0);
		rdrive_pin.setPwm(0);
		ldir_pin.setState(PinState.LOW);
		rdir_pin.setState(PinState.LOW);
		
		gpio.shutdown();
	}
	
	
	
	/*
	 * The main function used by PathPlanning
	 */
/*	public int drive(double velocity, int radius) {
		// Check that the arguments are within bounds
		if (velocity > MAX_MOTOR_VELOCITY || velocity < 0 ||
				((Math.abs(radius) < WHEEL_SEPARATION || Math.abs(radius) > MAX_TURN_RADIUS) 
						&& radius != DRIVE_STRAIGHT) ) {
			return 1;
		}
		
		// Determine the required velocities of the two motors
		double motor1Speed = 0;
		double motor2Speed = 0;
		
		if (velocity != 0) {  //If it is 0, we don't need to do anything.
			if (radius == DRIVE_STRAIGHT) {
				motor1Speed = velocity;
				motor2Speed = velocity;
			} else if (radius == 0) {
				motor1Speed = velocity;
				motor2Speed = -velocity;
			} else {
				double diff = (WHEEL_SEPARATION*velocity)/(2*radius);
				double check = velocity + Math.abs(diff);
				
				if (check > MAX_MOTOR_VELOCITY) {  //One of the motor velocities will be too high
					if (velocity + diff > velocity - diff) {
						motor1Speed = MAX_MOTOR_VELOCITY;
						motor2Speed = (velocity - diff)*(velocity + diff)/MAX_MOTOR_VELOCITY;
					} else {
						motor2Speed = MAX_MOTOR_VELOCITY;
						motor1Speed = (velocity - diff)*(velocity + diff)/MAX_MOTOR_VELOCITY;
					}
				} else {
					motor1Speed = velocity + diff;
					motor2Speed = velocity - diff;
				}
			}
		}
		
		motorPWM(motor1Speed, 1);
		motorPWM(motor2Speed, 2);
		return 0;
	}
	
	
	
	/*
	 * The function which sets the PWM of the motors based on desired speed
	 * These will be slightly more complex if the motors are allowed to reverse direction
	 */
/*	private void motorPWM(double motorSpeed, int motorNumber) {
				
		int dutyCycle = (int) (motorSpeed*900/MAX_MOTOR_VELOCITY + 0.5);
		
		// We shouldn't need this, but just in case, do some error checking
		if (dutyCycle > 900) dutyCycle = 900;
		else if (dutyCycle < 0) dutyCycle = 0;
		
		if (motorNumber == 1) {
			ende_pin.setPwm(dutyCycle);
		} else if (motorNumber == 2){
			ldrive_pin.setPwm(dutyCycle);
		}
	}	*/
	
	
	/*
	 * Getters!!!
	 */
/*	public int getRadius() {
		return robotRadius;
	}
	
	public double getSpeed() {
		return robotSpeed;
	}*/
}