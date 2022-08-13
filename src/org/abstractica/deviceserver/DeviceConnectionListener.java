package org.abstractica.deviceserver;

public interface DeviceConnectionListener
{
	void onCreated();
	void onConnected();
	void onDisconnected();
	void onLost();
	void onDestroyed();
}
