import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Enumeration;


public class  Driver{



	public static void main(String[] args) throws IOException, InterruptedException {
		NCAP ncap = new NCAP("http://192.168.254.102", 10); 
		Enumeration<NetworkInterface> interfaces = null;
		interfaces = NetworkInterface.getNetworkInterfaces();
		while(interfaces.hasMoreElements()) {
			NetworkInterface nic = interfaces.nextElement();
			if(nic.isUp())
				System.out.println("Interface Name: "+nic.getDisplayName());

		}
	}

}

