import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class InfraHandler {
	private final static boolean DISPLAY_DIGIT = false;
    private final static boolean DEBUG         = false;
    
    private static Pin spiClk  = RaspiPin.GPIO_22; // Pin #31, clock
    private static Pin spiMiso = RaspiPin.GPIO_27; // Pin #36, data in.  MISO: Master In Slave Out
    private static Pin spiMosi = RaspiPin.GPIO_25; // Pin #37, data out. MOSI: Master Out Slave In
    private static Pin spiCs   = RaspiPin.GPIO_21; // Pin #29, Chip Select
    
    private static int ADC_CHANNEL = 0; // Between 0 and 7, 8 channels on the MCP3008
    
    private static GpioPinDigitalInput  misoInput        = null;
    private static GpioPinDigitalOutput mosiOutput       = null;
    private static GpioPinDigitalOutput clockOutput      = null;
    private static GpioPinDigitalOutput chipSelectOutput = null;
    
    private static boolean go = true;
    
    public static void main(String[] args)
    {
      GpioController gpio = GpioFactory.getInstance();
      
      mosiOutput       = gpio.provisionDigitalOutputPin(spiMosi, "MOSI", PinState.LOW);
      clockOutput      = gpio.provisionDigitalOutputPin(spiClk,  "CLK",  PinState.LOW);
      chipSelectOutput = gpio.provisionDigitalOutputPin(spiCs,   "CS",   PinState.LOW);
      
      misoInput        = gpio.provisionDigitalInputPin(spiMiso, "MISO");
      
      Runtime.getRuntime().addShutdownHook(new Thread()
                                           {
                                             public void run()
                                             {
                                               System.out.println("Shutting down.");
                                               go = false;
                                             }
                                           });
      double distance;
      double adcVal;
      
      
      
      double max = 0;
      double min = 160;
      double sum = 0;
      for (int i = 0; i < 30; i++) {
    	  adcVal = read();
          distance = -1;
  	      if (adcVal != 0) distance = 70.0/adcVal - 6; 
  	      if (distance > 150) distance = -1;
  	      
  	      sum += distance;
  	      if (distance < min) min = distance;
  	      if (distance > max) max = distance;
  	      
  	      System.out.println(distance + " cm");
          try { Thread.sleep(100L); } catch (InterruptedException ie) { ie.printStackTrace(); }
      }
      System.out.println("average: " + sum/30);
      System.out.println("min: " + min);
      System.out.println("max: " + max);
      
      
      
      
      /*while (go)
      {
        adcVal = read();
        distance = -1;
	    if (adcVal != 0) distance = 70.0/adcVal - 6; 
	    
	    if (DEBUG) System.out.println("From ADC in volts:" + Double.toString(adcVal));        
	    
	    System.out.println(distance + " cm");
        try { Thread.sleep(100L); } catch (InterruptedException ie) { ie.printStackTrace(); }
      }
      System.out.println("Bye...");*/
      gpio.shutdown();
    }   
    
    // Returns a value in volts
    private static double read(){
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
