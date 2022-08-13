package org.abstractica.deviceserver.examples;

import org.abstractica.deviceserver.*;
import org.abstractica.deviceserver.impl.DeviceServerImpl;

import java.net.SocketException;
import java.net.UnknownHostException;

public class DeviceServerExample implements DeviceServerListener, DevicePacketHandler, DeviceConnectionListener
{
	public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException
	{
		(new DeviceServerExample()).runExample();
	}

	public void runExample() throws InterruptedException, SocketException, UnknownHostException
	{
		System.out.println("Creating server...");
		DeviceServer server = new DeviceServerImpl(3377,1024,10,1000, this);
		System.out.println("Creating device...");
		Device myDevice = server.createDevice(10313748, "TestDevice", 1);
		System.out.println("Adding listener...");
		myDevice.addConnectionListener(this);
		System.out.println("Adding device to server...");
		server.addDevice(myDevice);
		System.out.println("Starting server...");
		server.start();
		System.out.println("Waiting for connection...");
		myDevice.waitForConnection();
		//server.waitForAllDevicesToConnect();
		System.out.println("Device is connected!");
		System.out.println("Sending packet!");
		Response response = myDevice.sendPacket(42, 1, 2, null, true, false);
		System.out.println("Waiting for response...");
		System.out.println("Response: " + response.getResponse());
	}

	@Override
	public boolean acceptAndInitializeNewDevice(Device device)
	{
		System.out.println("ServerListener: acceptAndInitializeNewDevice -> " + device);
		if(!device.getDeviceType().equals("TestDevice") || !(device.getDeviceVersion() == 1))
		{
			return false;
		}
		device.setPacketHandler(this);
		return true;
	}

	@Override
	public void onDeviceAdded(Device device)
	{
		System.out.println("ServerListener: onDeviceAdded -> " + device);
	}

	@Override
	public void onDeviceRemoved(Device device)
	{
		System.out.println("ServerListener: onDeviceRemoved -> " + device);
	}

	@Override
	public int onPacket(int command, int arg1, int arg2, byte[] load)
	{
		System.out.print("DevicePacketHandler: onPacket -> ");
		System.out.println("(Command: " + command + ", arg1: " + arg1 + ", arg2: " + arg2 + ")");
		return 0;
	}

	@Override
	public void onCreated()
	{
		System.out.println("DeviceConnectionListener: onCreated()");
	}

	@Override
	public void onConnected()
	{
		System.out.println("DeviceConnectionListener: onConnected()");
	}

	@Override
	public void onDisconnected()
	{
		System.out.println("DeviceConnectionListener: onDisconnected()");
	}

	@Override
	public void onLost()
	{
		System.out.println("DeviceConnectionListener: onLost()");
	}

	@Override
	public void onDestroyed()
	{
		System.out.println("DeviceConnectionListener: onDestroyed()");
	}
}
