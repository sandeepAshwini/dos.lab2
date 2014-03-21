package sim;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Random;

import base.EventCategories;
import base.NationCategories;
import base.OlympicException;
import client.Tablet;

/**
 * A Tablet testing utility that generates random requests 
 * to test the client tablet associated with this TabletTester.
 * @author aravind
 *
 */
public class TabletTester implements Runnable
{
	private Tablet tabletInstance;
	private static Random rand = new Random();
	private int numRequests;
	private static int counter = 0;
	private static int ALL_REQUESTS = 4;
	private static int CLIENT_PULL_REQUESTS_ONLY = 3;
	
	//Simulation parameters
	private static int SLEEP_INTERVAL = 5000;
	private static int MIN_REQUESTS = 5;
	private static int RANGE = 10;
	private boolean allowServerPush = true;
	
	/**
	 * @param tabletInstance
	 * @throws IOException
	 */
	public TabletTester(Tablet tabletInstance) throws IOException {
		this.tabletInstance = tabletInstance;
		try {
			this.tabletInstance.setOut("./output/TabletTester" + counter++ + ".txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		numRequests = rand.nextInt(RANGE) + MIN_REQUESTS;
	}
	
	/**
	 * Generates and returns a random event category to construct a random request on the tablet.
	 * @return EventCategories
	 */
	public EventCategories getEventType() {
		EventCategories[] events  = EventCategories.values();
		return events[rand.nextInt(events.length)];
	}
	
	/**
	 * Generates and returns a random nation to construct a random request on the tablet.
	 * @return NationCategories
	 */
	public NationCategories getNation() {
		NationCategories[] nations  = NationCategories.values();
		return nations[rand.nextInt(nations.length)];
	}

	/**
	 * Generates a random number to pick from a list of actions the tablet can perform 
	 * to test and measure the performance of various requests on the tablet.
	 */
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		try {
			for(int i = 0; i < numRequests; i++)
			{
				int requestNumber = rand.nextInt(CLIENT_PULL_REQUESTS_ONLY);
				if(allowServerPush) {
					requestNumber = rand.nextInt(ALL_REQUESTS);
				}
				
				switch(requestNumber){
				case 0: this.tabletInstance.getResults(this.getEventType());
						break;
				case 1: this.tabletInstance.getMedalTally(this.getNation());
						break;
				case 2: this.tabletInstance.getCurrentScore(this.getEventType());
					break;
				case 3: this.tabletInstance.subscribeTo(this.getEventType());
					break;
				}
				Thread.sleep(SLEEP_INTERVAL);
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (OlympicException e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Average latency : " + (endTime - startTime - SLEEP_INTERVAL * numRequests) / (double) this.numRequests);		
	}
	
	/**
	 * Method to allow or disallow server push requests during tablet tests.
	 * @param boolean
	 */
	public void allowServerPush(boolean allowServerPush) {
		this.allowServerPush = allowServerPush;		
	}
}


