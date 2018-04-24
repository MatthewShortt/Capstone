import java.util.Scanner;

public class PathPlanning {

	public static void main(String[] args) {
		//BumpHandler bump=new BumpHandler();
		//ProximityHandler prox=new ProximityHandler();
		CameraVision cam=new CameraVision();
		WheelMotorControl motor=new WheelMotorControl();
		System.out.println("motor on");
		//InfraHandler inf=new InfraHandler();
		motor.start();
		Scanner s=new Scanner(System.in);		
		boolean flag=true;
		boolean coll=false;
		motor.endEffectorStart();

		while(flag){
			System.out.println("getting locations");
			int[]p=cam.getLocation();
			System.out.println("locations got");
			if(p.length==0){
				System.out.println("no balls");
				continue;
			}
			if(Math.abs(p[1])<10){
				System.out.println("driving at ball");
				if(p[0]<100){
					System.out.println("slow");
					motor.changeDriveState(1);
					coll=true;
				}else{
					System.out.println("fast");
					motor.changeDriveState(2);
				}
			}else if(p[1]>0){
				System.out.println("turning right");
				motor.changeDriveState(5);
			}else{
				System.out.println("turning left");
				motor.changeDriveState(6);
			}
			if(coll){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//			if(prox.getDistance()<30||inf.getDistance()<30){
			//				System.out.println("obstacle");
			//				motor.changeDriveState(0);
			//			}
			//			if(bump.getBump()){
			//				System.out.println("crashed");
			//				motor.endEffectorEStop();
			//				motor.changeDriveState(0);
			//			}
			System.out.println("print s to quit");
			if(s.next().equals("s"))flag=false;
		}


		motor.endEffectorStop();
		//		bump.destruct();
		//		prox.destruct();
		motor.destruct();
		motor.stop();
		//		inf.destruct();
		System.out.println("EXITING");
	}

}
