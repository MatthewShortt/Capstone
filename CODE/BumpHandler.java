import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@HAVE THIS INITIALIZE A LISTENER
public class BumpHandler {
	
	private GpioController gpio;
	private GpioPinDigitalInput bump;
	
	public BumpHandler() {
		gpio = GpioFactory.getInstance();
        bump = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, "PinBump", PinPullResistance.PULL_DOWN);  //Pin 16
	}
	
	public void destruct() {
		gpio.shutdown();
	}

	public boolean getBump() {
		if (bump.getState() == PinState.LOW) return true;
		return false;
	}
}
