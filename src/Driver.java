import java.io.IOException;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class  Driver{

	static int x=0;

	public static void main(String[] args) throws NumberFormatException, IOException, ParserConfigurationException, SAXException {

		NCAP ncap = NCAP.getNCAP("http://129.6.78.166", 10);//eth
		//NCAP ncap = NCAP.getNCAP("http://192.168.254.102", 10);

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
		Scanner key = new Scanner(System.in);

		while(true) {
			System.out.println("0 Open 1 Close:");
			String data = key.next();

			if(data.equals("0")) 
				ncap.writeTransducerData(105, data, 7);
			else if(data.equals("1")) 
				ncap.writeTransducerData(105, data, 7);
			else 
				break;
		}
		key.close();
	}
}



