import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class  Driver{
 
	@SuppressWarnings("unused")
	public static void main(String[] args) throws NumberFormatException, IOException, ParserConfigurationException, SAXException, InterruptedException {

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

/*		Scanner key = new Scanner(System.in);
		int i=0;
		while(true) {


			if(i==0) {

				ncap.writeTransducerData(105, i+"", 7);i=1;
			}else if(i==1) {
				ncap.writeTransducerData(105, i+"", 7);
				i=0;
			}
			Thread.sleep(1000);
		}*/
		
		
		
	}
}



