import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.jsoup.Jsoup;

/**
 * This class gives a user the ability to control WTIMs through an NCAP by HTTP access. ONLY FOR WIFI NCAP.
 * This class works by using the WEBPAGES of the NCAP and runs independently of them.
 * @author Rushad Antia
 */

public class NCAP{

	//this is the currentIP that the NCAP is hosting
	protected String currentIP;

	//holds timeout info
	protected int timeout;

	/** 
	 * Creates an object (instance) of the NCAP
	 * @param ip
	 */
	public NCAP(String ip,int time){
		currentIP = ip;
		this.timeout = time;

	}

	/**
	 * Get IP
	 * @return - Returns current IP
	 */
	public String getIP() {
		return currentIP;
	}

	/**
	 * Writes a message out to the screen 
	 * @param wtimId - The WTIM id Number 
	 * @param arg - The text to send to the screen
	 * @param timType - 1 is for Wi-Fi 0 is for the RS232 connection
	 * @throws IOException
	 */
	public void writeToScreen(int wtimId, String arg,int timType) throws IOException{

		writeTransducerData(wtimId, arg	, 9);

	}

	/**
	 * Writes data to a tim
	 * @param wtimId - id of the wtim
	 * @param argument - what to write
	 * @param channelId - channel you wish to write to 
	 * @throws IOException
	 */
	public void writeTransducerData(int wtimId,String argument,int channelId) throws IOException{
		//uses the JSoup library to excecute a command to a certain ip using http connection to the NCAP
		Jsoup.connect(currentIP+"/1451/TransducerAccess/WriteData.htm?timId="+wtimId+"&channelId="+channelId+"&timeout=10&samplingMode=7&timtype=1&format=0&transducerData="+argument).timeout(timeout*1000).execute();
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

		if(channelID>11)
			return null;

		try {
			//returns the sensor data at a certain channel id 
			String response = scrapePage(currentIP+"/1451/TransducerAccess/ReadData.htm?timId="+wtimID+"&channelId="+channelID+"&timeout="+timeOut+"&samplingMode=7&timtype=1&format=0");

			response =  response.substring(392, response.indexOf("© 2012 Esensors"));
			response = response.substring(response.indexOf("Transducer Data"));

			return "Transducer " + wtimID + " Data:" + response.substring(15);

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
		if(data==null)
			return data;

		String toReturn = data.substring(data.indexOf("Data: ")+5).trim();

		if(toReturn !=null)
			return toReturn;
		return null;

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

		String data = scrapePage(currentIP+"/1451/TEDSManager/ReadRawTeds.htm?timId="+timID+"&channelId="+channelID+"&timeout="+timeout+"&tedsType="+tedsType+"&timtype="+timType+"&format=0");
		data=data.substring(data.lastIndexOf("Raw TEDS"),data.indexOf("© 2012 Esensors"));

		if(data.equals("Raw TEDS   ")) 
			return "No TEDS Available";

		return data;
	}

	/**
	 * Returns a list of all the connected WTIMs cached in the NCAP's memory
	 * @return A list of all the connected WTIMs
	 * @throws IOException
	 */
	public ArrayList<String> getCachedWTIMList() throws IOException{

		String data = scrapePage(currentIP+"/1451/Discovery/TIMDiscovery.htm?reptim=1");

		data = data.substring(470, data.indexOf("   © 2012 Esensors"));

		ArrayList<String> tims = new ArrayList<String>(); 

		for(String tim:data.split(",")) 
			if(!tim.isEmpty())
				tims.add(tim.trim());

		return tims;
	}

	/**
	 * Returns the channels of a specified tim
	 * @param wtimId - id of desired tim
	 * @return - the channels
	 * @throws SocketTimeoutException
	 */
	public String getChannels(int wtimId) {

		String data = null;
		try {
			data = scrapePage(currentIP+ "/1451/Discovery/TransducerDiscovery.htm?timId="+wtimId+"&timeout=10&timtype=1&format=0");
		} catch (IOException e) {	
			e.printStackTrace();
		}
		if(data!=null)
			data = data.substring(354,data.indexOf("Transducer Names")).trim();
		else
			return null;

		if(!data.equals("Ids"))
			return data;

		return "No Channels Available";
	} 

	/**
	 * Checks if the tim is connected
	 * @param wtimId - id of the tim
	 * @return - true if connected
	 */
	public boolean isConnected(int wtimId) {
		try {
			String data = getChannels(wtimId);

			if(!data.equals("No Channels Available"))
				return true;
		}catch(Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * Gets all the connected WTIMS to the NCAP. CAN TAKE UP TO 90 SECONDS TO COMPLETE
	 * @param from - starting range 
	 * @param to - ending range
	 * @param continuous - true if you want to keep checking 
	 * @return A list of all the found tims
	 */
	public ArrayList<String> legitSearch(int from,int to) {

		if(from<2||to>254||from>to)
			throw new IllegalArgumentException("Invalid search indicies");

		ArrayList<String> foundTIMS = new ArrayList<String>(10);

		String data = null;
		try {
			data = Jsoup.connect(currentIP+"/1451/Discovery/TIMDiscovery.htm?wtimIdl="+from+"&wtimIdh="+to+"&reptim=0&timtype=1").timeout(70000).get().body().text();
		} catch (IOException e1) {
			e1.printStackTrace();
			foundTIMS.add("None Found");
			return foundTIMS;
		}

		String subbed=null;
		try {	 
			subbed = data.substring(data.indexOf("WTIM Ids List")+13,data.indexOf("   © 2012 Esensors"));
		}catch(Exception e) {e.printStackTrace();}

		for(String s : subbed.split(",")) 
			foundTIMS.add(s.trim());

		return foundTIMS;
	}

	/**
	 * Saves current discovery to cache of NCAP
	 */
	public void saveToCache() {
		try {
			Jsoup.connect(currentIP+"/1451/Discovery/TIMDiscovery.htm?reptim=2").timeout(timeout*1000).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Allows you to gain TEDS information that is readable
	 * @param wtimId
	 * @param channelId
	 * @param tedsType
	 * @param timeout
	 * @return
	 */
	public String queryTEDS(int wtimId,int channelId,int tedsType,int timeout) {

		try {
			String data = scrapePage(currentIP+"/1451/TEDSManager/QueryTeds.htm?timId="+wtimId+"&channelId="+channelId+"&timeout="+timeout+"&tedsType="+tedsType+"&timtype=1&format=0");
			int trans = data.indexOf("Transducer Channel Id ");
			String toReturn = data.substring(data.indexOf("TIM Id "),trans)+"\n";
			int tedsT = data.indexOf("TEDS Type");
			toReturn +=data.substring(trans,tedsT)+"\n";
			toReturn += data.substring(data.indexOf("TEDS Infor"),data.indexOf(" © 2012 Esensors"));
			return toReturn;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;		
	}

	/**
	 * Returns contents of a page
	 * @param u - URL
	 * @return
	 * @throws IOException
	 */
	private String scrapePage(String u) throws IOException{
		return Jsoup.connect(u).timeout(timeout*1000).get().body().text();
	}


	/**
	 * Gives a correct instance of the what type the ncap is
	 * @param currentIP
	 * @param timeout
	 * @return an ncap
	 */
	public static NCAP getNCAP(String currentIP,int timeout) {

		try {
			Jsoup.connect(currentIP+"/1451/RdTimSampleStatus.htm").get().body();
		} catch (IOException e) {
			return new NCAP(currentIP, timeout);
		}

		return new NCAP_ETH(currentIP, timeout);

	}

	/**
	 * Gives the connectiontype
	 * @return - wifi or ethernet
	 */
	public String currentConnectionType() {
		return "Wi-Fi";
	}

}