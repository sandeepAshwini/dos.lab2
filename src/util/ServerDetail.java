package util;

import java.io.Serializable;

public class ServerDetail implements Serializable {
	private static final long serialVersionUID = -7853196643095509724L;
	private String serviceName;
	private int PID;
	private String serviceAddress;

	public ServerDetail(String serviceName, int PID,
			String serviceAddress) {
		this.serviceName = serviceName;
		this.PID = PID;
		this.serviceAddress = serviceAddress;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public int getPID() {
		return PID;
	}

	public void setPID(int PID) {
		this.PID = PID;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}
	
	public String getServerName() {
		return this.serviceName + this.PID;
	}
}
