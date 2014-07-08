import java.io.IOException;


public class WTIMTextScroller extends Thread{

	private final int WTIMId;
	private String text;
	private NCAP ncap;
	private volatile boolean flag;

	public WTIMTextScroller(int wTIMId, String text,NCAP ncap) {
		WTIMId = wTIMId;
		this.text = text;
		this.ncap = ncap;
		flag = true;
	}

	@Override
	public void run() {
		flag = true;

		while(flag) {
			String oldText = text;
			String newText = oldText.substring(1)+oldText.substring(0,1);
			try {
				ncap.writeToScreen(107, newText, 1);
			} catch (IOException e) {

				e.printStackTrace();
			}
			text = newText;	
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
	}

	public void stopScrolling() {
		flag = false;
	}
}
