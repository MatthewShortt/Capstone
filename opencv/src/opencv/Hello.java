package opencv;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
public class Hello
{
	//initial min and max HSV filter values.
	//these will be changed using trackbars
	///*yellow
	int H_MIN = 13;
	int H_MAX = 31;
	int S_MIN = 94;
	int S_MAX = 206;
	int V_MIN = 61;
	int V_MAX = 132;
	//*/
	/*orange
	int H_MIN = 0;
	int H_MAX = 72;
	int S_MIN = 105;
	int S_MAX = 233;
	int V_MIN = 161;
	int V_MAX = 238;
	//*/
	/*orange
		int H_MIN =131;
		int H_MAX = 183;
		int S_MIN = 80;
		int S_MAX = 179;
		int V_MIN = 98;
		int V_MAX = 255;
		//*/
	///*blue
	int H_MIN1 = 88;
	int H_MAX1 = 126;
	int S_MIN1 = 192;
	int S_MAX1 = 255;
	int V_MIN1 = 82;
	int V_MAX1 = 255;
	//*/
	/*red
		int H_MIN = 88;
		int H_MAX = 126;
		int S_MIN = 192;
		int S_MAX = 255;
		int V_MIN = 68;
		int V_MAX = 255;
		//*/
	/*test
	public int H_MIN = 0;
	public int H_MAX = 255;
	public int S_MIN = 0;
	public int S_MAX = 255;
	public int V_MIN = 0;
	public int V_MAX = 255;
	//*/
	JFrame frame;	
	JLabel lbl1=new JLabel();
	JLabel lbl2=new JLabel();
	//default capture width and height
	int FRAME_WIDTH = 640;
	int FRAME_HEIGHT = 480;
	//max number of objects to be detected in frame
	int MAX_NUM_OBJECTS=50;
	//minimum and maximum object area
	int MIN_OBJECT_AREA = 40*40;
	int MAX_OBJECT_AREA = 200*200;
	//names that will appear at the top of each window
	String windowName = "Original Image";
	String windowName1 = "HSV Image";
	String windowName2 = "Thresholded Image";
	String windowName3 = "After Morphological Operations";
	String trackbarWindowName = "Trackbars";

