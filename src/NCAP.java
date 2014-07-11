import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.jsoup.Jsoup;

/**
 * This class gives a user the ability to control WTIMs through an NCAP by HTTP access. ONLY FOR WIFI NCAP.
 * Tends to to throw <code>SocketTimeoutException</code> when connection is not well
 * @author Rushad Antia
 */

public class NCAP{

	//this is the currentIP that the NCAP is hosting
	private String currentIP;

	
	private int timeout;
	/** 
	 * Creates an object (instance) of the NCAP
	 * @param ip
	 */
	public NCAP(String ip,int time){
		currentIP = ip;
		timeout = time;
	}

	/**
	 * Get IP
	 * @return - Returns current IP
	 */
	public String getIP() {
		return currentIP;
	}
	
	
	/**
	 * Gets all the connected WTIMS to the NCAP. CAN TAKE UP TO 90 SECONDS TO COMPLETE
	 * @param from - starting range 
	 * @param to - ending range
	 * @param continuous - true if you want to keep checking 
	 * @return A list of all the found tims
	 */
	public ArrayList<String> discoverWTIMs(int from , int to){

		//makes sure that the indices passed in are valid
		if(from<2||to>254||to<from)
			throw new IllegalArgumentException("Invalid search indicies");

		ArrayList<String> foundTIMS = new ArrayList<String>(10);

		for(int i=from;i<=to;i++) {
			String data = getTIMInfo(i, 10, 1);
			String subbed = null;

			try {	 
				subbed = data.substring(data.indexOf("Transducer Names"));
			}catch(Exception e) {e.printStackTrace();System.out.println(subbed);}

			if(subbed.length()>16&&subbed!=null) 
				foundTIMS.add(data.substring(data.indexOf("TIM Id")+6,data.indexOf("Transducer Channel")).trim());	
		}
		return foundTIMS;
	}

	/**
	 * Allows you to retrieve data such as channels and names of a specified transducer.
	 * Retrieves the information about a TIM
	 * @param timId - The ID of the TIM
	 * @param timeOut - The Timeout in seconds
	 * @return Returns the Information about the requested TIM
	 */
	public String getTIMInfo(int timId,int timeOut,int timType){

		String info = null;
		try {
			info = scrapePage(currentIP+ "/1451/Discovery/TransducerDiscovery.htm?timId="+timId+"&timeout="+timeOut+"&timtype="+timType+"&format=0");
		} catch (IOException e) {
			//makes sure that we return because otherwise we would substring stuff that doesnt exist throwing an null pointer. also protects against sockettimiing out 
			e.printStackTrace();
			return "";
		}

		//allows easy to read format of the recieved data
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
	public void writeToScreen(int wtimId, String arg,int timType) throws IOException{

		//uses the JSoup library to excecute a command to a certain ip using http connection to the NCAP
		Jsoup.connect(currentIP+"/1451/TransducerAccess/WriteData.htm?timId="+wtimId+"&channelId=9&timeout=10&samplingMode=7&timtype="+timType+"&format=0&transducerData="+arg).timeout(timeout*1000).execute();

	}

	/**
	 * Wi-Fi NCAP version of the getSensorData method converted by the NCAP
	 * @param wtimID - The WTIM ID
	 * @param channelID - The Channel ID
	 * @param timeOut - Timeout
	 * @return sensor data
	 * @throws IOException
	 */
	public String getSensorData(int wtimID , int channelID , int timeOut)throws IOException{

		try {

			//returns the sensor data at a certain channel id 
			String response = scrapePage(currentIP+"/1451/TransducerAccess/ReadData.htm?timId="+wtimID+"&channelId="+channelID+"&timeout="+timeOut+"&samplingMode=7&timtype=1&format=0");
			response =  response.substring(response.lastIndexOf("Transducer Data "), response.indexOf("© 2012 Esensors"));
			response = response.substring(response.indexOf("Transducer Data"));

			return "Transducer " + wtimID + " Data:" + response.substring(response.indexOf("Data")+4);

		}catch(StringIndexOutOfBoundsException |IOException ee) {
			ee.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns only the data no text just data.Can be parsed into a double 
	 * @param wtimID - the WTIM id
	 * @param channelID - the channel desired to access
	 * @param timeout - the timeout
	 * @return the converted sensor reading
	 * @throws IOException
	 */
	public String getSensorDataRaw(int wtimID , int channelID , int timeout) throws IOException{
		String data  = getSensorData(wtimID, channelID, timeout);
		return data.substring(data.indexOf("Data:")+5).trim();
	}


	/**
	 * This allows the user to read the TEDS from a specified TIM ID , along with other information
	 * @param timID - ID of the TIM
	 * @param channelID - what channel to get data from
	 * @param timeout - timout
	 * @param tedsType - which teds type
	 * @param timType - 1 WIFI 0 RS232
	 * @return the raw sensor data
	 * @throws IOException
	 */
	public String readRawTEDSFromTIM(int timID , int channelID, int timeout,int tedsType,int timType) throws IOException{

		String data = Jsoup.connect(currentIP+"/1451/TEDSManager/ReadRawTeds.htm?timId="+timID+"&channelId="+channelID+"&timeout="+timeout+"&tedsType="+tedsType+"&timtype="+timType+"&format=0").get().body().text();

		String toReturn = data.substring(data.indexOf("Error code"),data.lastIndexOf("TIM Id"))+"\n";
		toReturn += data.substring(data.lastIndexOf("TIM Id"),data.indexOf("Transducer Channel Id"))+"\n";
		toReturn +=data.substring(data.indexOf("Transducer Channel Id"),data.indexOf("TEDS Type"))+"\n";
		toReturn +=data.substring(data.indexOf("TEDS Type"),data.lastIndexOf("Raw TEDS"))+"\n";
		toReturn +=data.substring(data.lastIndexOf("Raw TEDS"),data.indexOf("© 2012 Esensors"));

		return toReturn;
	}

	/**
	 * Returns a list of all the connected WTIMs cached in the NCAP's memory
	 * @return A list of all the connected WTIMs
	 * @throws IOException
	 */
	public ArrayList<String> getCachedWTIMList() throws IOException{
		String data = scrapePage(currentIP+"/1451/Discovery/TIMDiscovery.htm?reptim=1");
		data = data.substring(data.indexOf("WTIM Ids")+13, data.indexOf("   © 2012 Esensors"));

		ArrayList<String> tims = new ArrayList<String>(); 

		for(String tim:data.split(",")) {
			if(!tim.isEmpty())
			tims.add(tim.trim());
		}
			

		return tims;
	}

	/**
	 * Returns the channels of a specified tim
	 * @param wtimId - id of desired tim
	 * @return - the channels
	 * @throws SocketTimeoutException
	 */
	public String getChannels(int wtimId) {
		String data = getTIMInfo(wtimId, 10, 1);
		System.out.println(data);
		data = data.substring(data.indexOf("Transducer Channel Ids")+"Transducer Channel Ids".length(),data.indexOf("Transducer Names")).trim();
		return data;
	} 


	/**
	 * Helper method that scrapes given url
	 * @param u - url
	 * @return - data from the scrape
	 * @throws IOException
	 */
	private String scrapePage(String u) throws IOException{
		return Jsoup.connect(u).timeout(timeout*1000).get().body().text();
	}

}