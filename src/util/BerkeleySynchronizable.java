package util;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BerkeleySynchronizable extends Remote {
	public long getTime() throws RemoteException;
	public void setClockOffset(long clockOffset) throws RemoteException;
}
