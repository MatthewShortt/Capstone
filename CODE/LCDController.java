import java.io.*;

public class LCDController {
	
	private final String filename = "j2p.txt";
	
	public void setLCD(String scenario) {
		String printnum;
		switch (scenario) {
	        case "comms":  printnum = "0";
	                 break;
	        case "weight": printnum = "1";
	                 break;
	        case "estop":  printnum = "2";
	                 break;
	        case "ultra":  printnum = "3";
	                 break;
	        case "infra":  printnum = "4";
	                 break;
	        case "camera": printnum = "5";
	                 break;
	        case "ende":   printnum = "6";
            		 break;
	        case "drive":  printnum = "7";
	                 break;
	        case "paused": printnum = "8";
	                 break;
	        case "fetching": printnum = "9";
	                 break;
	        case "obstacle": printnum = "10";
	                 break;
	        default: printnum = "";
	                 break;
	    }
		writeText(printnum);
	}
    	
    private void writeText(String text) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(new File(filename)));
			w.write(text);
			w.close();	
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}