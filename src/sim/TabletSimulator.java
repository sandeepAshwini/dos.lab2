package sim;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;

import base.OlympicException;
import client.Tablet;

/**
 * A simulator to deploy N tablets and perform tests on them.
 * @author aravind
 *
 */
public class TabletSimulator {
	
	private static int DEFAULT_TABLET_LIMIT = 3;
	
	/**
	 * Creates and deploys N tablets as specified to be used for testing.
	 * @param numberOfTablets
	 * @param obelixHost
	 * @return List<Tablet>
	 * @throws OlympicException
	 */
	private static List<Tablet> createTablets(int numberOfTablets, String obelixHost) throws OlympicException {
		List<Tablet> tablets = new ArrayList<Tablet>();
		for(int i = 0; i < numberOfTablets; i++) {
			tablets.add(Tablet.deployTablet(new String[]{obelixHost}));
		}
		return tablets;
	}
	
	
	/**
	 * Creates an associated TabletTester for each Tablet 
	 * to work on a new thread and test the Tablet.
	 * @param tablets
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private static void test(List<Tablet> tablets) throws InterruptedException, IOException {
		List<Thread> threads = new ArrayList<Thread>();
		
		for(Tablet tablet : tablets) {
			TabletTester tester = new TabletTester(tablet);
			Thread thread = new Thread(tester);
			thread.start();
			threads.add(thread);
		}
	
		for(Thread thread : threads) {
			thread.join();
		}		
	}
	
	/**
	 * @param args
	 * @throws OlympicException
	 * @throws IOException
	 * @throws NotBoundException
	 */
	public static void main(String[] args) throws OlympicException, IOException, NotBoundException {
		String obelixHost = (args.length < 1) ? null : args[0];
		int numTablets = (args.length < 2) ? DEFAULT_TABLET_LIMIT : Integer.parseInt(args[1]);
		List<Tablet> tablets = createTablets(numTablets, obelixHost);
		try {
			test(tablets);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
