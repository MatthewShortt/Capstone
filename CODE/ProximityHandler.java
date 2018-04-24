import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;

//GPIO_00 for trigger (pin 11), GPIO_02 for echo (pin 13)
public class ProximityHandler {
	private final int PULSE = 10000;  			// ns = 10us
	private final int SPEED_OF_SOUND = 34029;	// cm/s
	
	private GpioController gpio;
	private GpioPinDigitalInput echo;
	private GpioPinDigitalOutput trigger;
	
	public ProximityHandler() {
        gpio = GpioFactory.getInstance();
		
        trigger = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "PinTrigger", PinState.LOW);
        echo = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, "PinEcho", PinPullResistance.PULL_DOWN);
	}
	
	public void destruct() {
		gpio.shutdown();
	}
	
	public double getDistance() {
		//If echo pin is high, error
		if (echo.getState() == PinState.HIGH) return -1;
		
		long distance = 0;
		
		//Output 0 to trigger pin, for recommended time between samples
		trigger.setState(PinState.LOW);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
			return -4;
		}
		
		//Set trigger pin to 1
		trigger.setState(PinState.HIGH);
		// Perform a busy wait for 10us
		long start = System.nanoTime();
		while(System.nanoTime() - start < PULSE);
		//Set trigger pin to 0
		trigger.setState(PinState.LOW);
		
		long startWait = System.nanoTime();
		start = startWait;
		long stop = startWait;
		//Wait for the echo pulse to start - if low for more than 20ms, error
		while (echo.getState() == PinState.LOW) {
			start = System.nanoTime();
			if (start >= startWait + 1000000L*2) return -2;
        }
		//Wait for the echo pulse to end - if high for more than 20ms, error
        while (echo.getState() == PinState.HIGH) {
            stop = System.nanoTime();
            if (stop >= start + 1000000L*24) return -3;
        }
		
		//Length of returned pulse, in ns
        long diff = stop - start;
        distance = diff * SPEED_OF_SOUND;
		
        return distance/2.0/(1000000000L);
    }
}