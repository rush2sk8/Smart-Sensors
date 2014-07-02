import java.io.IOException;
import java.net.SocketTimeoutException;

import org.jsoup.Jsoup;

/**
 * @author Rushad Antia
 */

public class NCAP{

	private String currentIP;

	public NCAP(String ip){
		currentIP = ip;
	}

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
	 * Retrieves sensor data of a certain sensor at an interval 
	 * @param wtimID - The WTIM id
	 * @param channelID - The Channel ID
	 * @param interval - The interval to wait (in seconds)
	 * @param numSamples - The number of times to retrieve data at X intervals
	 * @throws IOException 
	 * @throws InterruptedException
	 */
	public void displaySensorDataAtInterval(int wtimID , int channelID , int interval, int numSamples) throws IOException, InterruptedException {

		for(int i=0;i<numSamples;i++) {
			System.out.println(getSensorData(107, 3, 1));
			Thread.currentThread().sleep((long)interval*1000);
		}

	}

	private String scrapePage(String u) throws IOException{
		return Jsoup.connect(u).get().body().text();
	}

}