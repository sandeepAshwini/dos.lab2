package server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.RegistryService;
import util.ServerDetail;
import base.OlympicException;

public class ServiceFinder implements ServiceFinderInterface {

	private static Random random;
	private static ServiceFinder serviceFinderInstance;
	private static String JAVA_RMI_HOSTNAME_PROPERTY = "java.rmi.server.hostname";
	private static int JAVA_RMI_PORT = 1099;

	private List<ServerDetail> services = new ArrayList<ServerDetail>();

	public ServiceFinder() {
		random = new Random();
	}

	public static void main(String[] args) throws OlympicException {
		ServiceFinder serviceFinderInstance = ServiceFinder
				.getServiceFinderInstance();
		try {
			RegistryService regService = new RegistryService();
			System.setProperty(JAVA_RMI_HOSTNAME_PROPERTY,
					regService.getLocalIPAddress());
			serviceFinderInstance.setupServiceFinder(regService);
		} catch (IOException e) {
			throw new OlympicException(
					"Registry Service could not be created.", e);
		}
	}

	private static ServiceFinder getServiceFinderInstance() {
		if (ServiceFinder.serviceFinderInstance == null) {
			ServiceFinder.serviceFinderInstance = new ServiceFinder();
		}
		return ServiceFinder.serviceFinderInstance;
	}

	private void setupServiceFinder(RegistryService regService)
			throws IOException, OlympicException {
		Registry registry = null;
		String SERVER_NAME = "ServiceFinder";
		ServiceFinderInterface serverStub = null;

		try {
			serverStub = (ServiceFinderInterface) UnicastRemoteObject
					.exportObject(ServiceFinder.getServiceFinderInstance(), 0);
			registry = LocateRegistry.getRegistry(JAVA_RMI_PORT);
			registry.rebind(SERVER_NAME, serverStub);
			System.err.println("Registry Service running at "
					+ regService.getLocalIPAddress() + ".");
			System.err.println("ServiceFinder ready.");
		} catch (RemoteException e) {
			regService.setupLocalRegistry();
			registry = LocateRegistry.getRegistry(JAVA_RMI_PORT);
			registry.rebind(SERVER_NAME, serverStub);
			System.err
					.println("New Registry Service created. ServiceFinder ready.");
		}
	}

	@Override
	public void registerService(String serviceName, int PID, String address)
			throws RemoteException {
		this.services.add(new ServerDetail(serviceName, PID, address));
	}

	@Override
	public ServerDetail getService(String serviceName) throws RemoteException {
		List<ServerDetail> matchingServices = getServices(serviceName);		
		return matchingServices.get(random.nextInt(matchingServices.size()));
	}
	
	@Override
	public List<ServerDetail> getServices(String serviceName) throws RemoteException {
		List<ServerDetail> matchingServices = new ArrayList<ServerDetail>();
		for (ServerDetail curServerDetail : this.services) {
			if (curServerDetail.getServiceName().equals(serviceName)) {
				matchingServices.add(curServerDetail);
			}
		}
		return matchingServices;
	}
}
