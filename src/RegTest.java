import java.io.IOException;
import java.net.SocketTimeoutException;

import org.jsoup.Jsoup;


public class RegTest {

	
	public static void RunSimultaneousOperationsTest(int times,int timId, int timeOut , int timType) throws InterruptedException {
	
		final NCAP handler = new NCAP("http://192.168.254.102");
		
		double[] nums = new double[times];
		for(int i=0;i<times;i++) {
			long start =  System.currentTimeMillis();

			handler.getTIMInfo(timId, timeOut, timType);

			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {

					try {
						Jsoup.connect("http://192.168.254.102/1451/RdTimSamplesStatus.htm").get().body().text();
					} catch (IOException e) {

						e.printStackTrace();
					}	
				}
			});
			Thread x = new Thread(new Runnable() {

				@Override
				public void run() {

					try {
						handler.discoverWTIMs(107, 111);
					} catch (SocketTimeoutException e) {
					
						e.printStackTrace();
					}

				}
			});
			
			t.start();
			x.start();
			
			x.join();
			t.join();
			
			nums[i] = (System.currentTimeMillis()-start)/1000.0 ;
			System.out.println(nums[i]+ " seconds");
		
		}
		double avg = 0;
		
		for(int i=0;i<nums.length;i++) {
			avg+=nums[i];
		}
		System.out.println("Total time took to do " + nums.length + " \"concurrent\" operations took: " + avg + " seconds");
		System.out.println("Average Time took out of "+ nums.length + " times is: "+ avg/nums.length);
	}

	
	public static void GetMultiplePiecesOfData(int times, int wtimID , int channelID , int timeOut) throws IOException,SocketTimeoutException {
		
		
		final NCAP handler = new  NCAP("http://192.168.254.102");
		
		for(int i=0;i<times;i++) {
			System.out.println(handler.getSensorData(wtimID, channelID, timeOut));
		}
	}

}
