import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class  Driver{

	static int x=0;

	public static void main(String[] args) throws NumberFormatException, IOException, ParserConfigurationException, SAXException {

		NCAP ncap = NCAP.getNCAP("http://129.6.78.166", 10);//eth
		

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
	 
		System.out.println(ncap.queryTEDS(95, 7, 51, 10));
	}

	
}


