import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;

public class EndEffectorControl {
	private static int PIN = 1;  //Change this
	private static int SPEED = 50;
	
	private final GpioController gpio;
	private final GpioPinDigitalOutput motorPin;
	
	public EndEffectorControl() {
		Gpio.wiringPiSetup();
		SoftPwm.softPwmCreate(PIN, 0, 100);
	}
	
	public startEndEffector() {
		SoftPwm.softPwmWrite(PIN, SPEED);
	}
	
	public stopEndEffector() {
		SoftPwm.softPwmWrite(PIN, 0);
	}
}