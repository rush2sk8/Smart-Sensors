import java.io.IOException;

import org.jsoup.Jsoup;

/**
 * This class gives a user the ability to control WTIMs through an NCAP by HTTP access. ONLY FOR WIFI NCAP
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

	/**
	 * Gets all the connected WTIMS to the NCAP
	 * @param from - starting range 
	 * @param to - ending range
	 * @param continuous - true if you want to keep checking 
	 * @return A list of all the found tims
	 */
	public String discoverWTIMs(int from , int to){

		//makes sure that the indicies passed in are valid
		if(from<2||to>254||to<from)
			throw new IllegalArgumentException("Invalid search indicies");

		for(int i=from;i<=to;i++) {
			String data = getTIMInfo(i, 10, 1);
			System.out.println(data);
		}

		return null;

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
			e.printStackTrace();

			//makes sure that we return because otherwise we would substring stuff that doesnt exist throwing an null pointer. also protects against sockettimiing out 
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
	public void writeToScreen(int wtimId, String arg,int timType) throws IOException {

		//uses the JSoup library to excecute a command to a certain ip using http connection to the NCAP
		Jsoup.connect(currentIP+"/1451/TransducerAccess/WriteData.htm?timId="+wtimId+"&channelId=9&timeout=10&samplingMode=7&timtype="+timType+"&format=0&transducerData="+arg).execute();

	}

	/**
	 * Wi-Fi NCAP version of the getSensorData method coonverted by the NCAP
	 * @param wtimID - The WTIM ID
	 * @param channelID - The Channel ID
	 * @param timeOut - Timeout
	 * @return sensor data
	 * @throws IOException
	 */
	public String getSensorData(int wtimID , int channelID , int timeOut)throws IOException {

		try {

			//returns the sensor data at a certain channel id 
			String response = Jsoup.connect(currentIP+"/1451/TransducerAccess/ReadData.htm?timId="+wtimID+"&channelId="+channelID+"&timeout="+timeOut+"&samplingMode=7&timtype=1&format=0").get().body().text();
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
	public String getSensorDataRaw(int wtimID , int channelID , int timeout) throws IOException {
		String data  = getSensorData(wtimID, channelID, timeout);
		return data.substring(data.indexOf("Data:")+5).trim();
	}

	/**
	 * Retrieves sensor data of a certain sensor at an interval.Works in its own thread
	 * @param wtimID - The WTIM id 
	 * @param channelID - The Channel ID
	 * @param interval - The interval to wait (in seconds)
	 * @param numSamples - The number of times to retrieve data at X intervals
	 * @throws IOException 
	 * @throws InterruptedException
	 */
	public void displaySensorDataAtInterval(final int wtimID ,final int channelID , final int interval, final int numSamples) throws IOException, InterruptedException {

		Thread x = new Thread(new Runnable() {

			@SuppressWarnings("static-access")
			@Override
			public void run() {
				for(int i=0;i<numSamples;i++) {

					try {
						System.out.println(getSensorData(wtimID, channelID, interval));
					} catch (IOException e1) {
						// TODO fix all the stuff with this
						e1.printStackTrace();
					}

					//statically sleeps the current thread that it is on 
					try {
						Thread.currentThread().sleep((long)interval*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		x.start();
		x.join();

	}

	/** 
	 * NOTICE: Invokes a thread to do the text take heed.
	 * Starts a scrolling text on the screen of a WTIM.
	 * @param wtimID - the id of the WTIM
	 * @param textToDisplay - the text to display
	 */
	public void startScrollingText(final int wtimID ,final String textToDisplay) {
		//makes flag true so that i can re-use this method
		flag = true;

		//creates and starts a new thread
		new Thread(new Runnable() {

			@SuppressWarnings("static-access")

			@Override
			public void run() {

				//makes the current text the textToDisplay
				String currentText = textToDisplay;

				//keeps going according to the flag. Unless the flag is false. That is why it is volatile
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
	 * This allows the user to read the TEDS from a specified TIM ID , along with other information
	 * @param timID
	 * @param channelID
	 * @param timeout
	 * @param tedsType
	 * @param timType
	 * @return the raw sensor data
	 * @throws IOException
	 */
	public String readRawTEDSFromTIM(int timID , int channelID, int timeout,int tedsType,int timType) throws IOException {

		String data = Jsoup.connect(currentIP+"/1451/TEDSManager/ReadRawTeds.htm?timId="+timID+"&channelId="+channelID+"&timeout="+timeout+"&tedsType="+tedsType+"&timtype="+timType+"&format=0").get().body().text();

		String toReturn = data.substring(data.indexOf("Error code"),data.lastIndexOf("TIM Id"))+"\n";
		toReturn += data.substring(data.lastIndexOf("TIM Id"),data.indexOf("Transducer Channel Id"))+"\n";
		toReturn +=data.substring(data.indexOf("Transducer Channel Id"),data.indexOf("TEDS Type"))+"\n";
		toReturn +=data.substring(data.indexOf("TEDS Type"),data.lastIndexOf("Raw TEDS"))+"\n";
		toReturn +=data.substring(data.lastIndexOf("Raw TEDS"),data.indexOf("© 2012 Esensors"));

		return toReturn;
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