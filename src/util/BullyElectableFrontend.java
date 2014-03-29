package util;

import java.rmi.RemoteException;
import java.util.List;

public interface BullyElectableFrontend extends BullyElectable {
	public LamportClock notifyTimeStamp(LamportClock incomingTimeStamp)
			throws RemoteException;

	public void addParticipant(String participantID) throws RemoteException;

	public void setLotteryEnterFrequency(int lotteryEnterFrequency)
			throws RemoteException;

	public void setLotteryEnterFrequency(Integer PID, int lotteryEnterFrequency)
			throws RemoteException;
	
	public List<Double> getLoadStatistics() throws RemoteException;
	
	public int getRequestCount() throws RemoteException;
	
	public void freezeLottery() throws RemoteException;
}
