import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class  Driver{

	static int x=0;

	public static void main(String[] args) throws NumberFormatException, IOException, ParserConfigurationException, SAXException {

		NCAP ncap = new NCAP("http://192.168.1.102", 10);

/*		for(int i=0;i<50;i++) {	
			long start = System.currentTimeMillis();

			System.out.println(ncap.getSensorDataXML(107, 3, 10));
			System.out.println("Time took for XML: "+(System.currentTimeMillis()-start)/1000.0);
			start = System.currentTimeMillis();

			System.out.println(ncap.getSensorDataRaw(107, 3, 10));
			System.out.println("Time took for HTML: "+(System.currentTimeMillis()-start)/1000.0);
		}
	}
*/

		System.out.println(ncap.getChannels(107));
	}
	

}

