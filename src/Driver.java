import java.io.IOException;


public class  Driver{



	public static void main(String[] args) throws IOException, InterruptedException {
		NCAP ncap = new NCAP("http://192.168.254.102", 10); 

		System.out.println(ncap.queryTEDS(106, 100, 1, 10));


	}

}

