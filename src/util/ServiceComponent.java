package util;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Random;

import server.ServiceFinderInterface;
import base.OlympicException;

public abstract class ServiceComponent {

	private static Random random;
	private ServiceFinderInterface serviceFinderStub;
	private String serviceFinderHost;
	protected int PID;
	protected String serviceName;

	private static String SERVICE_FINDER_NAME = "ServiceFinder";
	private static int JAVA_RMI_PORT = 1099;
	
	
	public ServiceComponent(){
		random = new Random();
		this.PID = Math.abs(random.nextInt());
	}

	public ServiceComponent(String serviceName, String serviceFinderHost) {
		this.serviceName = serviceName;
		try {
			setServiceFinderHost(serviceFinderHost);
		} catch (OlympicException e) {
			e.printStackTrace();
		}
		random = new Random();
		this.PID = Math.abs(random.nextInt());
	}
	
	public void setServiceFinderHost(String serviceFinderHost) throws OlympicException {
		this.serviceFinderHost = serviceFinderHost;
		setupServiceFinderStub();
	}

	private void setupServiceFinderStub() throws OlympicException {
		Registry registry = null;
		try {
			registry = LocateRegistry.getRegistry(serviceFinderHost,
					JAVA_RMI_PORT);
			ServiceFinderInterface serviceFinderStub = (ServiceFinderInterface) registry
					.lookup(SERVICE_FINDER_NAME);
			this.serviceFinderStub = serviceFinderStub;
		} catch (Exception e) {
			throw new OlympicException("Could not set up Service Finder Stub.");
		}
	}
	
	public String getServerName() {
		return this.serviceName + this.PID;
	}

	public void register(String serviceName, String address) throws RemoteException {
		serviceFinderStub.registerService(serviceName, PID, address);
	}
	
	public ServerDetail getServerDetail(String serviceName) throws RemoteException {
		return serviceFinderStub.getService(serviceName);
	}
	
	public List<ServerDetail> getServerDetails(String serviceName) throws RemoteException {
		return serviceFinderStub.getServices(serviceName);
	}
}
