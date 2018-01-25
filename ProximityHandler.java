import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;

//GPIO17 for trigger, GPIO10 for echo
public class ProximityHandler {
	private final int PULSE = 10000;  			// ns = 10us
	private final int SPEED_OF_SOUND = 34029;	// cm/s
	
	private int triggerPin = 17;
	private int echoPin = 10;
	
	GpioController gpio;
	GpioPinDigitalInput echo;
	GpioPinDigitalOutput trigger;
	
	public ProximityHandler() {
		gpio = GpioFactory.getInstance();
		
        trigger = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_17, "PinTrigger", PinState.LOW);
        echo = gpio.provisionDigitalInputPin(RaspiPin.GPIO_10, "PinEcho", PinPullResistance.PULL_UP);
        //It really doesn't make sense to me that this is pull up
        
/*		trigger = (GPIOPin) DeviceManager.open(new GPIOPinConfig(0, triggerPin, 
				GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL,
				       GPIOPinConfig.TRIGGER_NONE, false));

		echo = (GPIOPin) DeviceManager.open(new GPIOPinConfig(0, echoPin, 
				GPIOPinConfig.DIR_INPUT_ONLY, GPIOPinConfig.MODE_INPUT_PULL_UP, 
				GPIOPinConfig.TRIGGER_NONE, false));*/
	}
	
	public void destruct() {
		gpio.shutdown();
	}
	
	public double getDistance() {
		//If echo pin is high, error
		if (echo.getState() == PinState.HIGH) return -1;
		
		double distance = 0;
		
		//Output 0 to trigger pin, for recommended time between samples
		trigger.setState(PinState.LOW);
		Thread.sleep(50);
		
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
			if (start >= startWait + 1000000L*2) return -1;
	    }
		//Wait for the echo pulse to end - if high for more than 20ms, error
	    while (echo.getState() == PinState.HIGH) {
	        stop = System.nanoTime();
	        if (stop >= start + 1000000L*2) return -1;
	    }
		
		//Length of returned pulse, in ns
	    long diff = stop - start;
		distance = (diff/2/1000000000L) * SPEED_OF_SOUND;
		
		return distance;
	}
}
