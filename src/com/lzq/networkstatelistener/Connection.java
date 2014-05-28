package com.lzq.networkstatelistener;

public class Connection implements Cloneable {

	String srcPort = null;
	String desPort = null;

	String srcIP = null;
	String desIP = null;

	String Type = null;

	String State = null;

	int index = 0;

	long startTime = 0;
	long endTime = 0;

	public Connection() {

	}

	public Connection(String type, String srcIP, String srcPort, String desIP,
			String desPort) {
		super();
		this.srcPort = srcPort;
		this.desPort = desPort;
		this.srcIP = srcIP;
		this.desIP = desIP;
		Type = type;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public String getSrcPort() {
		return srcPort;
	}

	public void setSrcPort(String srcPort) {
		this.srcPort = srcPort;
	}

	public String getDesPort() {
		return desPort;
	}

	public void setDesPort(String desPort) {
		this.desPort = desPort;
	}

	public String getSrcIP() {
		return srcIP;
	}

	public void setSrcIP(String srcIP) {
		this.srcIP = srcIP;
	}

	public String getDesIP() {
		return desIP;
	}

	public void setDesIP(String desIP) {
		this.desIP = desIP;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getState() {
		return State;
	}

	public void setState(String state) {
		State = state;
	}

	public String toString() {
		return srcIP + ":" + srcPort + "  " + desIP + ":" + desPort + " "
				+ index + " " + startTime + " " + endTime;
	}

	@Override
	protected Connection clone() {

		Connection clone = null;

		try {

			clone = (Connection) super.clone();

		} catch (CloneNotSupportedException e) {

			throw new RuntimeException(e); // won't happen

		}

		return clone;

	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		boolean result=false;
		
		if(obj instanceof Connection)
		{
			Connection that=(Connection)obj;
			result=(this.getSrcIP().equals(that.getSrcIP())&&this.getSrcPort().equals(that.getSrcPort())&&this.getDesPort().equals(that.getDesPort())&&this.getDesIP().equals(that.getDesIP()));
					
		}
		return result;
	}


    @Override public int hashCode() {
        return (1 * (41 + Integer.parseInt(getDesPort()) + Integer.parseInt(getSrcPort())));
    }


}
