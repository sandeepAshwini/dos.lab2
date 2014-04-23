package base;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface to allow synchronized printing in the client tablet.
 * 
 * @author sandeep
 * 
 */
public abstract class Printable implements Serializable {

	private static final long serialVersionUID = 9081762682126928445L;
	public Long timestamp;

	public void printContents() {
	}

	public List<Printable> convertToList() {
		List<Printable> tempList = new ArrayList<Printable>();
		tempList.add(this);
		return tempList;
	}

	public void writeToFile(FileWriter writer) throws IOException {
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Long getTimestamp() {
		return this.timestamp;
	}
}
