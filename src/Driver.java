import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class  Driver{

 static NCAP ncap = new NCAP("http://192.168.254.102", 10); 
	public static void main(String[] args) throws IOException, InterruptedException {

	ncap.writeTransducerData(107, "Hello", 9);
		
	}

	public static void WRITEALLTEDS() throws IOException {
		FileWriter outFile = new FileWriter(new File("allGoldTEDS.txt"));
		int[] nums = {95,105,106,107};
	
		for(int num:nums) {
			for(int i=0;i<20;i++) {
				for(int k=0;k<20;k++) {
					System.out.println(i+" "+k);
					String data = ncap.readRawTEDSFromTIM(num, i, 10, k, 1);
					if(!data.contains("No TEDS Available"))
					outFile.write("TIM ID: " + num + "@ Channel ID: "+i+" and @TEDSType: "+k + data+"\r\n");
				}
			}
		}
		outFile.close();
		Toolkit.getDefaultToolkit().beep();
	}

	public static void doConcurrentDataRetreival(final int times) {
		final Thread x  = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					RegTest.GetMultiplePiecesOfData(times, 106, 3, 5);
				} catch (IOException e) {

					e.printStackTrace();

				}

			}
		});
		Thread y = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					RegTest.GetMultiplePiecesOfData(times, 107, 3, 3);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});

		long s = System.currentTimeMillis();

		y.start();
		x.start();

		try {
			y.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		try {
			x.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		System.out.println("Time taken: " + (System.currentTimeMillis()-s)/1000. + " seconds");
	}

	public static void doMultipleThings() throws InterruptedException {

		long current = System.currentTimeMillis();


		Thread x = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					RegTest.RunSimultaneousOperationsTest(50, 106, 10, 1);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}

			}
		});
		Thread y = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					RegTest.RunSimultaneousOperationsTest(50, 107, 10, 1);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}

			}
		});
		x.start();
		y.start();x.join();y.join();

		System.out.println("Total Time To Do Alot of calculation and network stuff: "+(System.currentTimeMillis()-current)/1000.0);

	}

}

