import java.io.IOException;
import java.net.SocketTimeoutException;

import org.jsoup.Jsoup;

/**
 * This class allows a user to run various rigorous tests on the NCAP and WTIMs 
 * NOTE: THIS CLASS RUNS ONLY AT THE DEFAULT IP ADDRESS FOR ADDITIONAL INFO CONTACT ME 
 *@author Rushad Antia
 *@version 1.0
 *@since 7-02-14 
 */
public class RegTest {


	/**
	 * This method runs 2 threads X number of times.Initially it gets the TIM info for the specified TIM then One thread gets the tim status
	 * the other thread discovers WTIMs 107 to 111. After that it displays the time taken to do that single operation. And finally it prints out the total 
	 * average time taken to complete the full operations.
	 *@param times - how many times you want to run the operations
	 *@param timId - the id of the TIM
	 *@param timeOut - the timeout
	 *@param timType - 1 is wireless 0 is RS232 serial connection
	 *@throws InterruptedException
	 */
	public static void RunSimultaneousOperationsTest(int times,int timId, int timeOut , int timType) throws InterruptedException {

		//creates and NCAP
		final NCAP handler = new NCAP("http://192.168.254.102",10);

		//stores all the times for each trial 
		double[] nums = new double[times];

		for(int i=0;i<times;i++) {
			long start =  System.currentTimeMillis();

			//gets the tim info for the specified tim 
			//handler.getTIMInfo(timId, timeOut);

			//creates a new thread that safely tries to get the sample status of the tim  RUNS ONCE
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

			//creates another thread that discovers WTIMS from 107 to 111
			Thread x = new Thread(new Runnable() {

				@Override
				public void run() {

					handler.discoverWTIMs(107, 111);

				}
			});

			//starts both the threads 
			t.start();
			x.start();

			//safely joins both the running threads back to the master thread
			x.join();
			t.join();

			//stores the times in the array 
			nums[i] = (System.currentTimeMillis()-start)/1000.0 ;

			//prints out the time taken
			System.out.println(nums[i]+ " seconds");

		}

		//creates a double to calculate the total average 
		double avg = 0;

		//calculates the average time taken to complete X number of operations
		for(int i=0;i<nums.length;i++)  
			avg+=nums[i];

		//prints out the total time to do all of those operations
		System.out.println("Total time took to do " + nums.length + " \"concurrent\" operations took: " + avg + " seconds");

		//prints out the average time to complete all of operations
		System.out.println("Average Time took out of "+ nums.length + " times is: "+ avg/nums.length);
	}

	/**
	 * This method gets multiple pieces of data from a specified WTIM at a specified channel
	 * <p>
	 *and prints them out a specified number of times.
	 *@param times - the number of times to retrieve data from the specified WTIM 
	 *@param wtimID - the WTIM ID
	 *@param channelID - the channelID or the channel which the data resides in
	 *@param timeOut - the time out
	 *@throws IOException
	 *@throws SocketTimeoutException
	 */
	public static void GetMultiplePiecesOfData(int times, int wtimID , int channelID , int timeOut) throws IOException,SocketTimeoutException {

		//creates a NCAP at the default IP address 
		final NCAP handler = new  NCAP("http://192.168.254.102",10);

		//does the other stuff
		for(int i=0;i<times;i++) {
			System.out.println(handler.getSensorData(wtimID, channelID, timeOut));
		}
	}

}
