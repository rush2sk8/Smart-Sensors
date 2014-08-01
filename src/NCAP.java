import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;







import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class gives a user the ability to control WTIMs through an NCAP by HTTP access. ONLY FOR WIFI NCAP.
 * Tends to to throw <code>SocketTimeoutException</code> when connection is not well.
 * 
 * 
 * This class works by using the WEBPAGES of the NCAP and runs independently of them.
 * @author Rushad Antia
 */

public class NCAP{

	//this is the currentIP that the NCAP is hosting
	private String currentIP;

	//holds timeout info
	private int timeout;

	//docBuilder for holding XML data
	private	DocumentBuilder docBuilder;

	private boolean isEthernet;

	/** 
	 * Creates an object (instance) of the NCAP
	 * @param ip
	 */
	public NCAP(String ip,int time){
		currentIP = ip;
		timeout = time;
		try {
			docBuilder  = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		isEthernet = isOnEthernet(ip);
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
	 * Allows you to retrieve data such as channels and names of a specified transducer.	XXX XML
	 * Retrieves the information about a TIM
	 * @param timId - The ID of the TIM
	 * @param timeOut - The Timeout in seconds
	 * @return Returns the Information about the requested TIM
	 */
	public String getTIMInfo(int timId,int timeOut,int timType) {
		String toReturn = "";

		String data = getXml(currentIP,  "/1451/Discovery/TransducerDiscovery.htm?timId="+timId+"&timeout="+timeOut+"&timtype="+timType+"&format=1");

		Document parse  = parse(data);

		NodeList list = parse.getElementsByTagName("TransducerDiscoveryHTTPResponse");


		Element element = (Element)list.item(0);

		NodeList ids = element.getElementsByTagName("channelIds");
		Element line = (Element) ids.item(0);

		toReturn += "TIM ID: "+ timId+"\n";
		toReturn += "Channel Ids: "+ getCharacterDataFromElement(line);

		return toReturn;
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
		if(isEthernet) 
			return getSensorDataXML(wtimID, channelID, timeout);

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
		if(isEthernet)
			return getCachedWTIMListXML();

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
		if(isEthernet)
			return getChannelsXML(wtimId);

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
			String data = scrapePage(currentIP+"/1451/TEDSManager/QueryTeds.htm?timId="+wtimId+"&channelId="+channelId+"&timeout="+timeout+"&tedsType="+tedsType+"&timtype=1");
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

	private String getSensorDataXML(int wtimID , int channelID , int timeout) {

		Document document = parse(getXml(currentIP, "/1451/TransducerAccess/ReadData.htm?timId="+wtimID+"&channelId="+channelID+"&timeout="+timeout+"&samplingMode=7&timtype=1&format=1"));

		NodeList nodes = document.getElementsByTagName("ReadDataHTTPResponse");

		Element element = (Element) nodes.item(0);

		NodeList data = element.getElementsByTagName("transducerData");

		return getCharacterDataFromElement((Element) data.item(0)).trim();	

	}

	private String scrapePage(String u) throws IOException{
		return Jsoup.connect(u).timeout(timeout*1000).get().body().text();
	}

	private String getXml(String header,String url){;

	Response finalResponse = null;
	String thisUrl = url;

	for(int i=0;i<2;i++) {
		Response response = null;
		try {
			response = Jsoup.connect(header+thisUrl).followRedirects(false).timeout(1000*timeout).execute();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		if(response==null)
			return "Problem";
		
		finalResponse = response;
		int status = response.statusCode();

		if(status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
			String redirect = response.header("location");
			thisUrl = redirect;
		}
	}

	return finalResponse.body();
	}

	private String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if(child instanceof CharacterData) {
			CharacterData cd = (CharacterData)child;
			return cd.getData();
		}
		return "?";
	}

	private Document parse(String xmlData) {
		try {
			return docBuilder.parse(new InputSource(new StringReader(xmlData)));
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean isOnEthernet(String url) {
		try {
			Jsoup.connect(url+"/1451/RdTimSamplesStatus.htm").get().body();
		} catch (IOException e) {
			return false;
		} 
		return true;
	}

	private ArrayList<String> getCachedWTIMListXML(){
		System.out.println("eth");
		Document doc = parse(getXml(currentIP, "/1451/Discovery/TIMDiscovery.htm?reptim=1&format=1"));
		NodeList nodes = doc.getElementsByTagName("TIMDiscoveryHTTPResponse");
		Element element = (Element) nodes.item(0);
		NodeList data = element.getElementsByTagName("timIds");
		return new ArrayList<String>(Arrays.asList(getCharacterDataFromElement((Element)data.item(0)).split(",")));

	}

	private String getChannelsXML(int wtimId) {
		String data =  getTIMInfo(wtimId, 10, 1);
		return data.substring(data.indexOf("Channel Ids: ")+13);
//TODO Make an NCAP ETH calss and do it that way
	}


	public boolean isEthernet() {
		return isEthernet;
	}
}