package org.abstractica.deviceserver;

public interface DeviceServerListener
{
	boolean acceptAndInitializeNewDevice(Device device);
	void onDeviceAdded(Device device);
	void onDeviceRemoved(Device device);
}
