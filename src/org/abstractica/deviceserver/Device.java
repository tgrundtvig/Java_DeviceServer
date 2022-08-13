package org.abstractica.deviceserver;

public interface Device
{
	long getDeviceId();
	String getDeviceType();
	long getDeviceVersion();
	void setPacketHandler(DevicePacketHandler packetHandler);
	boolean addConnectionListener(DeviceConnectionListener listener);
	boolean removeConnectionListener(DeviceConnectionListener listener);
	boolean isConnected();
	void waitForConnection() throws InterruptedException;
	Response sendPacket(int command,
	                    int arg1,
	                    int arg2,
	                    byte[] packet,
	                    boolean blocking,
	                    boolean forceSend) throws InterruptedException;
	default int sendPacketAndWait(int command,
	                      int arg1,
	                      int arg2,
	                      byte[] packet,
	                      boolean blocking,
	                      boolean forceSend) throws InterruptedException
	{
		Response response = sendPacket(command, arg1, arg2, packet, blocking, forceSend);
		return response.getResponse();
	}
}
