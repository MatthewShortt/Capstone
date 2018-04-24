

import java.io.IOException;
import java.util.Scanner;

import com.pi4j.io.gpio.*;

// The two PWM pins are GPIO26 and GPIO23, pin numbers 32 and 33 respectively.
// The current test code should spin a motor at 50% for 5 seconds, 25% for 5 seconds, then turn off.
public class WheelMotorControl extends Thread {


	private GpioController gpio;
	private GpioPinPwmOutput ende_pin;
	private GpioPinPwmOutput ldrive_pin;
	private GpioPinPwmOutput rdrive_pin;
	private GpioPinDigitalOutput rdir_pin;
	private GpioPinDigitalOutput ldir_pin;

	private BumpHandler bump;

	private volatile int state = 0;
	private int stateL;
	private boolean estop = false;

	public void endEffectorStart() {
		ende_pin.setPwm(152);  //Minimum reverse
	}

	public void endEffectorStop() {
		ende_pin.setPwm(160);  //Neutral
	}

	// The end effector must be armed again after calling this function
	public void endEffectorEStop() {
		ende_pin.setPwm(0);
	}
	public void run(){
		drive();
	}
	
	private void rearm() {
		endEffectorStop();
		
		System.out.println("Outputting PWM to rearm the ESC. Turn on the ESC, and then press enter after the beeps have finished. If you do not wait, you will not be able to use the end effector.");
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// The following is meant to be called from a thread in Path Planning. The idea is, we need to stop briefly 
	// before switching direction, and speed up gradually.
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////	

	// The states in order of precedence: 
	// | 0    | 1         | 2         | 3         | 4        | 5         | 6        | 7       |
	// | stop | driveSlow | driveFast | bankRight | bankLeft | turnRight | turnLeft | reverse |
	public void drive() {
		long stopTimer = System.currentTimeMillis();
		boolean stopTiming = false;
		long slowTimer = System.currentTimeMillis();
		boolean slowTiming = false;
		boolean bump_state = false;
		int prev_state = 0;
		while (true) {
			stateL=state;
			bump_state = bump.getBump();
			if (estop || bump_state) {
				endEffectorEStop();
				stopDrive();
				stateL = 0;
				estop = true;
			}
			if (estop && !bump_state) {
				// Note that the robot will not move until rearmed
				rearm();
				stateL = 0;
				estop = false;
				if (bump.getBump()) continue;
			}
			
			if (stateL == 0) {
				stopDrive();
				prev_state = 0;
				slowTiming = false;
				stopTiming = false;
			} else {
				// Check if a new stateL has been requested
				if (stateL != prev_state) {
					// Start counting, for those that need it
					// If 5/6/7 has been entered from any stateL, stop for 250 ms
					// If the previous stateL was 5/6/7, stop for 250 ms
					// If 1 is entered, continue (no count)
					// If 2 is entered from 0/5/6/7, slow for 250 ms
					// Not sure about banking yet

					if (stateL == 5 || stateL == 6 || stateL == 7 || prev_state == 5 || prev_state == 6 || prev_state == 7) {
						stopTiming = true;
						stopTimer = System.currentTimeMillis();
						stopDrive();
					} else if (stateL == 1) {
						driveSlow();
					} else if (stateL == 2 && prev_state == 0) {
						driveSlow();
						slowTimer = System.currentTimeMillis();
						slowTiming = true;
					} else if (stateL == 2 && prev_state == 1) {
						driveFast();
					} else {
						stopDrive();
					}

					prev_state = stateL;

				} else {
					// If the stop timer ends 
					if (stopTiming == true && System.currentTimeMillis() >= stopTimer + 250) {
						if (stateL == 1) {
							driveSlow();
						} else if (stateL == 2) {
							driveSlow();
							slowTimer = System.currentTimeMillis();
							slowTiming = true;
						} else if (stateL == 5) {
							turnRight();
						} else if (stateL == 6) {
							turnLeft();
						} else if (stateL == 7) {
							reverse();
						} else {
							stopDrive();
						}
						stopTiming = false;
					}

					// If the slow timer ends (currently only for driveFast)
					if (slowTiming == true && System.currentTimeMillis() >= slowTimer + 250) {
						slowTiming = false;
						driveFast();
					}
				}
			}
		}
	}

	private void stopDrive() {
		ldrive_pin.setPwm(0);
		rdrive_pin.setPwm(0);
		rdir_pin.setState(PinState.LOW);
		ldir_pin.setState(PinState.LOW);
	}

	private void turnRight() {
		rdir_pin.setState(PinState.HIGH);
		ldir_pin.setState(PinState.LOW);
		ldrive_pin.setPwm(50*20);
		rdrive_pin.setPwm(50);
	}

	private void turnLeft() {
		rdir_pin.setState(PinState.LOW);
		ldir_pin.setState(PinState.HIGH);
		ldrive_pin.setPwm(50*20);
		rdrive_pin.setPwm(50);
	}

	private void driveFast() {
		rdir_pin.setState(PinState.LOW);
		ldir_pin.setState(PinState.LOW);
		ldrive_pin.setPwm(100*20);
		rdrive_pin.setPwm(100);
	}

	private void driveSlow() {
		rdir_pin.setState(PinState.LOW);
		ldir_pin.setState(PinState.LOW);
		ldrive_pin.setPwm(50*20);
		rdrive_pin.setPwm(50);
	}

	private void reverse() {
		rdir_pin.setState(PinState.HIGH);
		ldir_pin.setState(PinState.HIGH);
		ldrive_pin.setPwm(50*20);
		rdrive_pin.setPwm(50);
	}

	public void changeDriveState(int new_state) {
		state = new_state;
	}


	public WheelMotorControl() {
		gpio = GpioFactory.getInstance();

		ende_pin = gpio.provisionPwmOutputPin(RaspiPin.GPIO_26); //PWM0
		ldrive_pin = gpio.provisionPwmOutputPin(RaspiPin.GPIO_23); //PWM1
		rdrive_pin = gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_03);
		ldir_pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "DirL", PinState.LOW);  //LOW=forward
		rdir_pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "DirR", PinState.LOW);
		
		bump = new BumpHandler();

		com.pi4j.wiringpi.Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS);
		com.pi4j.wiringpi.Gpio.pwmSetRange(2000);
		com.pi4j.wiringpi.Gpio.pwmSetClock(192);

		rdrive_pin.setPwmRange(100);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Make sure motors are turned off to start
		ldrive_pin.setPwm(0);
		rdrive_pin.setPwm(0);

		ende_pin.setPwm(160);

		System.out.println("Turn on the ESC NOW. Press enter AFTER the beeps are done.");
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

}