	private String intToString(int number){
		return number+"";
	}
	void filterChange(Mat cur, Mat pre){
		cur=cur.mul(pre);
	}
	void drawObject(int x, int y,Mat frame,Scalar c){

		//use some of the openCV drawing functions to draw cross hairs
		//on your tracked image!

		//UPDATE:JUNE 18TH, 2013
		//added 'if' and 'else' statements to prevent
		//memory errors from writing off the screen (ie. (-25,-25) is not within the window!)
		int h=(int)frame.get(FRAME_WIDTH/2,FRAME_HEIGHT/2)[0];
		int s=(int)frame.get(FRAME_WIDTH/2,FRAME_HEIGHT/2)[1];
		int l=(int)frame.get(FRAME_WIDTH/2,FRAME_HEIGHT/2)[2];
		Imgproc.circle(frame,new Point(x,y),20,c,2);
		if(y-25>0)
			Imgproc.line(frame,new Point(x,y),new Point(x,y-25),c,2);
		else Imgproc.line(frame,new Point(x,y),new Point(x,0),c,2);
		if(y+25<FRAME_HEIGHT)
			Imgproc.line(frame,new Point(x,y),new Point(x,y+25),c,2);
		else Imgproc.line(frame,new Point(x,y),new Point(x,FRAME_HEIGHT),c,2);
		if(x-25>0)
			Imgproc.line(frame,new Point(x,y),new Point(x-25,y),c,2);
		else Imgproc.line(frame,new Point(x,y),new Point(0,y),c,2);
		if(x+25<FRAME_WIDTH)
			Imgproc.line(frame,new Point(x,y),new Point(x+25,y),c,2);
		else Imgproc.line(frame,new Point(x,y),new Point(FRAME_WIDTH,y),c,2);		
		//Imgproc.putText(frame,"("+h+","+s+","+l+")",new Point(FRAME_WIDTH/2+30,FRAME_HEIGHT/2),1,1,c,2);
		//Imgproc.circle(frame,new Point(FRAME_WIDTH/2,FRAME_HEIGHT/2),10,c,2);

	}
	void morphOps(Mat thresh){

		//create structuring element that will be used to "dilate" and "erode" image.
		//the element chosen here is a 3px by 3px rectangle

		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new Size(3,3));
		//dilate with larger element so make sure object is nicely visible
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new Size(8,8));

		Imgproc.erode(thresh,thresh,erodeElement);
		Imgproc.erode(thresh,thresh,erodeElement);
		//Imgproc.erode(thresh,thresh,erodeElement);

		Imgproc.dilate(thresh,thresh,dilateElement);
		Imgproc.dilate(thresh,thresh,dilateElement);		
		//Imgproc.dilate(thresh,thresh,dilateElement);

		Imgproc.dilate(thresh,thresh,dilateElement);
		Imgproc.dilate(thresh,thresh,dilateElement);

		Imgproc.erode(thresh,thresh,erodeElement);
		Imgproc.erode(thresh,thresh,erodeElement);
		//Imgproc.dilate(thresh,thresh,dilateElement);
		//Imgproc.erode(thresh,thresh,erodeElement);


	}
	void trackFilteredObject(int x, int y, Mat threshold, Mat cameraFeed,Scalar c){

		Mat temp=new Mat();
		threshold.copyTo(temp);
		//these two vectors needed for output of findContours
		List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
		MatOfInt4 hierarchy=new MatOfInt4();
		//find contours of filtered image using openCV findContours function
		Imgproc.findContours(temp,contours,hierarchy,Imgproc.RETR_CCOMP,Imgproc.CHAIN_APPROX_SIMPLE );
		//use moments method to find our filtered object
		double refArea = 0;
		boolean objectFound = false;
		if (hierarchy.size().area() > 0) {
			int numObjects = (int) hierarchy.size().area();
			//if number of objects greater than MAX_NUM_OBJECTS we have a noisy filter
			if(numObjects<MAX_NUM_OBJECTS){
				for (int index = 0; index >= 0; index = (int) hierarchy.get(0, index)[0]) {

					Moments moment = Imgproc.moments(contours.get(index));
					double area = moment.m00;

					//if the area is less than 20 px by 20px then it is probably just noise
					//if the area is the same as the 3/2 of the image size, probably just a bad filter
					//we only want the object with the largest area so we safe a reference area each
					//iteration and compare it to the area in the next iteration.
					double epsilon=0.2;
					//a=pr^2
					//r=sqrt(a/p)
					//c=2pr
					//c=2psqrt(a/p)
					double circ=2*Math.PI*Math.sqrt(area/Math.PI);

					MatOfPoint2f contour = new MatOfPoint2f();
					contours.get(index).convertTo(contour, CvType.CV_32F);
					if(area>MIN_OBJECT_AREA && area<MAX_OBJECT_AREA){
						if ((moment.mu20-moment.mu02<=(moment.mu20+moment.mu02)/2*epsilon)&&
								(Imgproc.arcLength(contour,true)-circ)<=(Imgproc.arcLength(contour,true)+circ)/2*epsilon
								){
							x = (int) (moment.m10/area);
							y = (int) (moment.m01/area);
							objectFound = true;
							//refArea = area;
						}else{
							objectFound = false;
							Imgproc.putText(cameraFeed,"non circular object found",new Point(0,50),1,2,new Scalar(0,0,255),2);
							System.out.println("("+moment.mu20+" = "+moment.mu02+"),("+Imgproc.arcLength(contour,true)+" = "+circ+")");
						}
					}else{
						System.out.println("object of bad size found");
					}

					if(objectFound ==true){

						Imgproc.putText(cameraFeed,"Tracking Object",new Point(0,50),2,1,c,2);
						System.out.println("object found");
						//draw object location on screen
						drawObject(x,y,cameraFeed,c);
					}else{

					}
				}
			}else{
				Imgproc.putText(cameraFeed,"TOO MUCH NOISE! ADJUST FILTER",new Point(0,50),1,2,new Scalar(0,0,255),2);
				System.out.println("too much noise");
			}
		}else{
			Imgproc.putText(cameraFeed,"no objects found",new Point(0,50),1,2,new Scalar(0,0,255),2);
			System.out.println("no object found");
		}
	}
	public Hello(){
		sliders();
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		//some boolean variables for different functionality within this
		//program
		boolean trackObjects = true;
		boolean useMorphOps = true;
		//Matrix to store each frame of the webcam feed
		Mat cameraFeed=new Mat();
		//matrix storage for HSV image
		Mat HSV=new Mat();
		//matrix storage for binary threshold image
		Mat threshold=new Mat();
		Mat threshold1=new Mat();
		Mat thresholdPre=null;
		//x and y values for the location of the object
		int x=0, y=0;
		//create slider bars for HSV filtering

		//video capture object to acquire webcam feed
		VideoCapture capture=new VideoCapture();
		//open capture object at location zero (default location for webcam)
		capture.open(0);
		//set height and width of capture frame
		capture.set(Videoio.CAP_PROP_FRAME_WIDTH,FRAME_WIDTH);
		capture.set(Videoio.CAP_PROP_FRAME_HEIGHT,FRAME_HEIGHT);
		//start an infinite loop where webcam feed is copied to cameraFeed matrix
		//all of our operations will be performed within this loop
		while(true){
			//store image to matrix
			capture.read(cameraFeed);
			//convert frame from BGR to HSV colorspace
			Imgproc.cvtColor(cameraFeed,HSV,Imgproc.COLOR_BGR2HSV);
			//filter HSV image between values and store filtered image to
			//threshold matrix
			Core.inRange(HSV,new Scalar(H_MIN,S_MIN,V_MIN),new Scalar(H_MAX,S_MAX,V_MAX),threshold);
			Core.inRange(HSV,new Scalar(H_MIN1,S_MIN1,V_MIN1),new Scalar(H_MAX1,S_MAX1,V_MAX1),threshold1);
			//perform morphological operations on thresholded image to eliminate noise
			//and emphasize the filtered object(s)
			if(useMorphOps){
				morphOps(threshold);
				morphOps(threshold1);
			}
			if (thresholdPre==null)thresholdPre=threshold;
			//filterChange(threshold,thresholdPre);
			//pass in thresholded frame to our object tracking function
			//this function will return the x and y coordinates of the
			//filtered object
			if(trackObjects){
				trackFilteredObject(x,y,threshold,cameraFeed,new Scalar(0,255,0));
				thresholdPre=threshold;
				//trackFilteredObject(x,y,threshold1,cameraFeed,new Scalar(255,0,0));
			}
			//show frames 
			//display(lbl2,m2i(threshold));
			display(lbl2,m2i(cameraFeed));
			//display(windowName1,m2i(HSV));
			//System.out.println("H->("+H_MIN+","+H_MAX+")");
			//System.out.println("S->("+S_MIN+","+S_MAX+")");
			//System.out.println("V->("+V_MIN+","+V_MAX+")");

		}
	}
	public static void main(String[] args)
	{
		new Hello();
	}
	public BufferedImage m2i(Mat m){
		// source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
		// Fastest code
		// The output can be assigned either to a BufferedImage or to an Image

		int type = BufferedImage.TYPE_BYTE_GRAY;
		if ( m.channels() > 1 ) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels()*m.cols()*m.rows();
		byte [] b = new byte[bufferSize];
		m.get(0,0,b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);  
		return image;

	}
	public void display(JLabel lbl,Image img2)
	{   
		//BufferedImage img=ImageIO.read(new File("/HelloOpenCV/lena.png"));
		if(frame==null){
			ImageIcon icon=new ImageIcon(img2);
			frame=new JFrame();
			frame.setLayout(new FlowLayout());        
			frame.setSize(img2.getWidth(null)+50, img2.getHeight(null)+50);  		
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}else{
			boolean flag=false;
			for (int i=0;i<frame.getComponentCount();i++){
				if (frame.getComponents()[i].equals(lbl))flag=true;
			}
			if(!flag){				
				if(lbl.equals(lbl2))frame.add(lbl);
				frame.setSize(lbl.getWidth()+25,lbl.getHeight()+25);
			}
			ImageIcon icon=new ImageIcon(img2);
			lbl.setIcon(icon);
		}
		frame.repaint();
		/*
		 try {		 
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//*/


	}
	public void sliders(){
		JFrame slide=new JFrame("colour");
		slide.setLayout(new FlowLayout());
		slide.setVisible(true);
		slide.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		slide.setLocation(0,FRAME_HEIGHT+50);			
		JLabel hminL=new JLabel(H_MIN+"");
		JSlider hmin=new JSlider(0,255,H_MIN);
		hmin.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {				
				H_MIN=hmin.getValue();
				hminL.setText(H_MIN+"");
			}		
		});
		slide.add(hminL);
		slide.add(hmin);
		JLabel hmaxL=new JLabel(H_MAX+"");
		JSlider hmax=new JSlider(0,255,H_MAX);
		hmax.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {				
				H_MAX=hmax.getValue();
				hmaxL.setText(H_MAX+"");
			}		
		});
		slide.add(hmax);
		slide.add(hmaxL);		
		JLabel sminL=new JLabel(S_MIN+"");
		JSlider smin=new JSlider(0,255,S_MIN);
		smin.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {				
				S_MIN=smin.getValue();
				sminL.setText(S_MIN+"");
			}		
		});
		slide.add(sminL);
		slide.add(smin);
		JLabel smaxL=new JLabel(S_MAX+"");
		JSlider smax=new JSlider(0,255,S_MAX);
		smax.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {				
				S_MAX=smax.getValue();
				smaxL.setText(S_MAX+"");
			}		
		});
		slide.add(smax);
		slide.add(smaxL);

		JLabel vminL=new JLabel(V_MIN+"");
		JSlider vmin=new JSlider(0,255,V_MIN);
		vmin.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {				
				V_MIN=vmin.getValue();
				vminL.setText(V_MIN+"");
			}		
		});
		slide.add(vminL);
		slide.add(vmin);
		JLabel vmaxL=new JLabel(V_MAX+"");
		JSlider vmax=new JSlider(0,255,V_MAX);
		vmax.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {				
				V_MAX=vmax.getValue();
				vmaxL.setText(V_MAX+"");
			}		
		});
		slide.add(vmax);
		slide.add(vmaxL);		
		slide.pack();
		slide.setSize(new Dimension(vmaxL.getWidth()*2+vmax.getWidth()*2+20,vmaxL.getHeight()*9));
		slide.repaint();
	}
}