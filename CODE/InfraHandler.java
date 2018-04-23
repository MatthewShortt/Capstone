import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class InfraHandler {
	private final boolean DISPLAY_DIGIT = false;
    
    private Pin spiClk  = RaspiPin.GPIO_22; // Pin #31, clock
    private Pin spiMiso = RaspiPin.GPIO_27; // Pin #36, data in.  MISO: Master In Slave Out
    private Pin spiMosi = RaspiPin.GPIO_25; // Pin #37, data out. MOSI: Master Out Slave In
    private Pin spiCs   = RaspiPin.GPIO_21; // Pin #29, Chip Select
    
    private int ADC_CHANNEL = 0; // Between 0 and 7, 8 channels on the MCP3008
    
    private GpioController gpio;
    private GpioPinDigitalInput  misoInput        = null;
    private GpioPinDigitalOutput mosiOutput       = null;
    private GpioPinDigitalOutput clockOutput      = null;
    private GpioPinDigitalOutput chipSelectOutput = null;
    
    public InfraHandler() {
    	gpio = GpioFactory.getInstance();
        
        mosiOutput       = gpio.provisionDigitalOutputPin(spiMosi, "MOSI", PinState.LOW);
        clockOutput      = gpio.provisionDigitalOutputPin(spiClk,  "CLK",  PinState.LOW);
        chipSelectOutput = gpio.provisionDigitalOutputPin(spiCs,   "CS",   PinState.LOW);
        
        misoInput        = gpio.provisionDigitalInputPin(spiMiso, "MISO");
    }
    
    public void destruct() {
    	gpio.shutdown();
    }
    
    // Returns distance in cm, or -1 if out of range
    public double getDistance() {
    	double adcVal = read();
        double distance = -1;
		if (adcVal != 0) distance = 70.0/adcVal - 6; 
		if (distance > 150) distance = -1;
		return distance;
    }
    
    // Returns a value in volts
    private double read(){
      chipSelectOutput.high();
      
      clockOutput.low();
      chipSelectOutput.low();
    
      int adccommand = ADC_CHANNEL;
      adccommand |= 0x18; // 0x18: 00011000
      adccommand <<= 3;
      // Send 5 bits: 8 - 3. 8 input channels on the MCP3008.
      for (int i=0; i<5; i++) //
      {
        if ((adccommand & 0x80) != 0x0) // 0x80 = 0&10000000
          mosiOutput.high();
        else
          mosiOutput.low();
        adccommand <<= 1;      
        clockOutput.high();
        clockOutput.low();      
      }
  
      int adcOut = 0;
      for (int i=0; i<12; i++) // Read in one empty bit, one null bit and 10 ADC bits
      {
        clockOutput.high();
        clockOutput.low();      
        adcOut <<= 1;
  
        if (misoInput.isHigh())
        {
          // Shift one bit on the adcOut
          adcOut |= 0x1;
        }
        
        if (DISPLAY_DIGIT)
          System.out.println("ADCOUT: 0x" + Integer.toString(adcOut, 16).toUpperCase() + 
                                   ", 0&" + Integer.toString(adcOut, 2).toUpperCase());
      }
      chipSelectOutput.high();
  
      adcOut >>= 1; // Drop first bit
        
      adcOut = (int) (adcOut*3300/1024 + 0.5);  // Convert to mV
      	return 1.0*adcOut/1000;					// Convert to V
    }
}
