package opencv;

import java.awt.Dimension;
import java.awt.FlowLayout;
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
	
	/*yellow
	int H_MIN = 60;
	int H_MAX = 70;
	int S_MIN = 90;
	int S_MAX = 100;
	int V_MIN = 90;
	int V_MAX = 100;
	//*/
	///*blue
	int H_MIN = 88;
	int H_MAX = 126;
	int S_MIN = 192;
	int S_MAX = 255;
	int V_MIN = 68;
	int V_MAX = 255;
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
	JLabel lbl;
	//default capture width and height
	int FRAME_WIDTH = 640;
	int FRAME_HEIGHT = 480;
	//max number of objects to be detected in frame
	int MAX_NUM_OBJECTS=50;
	//minimum and maximum object area
	int MIN_OBJECT_AREA = 20*20;
	int MAX_OBJECT_AREA = (int) (FRAME_HEIGHT*FRAME_WIDTH/1.5);
	//names that will appear at the top of each window
	String windowName = "Original Image";
	String windowName1 = "HSV Image";
	String windowName2 = "Thresholded Image";
	String windowName3 = "After Morphological Operations";
	String trackbarWindowName = "Trackbars";

	private String intToString(int number){
		return number+"";
	}

	void drawObject(int x, int y,Mat frame){

		//use some of the openCV drawing functions to draw cross hairs
		//on your tracked image!

		//UPDATE:JUNE 18TH, 2013
		//added 'if' and 'else' statements to prevent
		//memory errors from writing off the screen (ie. (-25,-25) is not within the window!)

		Imgproc.circle(frame,new Point(x,y),20,new Scalar(0,255,0),2);
		if(y-25>0)
			Imgproc.line(frame,new Point(x,y),new Point(x,y-25),new Scalar(0,255,0),2);
		else Imgproc.line(frame,new Point(x,y),new Point(x,0),new Scalar(0,255,0),2);
		if(y+25<FRAME_HEIGHT)
			Imgproc.line(frame,new Point(x,y),new Point(x,y+25),new Scalar(0,255,0),2);
		else Imgproc.line(frame,new Point(x,y),new Point(x,FRAME_HEIGHT),new Scalar(0,255,0),2);
		if(x-25>0)
			Imgproc.line(frame,new Point(x,y),new Point(x-25,y),new Scalar(0,255,0),2);
		else Imgproc.line(frame,new Point(x,y),new Point(0,y),new Scalar(0,255,0),2);
		if(x+25<FRAME_WIDTH)
			Imgproc.line(frame,new Point(x,y),new Point(x+25,y),new Scalar(0,255,0),2);
		else Imgproc.line(frame,new Point(x,y),new Point(FRAME_WIDTH,y),new Scalar(0,255,0),2);

		Imgproc.putText(frame,intToString(x)+","+intToString(y),new Point(x,y+30),1,1,new Scalar(0,255,0),2);
		 
	}
	void morphOps(Mat thresh){

		//create structuring element that will be used to "dilate" and "erode" image.
		//the element chosen here is a 3px by 3px rectangle

		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));
		//dilate with larger element so make sure object is nicely visible
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(8,8));

		Imgproc.erode(thresh,thresh,erodeElement);
		Imgproc.erode(thresh,thresh,erodeElement);


		Imgproc.dilate(thresh,thresh,dilateElement);
		Imgproc.dilate(thresh,thresh,dilateElement);



	}
	void trackFilteredObject(int x, int y, Mat threshold, Mat cameraFeed){

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
					if(area>MIN_OBJECT_AREA && area<MAX_OBJECT_AREA && area>refArea){
						x = (int) (moment.m10/area);
						y = (int) (moment.m01/area);
						objectFound = true;
						refArea = area;
					}else objectFound = false;


				}
				//let user know you found an object
				if(objectFound ==true){
					Imgproc.putText(cameraFeed,"Tracking Object",new Point(0,50),2,1,new Scalar(0,255,0),2);
					//draw object location on screen
					drawObject(x,y,cameraFeed);}

			}else Imgproc.putText(cameraFeed,"TOO MUCH NOISE! ADJUST FILTER",new Point(0,50),1,2,new Scalar(0,0,255),2);
		}else{
			Imgproc.putText(cameraFeed,"no objects found",new Point(0,50),1,2,new Scalar(0,0,255),2);
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
			//perform morphological operations on thresholded image to eliminate noise
			//and emphasize the filtered object(s)
			if(useMorphOps)
				morphOps(threshold);
			//pass in thresholded frame to our object tracking function
			//this function will return the x and y coordinates of the
			//filtered object
			if(trackObjects)
				trackFilteredObject(x,y,threshold,cameraFeed);

			//show frames 
			//display(windowName2,m2i(threshold));
			display(windowName,m2i(cameraFeed));
			//display(windowName1,m2i(HSV));
			System.out.println("H->("+H_MIN+","+H_MAX+")");
			System.out.println("S->("+S_MIN+","+S_MAX+")");
			System.out.println("V->("+V_MIN+","+V_MAX+")");
			
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
	public void display(String name,Image img2)
	{   
		//BufferedImage img=ImageIO.read(new File("/HelloOpenCV/lena.png"));
		if(frame==null){
		ImageIcon icon=new ImageIcon(img2);
		frame=new JFrame(name);
		frame.setLayout(new FlowLayout());        
		frame.setSize(img2.getWidth(null)+50, img2.getHeight(null)+50);     
		lbl=new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}else{
			ImageIcon icon=new ImageIcon(img2);
			lbl.setIcon(icon);
		}
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
		slide.setLocation(600, 600);
		slide.setMinimumSize(new Dimension(405,150));
		JSlider hmin=new JSlider(0,255,H_MIN);
		hmin.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {				
				H_MIN=hmin.getValue();
			}		
		});
		slide.add(hmin);
		JSlider hmax=new JSlider(0,255,H_MAX);
		hmax.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {				
				H_MAX=hmax.getValue();
			}		
		});
		slide.add(hmax);
		JSlider smin=new JSlider(0,255,S_MIN);
		smin.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {				
				S_MIN=smin.getValue();
			}		
		});
		slide.add(smin);
		JSlider smax=new JSlider(0,255,S_MAX);
		smax.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {				
				S_MAX=smax.getValue();
			}		
		});
		slide.add(smax);
		JSlider vmin=new JSlider(0,255,V_MIN);
		vmin.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {				
				V_MIN=vmin.getValue();
			}		
		});
		slide.add(vmin);
		JSlider vmax=new JSlider(0,255,V_MAX);
		vmax.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {				
				V_MAX=vmax.getValue();
			}		
		});
		slide.add(vmax);
		System.out.println("slider created");
	}
}