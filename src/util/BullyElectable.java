package util;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BullyElectable extends Remote {
	public void startElection(int callerPID) throws RemoteException;
	public void notifyAlive() throws RemoteException;
	public void notifyVictory(String callerServerName) throws RemoteException;
}
