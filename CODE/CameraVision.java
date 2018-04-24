import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;


public class CameraVision {
	int HEIGHT=500;
	int WIDTH=500;

	int minR=15;
	int maxR=30;
	int H_MIN = 31;
	int H_MAX = 107;
	int S_MIN = 0;
	int S_MAX = 153;
	int V_MIN = 43;
	int V_MAX = 255;
	int BLUR=3;
	int CANNY=82;
	int ERODE=5;
	int DILATE=8;
	int THRES=17;	
	int tol=50;
	Mat cameraFeed;	
	Mat HSV;
	Mat threshold;	
	RPiCamera piCamera =null;
	BufferedImage image =null;
	public CameraVision(){
		initOpenCv();
		cameraFeed=new Mat();	
		HSV=new Mat();
		threshold=new Mat();
		try {
			piCamera = new RPiCamera("").setWidth(WIDTH).setHeight(HEIGHT).turnOffPreview().setTimeout(100);
		} catch (FailedToRunRaspistillException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public  int[] getLocation(){		
		System.out.println("taking picture");
		try {
			image = piCamera.takeBufferedStill();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("picture taken");
		cameraFeed=b2m(image);				
		Imgproc.cvtColor(cameraFeed,HSV,Imgproc.COLOR_BGR2GRAY);		
		Imgproc.Canny(HSV,threshold,33 ,100);	
		morphOps2(threshold);
		System.out.println("tracking stuff");
		int [][] locations =trackFilteredObject2(HSV,cameraFeed);
		System.out.println("mapping stuff");
		if (locations.length>0)return map(locations);
		return new int[0];
	}
	
	private int[] map(int[][] loc){		
		int [][]newLoc=new int[loc.length][2];
		for(int i=0;i<loc.length;i++){
			newLoc[i][0]=loc[i][0]-WIDTH;
			newLoc[i][1]=loc[i][1];
		}
		
		// Find the closest ball
		double d=mag(newLoc[0]);
		int di=0;
		for(int i=1;i<loc.length;i++){
			if(d>mag(newLoc[i])){
				d=mag(newLoc[i]);
				di=i;
			}
		}
		
		int[] val=new int[2];
		val[0]=(int)d;  //Distance to ball
		
		// newLoc[][0] is x, newLoc[][1] is y
		val[1]=(int) (Math.atan(newLoc[di][0]/newLoc[di][1])*180/Math.PI);
		
		return val;		
	}
	
	private double mag(int[]x){
		return (Math.sqrt(Math.pow(x[0], 2)+Math.pow(x[1], 2)));
	}
	
	int[][] trackFilteredObject2(Mat threshold,Mat cam){
		/// Reduce the noise so we avoid false circle detection
		//Imgproc.cvtColor(cameraFeed,threshold,Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur( threshold,threshold, new Size(BLUR, BLUR), 2, 2 );
		Mat circles = new Mat();

		/// Apply the Hough Transform to find the circles
		Imgproc.HoughCircles( threshold,circles, Imgproc.HOUGH_GRADIENT, 1, maxR, CANNY, THRES, minR,minR);
		//  System.out.println(circles.cols());
		ArrayList<int[]> circ=new  ArrayList<int[]>();
		for (int i = 0; i < circles.cols(); i++)
		{
			Mat tempM=new Mat();

			Core.inRange(cam,new Scalar(H_MIN,S_MIN,V_MIN),new Scalar(H_MAX,S_MAX,V_MAX),tempM);
			double vCircle[] = circles.get(0,i);
			tempM.convertTo(tempM, CvType.CV_64FC3); 

			//int size = (int) (tempM.total() * tempM.channels());
			//double[] tempD = new double[size]; 
			//tempM.get(0, 0, tempD);
			int x=(int)vCircle[0];
			int y=(int)vCircle[1];
			int r=(int)vCircle[2];
			int count=0;
			//for (int j = y-r; j < y+r; j++) {
				//for (int k = x; Math.pow(k-x,2) + Math.pow((j-y),2) <= Math.pow(r,2); k--) {
					//if(k<tempM.rows()&&j<tempM.cols())count+=(int)tempM.get(k,j)[0];
				//}
				//for (int k = x+1; Math.pow(k-x,2) + Math.pow((j-y),2) <= Math.pow(r,2); k++) {
					//if(k<tempM.rows()&&j<tempM.cols())count+=(int)tempM.get(k,j)[0];
				//}
			//}
			int ColourThreshold=1;
			if (count<Math.PI*r*r*ColourThreshold||true){
				int[] tempI=new int[3];
				tempI[0]=(int)vCircle[0];
				tempI[1]=(int)vCircle[1];
				tempI[2]=(int)vCircle[2];
				circ.add(tempI);
			}
		}
		int[][]locations=new int[circ.size()][3];
		for(int i=0;i<locations.length;i++){
			locations[i]=circ.get(i);
			Imgproc.circle(cam,new Point(circ.get(i)[0],circ.get(i)[1]),circ.get(i)[2],new Scalar(0,255,0),2);
		}
		return locations;
	}
	
	private void morphOps2(Mat thresh){
		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(ERODE,ERODE));		
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(DILATE,DILATE));
		Imgproc.dilate(thresh,thresh,dilateElement);
		Imgproc.erode(thresh,thresh,erodeElement);
	}
	
	private BufferedImage m2i(Mat m){
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

	
	private static void initOpenCv() {
		setLibraryPath();
		System.out.println(System.getProperty("java.library.path"));
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("OpenCV loaded. Version: " + Core.VERSION);
	}
	private static Mat b2m(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		return mat;
	}
	private static void setLibraryPath() {
		try {
			File file=new File("lib");
			String [] files=file.list();
			for(int i=0;i<files.length;i++){
				System.out.println(file.list()[i]);
			}
			System.setProperty("java.library.path",file.getAbsolutePath());
			System.setProperty("java.class.path",file.getAbsolutePath());
			Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}

	}
}

