import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection; 
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class gives a user the ability to control WTIMs through an NCAP by HTTP access. ONLY FOR ETHERNET NCAP.
 * This class works by using the WEBPAGES of the NCAP and runs independently of them.
 * @author Rushad Antia
 * @since 8/4/14
 */
public class NCAP_ETH extends NCAP {

	private DocumentBuilder builder;

	public NCAP_ETH(String ip, int time) {
		super(ip, time); 

		System.out.println("Ethernet");

		try {
			builder  = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {e.printStackTrace();}
	}

	//works
	public ArrayList<String> getCachedWTIMList(){

		Document doc = parse(getXml(currentIP, "/1451/Discovery/TIMDiscovery.htm?reptim=1&format=1"));
		NodeList nodes = doc.getElementsByTagName("TIMDiscoveryHTTPResponse");
		Element element = (Element) nodes.item(0);
		NodeList data = element.getElementsByTagName("timIds");
		return new ArrayList<String>(Arrays.asList(getCharacterDataFromElement((Element)data.item(0)).split(",")));

	}

	//works
	public String getChannels(int wtimId) {

		Document doc = parse(getXml(currentIP, "/1451/Discovery/TransducerDiscovery.htm?timId="+wtimId+"&timeout=10&timtype=1&format=1"));
		NodeList nodes = doc.getElementsByTagName("TransducerDiscoveryHTTPResponse");
		Element element = (Element)nodes.item(0);
		NodeList data = element.getElementsByTagName("channelIds");
		return getCharacterDataFromElement((Element)data.item(0));

	}

	//works
	public String getSensorData(int wtimID , int channelID , int timeout) {

		Document document = parse(getXml(currentIP, "/1451/TransducerAccess/ReadData.htm?timId="+wtimID+"&channelId="+channelID+"&timeout="+timeout+"&samplingMode=7&timtype=1&format=1"));
		if(document==null)
			return "";
		NodeList nodes = document.getElementsByTagName("ReadDataHTTPResponse");

		Element element = (Element) nodes.item(0);

		NodeList data = element.getElementsByTagName("transducerData");

		return getCharacterDataFromElement((Element) data.item(0)).trim();	

	}

	//works
	public String getSensorDataRaw(int wtimID, int channelID, int timeout)throws IOException {
		return this.getSensorData(wtimID, channelID, timeout);
	}

	//works
	public ArrayList<String> legitSearch(int from, int to) {
		Document doc = parse(getXml(currentIP, "/1451/Discovery/TIMDiscovery.htm?wtimIdl="+from+"&wtimIdh="+to+"&reptim=0&timtype=1"));
		NodeList nodes = doc.getElementsByTagName("TIMDiscoveryHTTPResponse");
		Element element = (Element)nodes.item(0);
		NodeList data = element.getElementsByTagName("timIds");
		return new ArrayList<String>(Arrays.asList(getCharacterDataFromElement( (Element)data.item(0) ).split(",")));
	}

	//works
	public String readRawTEDSFromTIM(int timID, int channelID, int timeout,int tedsType, int timType) throws IOException {
		Document doc = parse(getXml(currentIP, "/1451/TEDSManager/ReadRawTeds.htm?timId="+timID+"&channelId="+channelID+"&timeout="+timeout+"&tedsType="+tedsType+"&timtype="+timType+"&format=1"));
		NodeList nodes = doc.getElementsByTagName("ReadRawTEDSHTTPResponse");
		Element element = (Element) nodes.item(0);
		NodeList data = element.getElementsByTagName("teds");
		String toReturn = getCharacterDataFromElement((Element) data.item(0));
		return toReturn.isEmpty() ? toReturn : "No TEDS Available";
	}

	//work
	public String queryTEDS(int wtimId, int channelId, int tedsType, int timeout) {

		Document doc = parse(getXml(currentIP, "/1451/TEDSManager/QueryTeds.htm?timId="+wtimId+"&channelId="+channelId+"&timeout="+timeout+"&tedsType="+tedsType+"&timtype=1&format=1"));
		NodeList nodes = doc.getElementsByTagName("QueryTEDSHTTPResponse");
		Element element = (Element) nodes.item(0);
		NodeList data = element.getElementsByTagName("tedsInfo");
		String toReturn = getCharacterDataFromElement((Element) data.item(0));

		return toReturn.isEmpty() ? "No Info Available" : toReturn;
	}



	public String currentConnectionType() {

		return "Ethernet";
	}

	/*** PRIVATE METHODS BELOW***/	



	/**
	 * Follows the redirects for the xml data
	 * @param header
	 * @param url
	 * @return
	 */
	private String getXml(String header,String url){;

	Response finalResponse = null;
	String thisUrl = url;

	for(int i=0;i<2;i++) {
		Response response = null;
		try {
			response = Jsoup.connect(header+thisUrl).followRedirects(false).timeout(1000*timeout*2).execute();
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

	/**
	 * Gets the character data from the element
	 * @param e
	 * @return
	 */
	private String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if(child instanceof CharacterData) {
			CharacterData cd = (CharacterData)child;
			return cd.getData();
		}
		return "?";
	}

	/**
	 * Returns the document containing the XML data
	 * @param xmlData
	 * @return
	 */
	private Document parse(String xmlData) {
		try {
			return builder.parse(new InputSource(new StringReader(xmlData)));
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
