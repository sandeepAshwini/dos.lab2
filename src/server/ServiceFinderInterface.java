package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import util.ServerDetail;

public interface ServiceFinderInterface extends Remote {
	public void registerService(String serviceName, int PID, String address) throws RemoteException;
	public ServerDetail getService(String serviceName) throws RemoteException;
	public List<ServerDetail> getServices(String serviceName) throws RemoteException;
}
