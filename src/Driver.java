import java.io.IOException;


public class  Driver{

	final static NCAP ncap  = new NCAP("http://192.168.254.102",10);
	public static void main(String[] args) throws IOException, InterruptedException {

		System.out.println(ncap.getSensorDataRaw(105,99, 10));
		
		
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

