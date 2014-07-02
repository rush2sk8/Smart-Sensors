import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.swing.Timer;

import org.jsoup.Jsoup;

/**
 * @author Rushad Antia
 */

public class NCAP{

	//this is the currentIP that the NCAP is hosting
	private String currentIP;

	//this is the flag to stop the scrolling text. must be volatile because 2 threads have to access it.
	private static volatile boolean flag;

	/**
	 * Creates an object (instance) of the NCAP
	 * @param ip
	 */
	public NCAP(String ip){
		flag = true;
		currentIP = ip;
	}

	//TODO fix the sockettimeout
	/**
	 * Gets all the connected WTIMS to the NCAP
	 * @param from - starting range
	 * @param to - ending range 
	 * @return All the found WTIMS that the NCAP has discovered
	 * @throws SocketTimeoutException
	 */
	public String discoverWTIMs(int from , int to) throws SocketTimeoutException{
		return discoverWTIMs(from, to, false);
	}

	/**
	 * Gets all the connected WTIMS to the NCAP
	 * @param from - starting range 
	 * @param to - ending range
	 * @param continuous - true if you want to keep checking 
	 * @return A list of all the found tims
	 */
	public String discoverWTIMs(int from , int to, boolean continuous){

		//makes sure that the indicies passed in are valid
		if(from<2||to>254||to<from)
			throw new IllegalArgumentException("Invalid search indicies");

		do {
			try {
				String found = scrapePage(currentIP+"/1451/Discovery/TIMDiscovery.htm?wtimIdl="+from+"&wtimIdh="+to+"&reptim=0&timtype=1&format=0");
				String toReturn = found.substring(found.indexOf("Error code"),found.indexOf("WTIM Number")) + "\n";
				toReturn += found.substring(found.indexOf("WTIM Number"),found.indexOf("WTIM Ids List ")) + "\n";
				toReturn += found.substring(found.indexOf("WTIM Ids List "),found.indexOf("   © 2012 Esensors"));
				return toReturn;

			} catch (IOException e) {e.printStackTrace();}

		//this continues the process of finding something
		}while(continuous);

		return null;

	}

	/**
	 * Retrieves the information about a TIM
	 * @param timId - The ID of the TIM
	 * @param timeOut - The Timeout in seconds
	 * @param timType - 1 for Wi-Fi 0 for RS232
	 * @return Returns the Information about the requested TIM
	 */
	public String getTIMInfo(int timId,int timeOut , int timType){

		String info = null;
		try {
			info = scrapePage(currentIP+ "/1451/Discovery/TransducerDiscovery.htm?timId="+timId+"&timeout="+timeOut+"&timtype="+timType+"&format=0");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		info = info.substring(info.indexOf("TIM Id "),info.indexOf("   ©"));
		String toReturn = info.substring(info.indexOf("TIM Id"),info.indexOf("Transducer Channel ")) + "\n";

		toReturn += info.substring(info.indexOf("Transducer Channel Ids"), info.indexOf("Transducer Names")) + "\n";
		toReturn += info.substring(info.indexOf("Transducer Names"));
		return toReturn;
	}

	/**
	 * Writes a message out to the screen 
	 * @param wtimId - The WTIM id Number 
	 * @param arg - The text to send to the screen
	 * @param timType - 1 is for Wi-Fi 0 is for the RS232 connection
	 * @throws IOException
	 */
	public void writeToScreen(int wtimId, String arg,int timType) throws IOException {

		Jsoup.connect(currentIP+"/1451/TransducerAccess/WriteData.htm?timId="+wtimId+"&channelId=9&timeout=10&samplingMode=7&timtype="+timType+"&format=0&transducerData="+arg).execute();

	}

	/**
	 * Returns the converted sensor data
	 * @param wtimID - The WTIM ID
	 * @param channelID - The Channel ID
	 * @param timeOut - Timeout
	 * @return sensor data
	 * @throws IOException
	 */
	public String getSensorData(int wtimID , int channelID , int timeOut)throws IOException {
		String response = Jsoup.connect(currentIP+"/1451/TransducerAccess/ReadData.htm?timId="+wtimID+"&channelId="+channelID+"&timeout="+timeOut+"&samplingMode=7&timtype=1&format=0").get().body().text();
		response =  response.substring(response.indexOf("Time(nanosecs) "), response.indexOf("© 2012 Esensors"));
		response = response.substring(response.indexOf("Transducer Data"));

		return "Transducer " + wtimID + " Data: " + response.substring(response.indexOf("Data"));
	}

	/**
	 * Returns only the data no text just data
	 * @param wtimID - the WTIM id
	 * @param channelID - the channel desired to access
	 * @param timeout - the timeout
	 * @return the converted sensor reading
	 * @throws IOException
	 */
	public String getSensorDataRaw(int wtimID , int channelID , int timeout) throws IOException {
		String data  = getSensorData(wtimID, channelID, timeout);
		return data.substring(data.indexOf("Data")+11);
	}

	/**
	 * Retrieves sensor data of a certain sensor at an interval 
	 * @param wtimID - The WTIM id
	 * @param channelID - The Channel ID
	 * @param interval - The interval to wait (in seconds)
	 * @param numSamples - The number of times to retrieve data at X intervals
	 * @throws IOException 
	 * @throws InterruptedException
	 */
	@SuppressWarnings("static-access")
	public void displaySensorDataAtInterval(int wtimID , int channelID , int interval, int numSamples) throws IOException, InterruptedException {

		for(int i=0;i<numSamples;i++) {
			System.out.println(getSensorData(107, 3, 1));
			Thread.currentThread().sleep((long)interval*1000);
		}

	}

	/** 
	 * NOTICE: Invokes a thread to do the text take heed.
	 * Starts a scrolling text on the screen of a WTIM.
	 * @param wtimID - the id of the WTIM
	 * @param textToDisplay - the text to display
	 */
	public void startScrollingText(final int wtimID ,final String textToDisplay) {
		flag = true;

		new Thread(new Runnable() {

			@SuppressWarnings("static-access")
			@Override
			public void run() {

				String currentText = textToDisplay;

				while(flag) {

					String oldText = currentText;
					String newText = oldText.substring(1)+oldText.substring(0,1);
					try {
						writeToScreen(107, newText, 1);
					} catch (IOException e) {

						e.printStackTrace();
					}
					currentText = newText;	
					try {
						Thread.currentThread().sleep(100);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				}

			}
		}).start();

	}

	/**
	 * Stops the text from scrolling. 
	 * If no text is scrolling on the screen this method will have no effect
	 */
	public void stopScrollingText() {
		flag = false;
	}
	
	/**
	 * Helper method that scrapes given url
	 * @param u - url
	 * @return - data from the scrape
	 * @throws IOException
	 */
	private String scrapePage(String u) throws IOException{
		return Jsoup.connect(u).get().body().text();
	}

}