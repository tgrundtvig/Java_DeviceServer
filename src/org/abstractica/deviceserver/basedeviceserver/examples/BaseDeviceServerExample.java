package org.abstractica.deviceserver.basedeviceserver.examples;

import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServer;
import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServerListener;
import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServerPacketSendCallback;
import org.abstractica.deviceserver.basedeviceserver.impl.BaseDeviceServerImpl;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class BaseDeviceServerExample implements BaseDeviceServerListener, BaseDeviceServerPacketSendCallback
{
	public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException
	{
		BaseDeviceServerExample exampleServer = new BaseDeviceServerExample(3377, 1024, 10, 1000);
		exampleServer.run();
	}

	private BaseDeviceServer server;

	public BaseDeviceServerExample(int port,
	                               int maxPacketSize,
	                               int bufferSize,
	                               long updateInterval) throws SocketException, UnknownHostException
	{
		server = new BaseDeviceServerImpl(port, maxPacketSize, bufferSize, updateInterval, this);
	}

	public void run() throws InterruptedException
	{
		server.start();
		int command = 0;
		while(true)
		{
			long[] ids = server.getAllDeviceIds();
			if(ids.length > 0)
			{
				++command;
				System.out.println("Sending command: " + command + " to all devices.");
				for (long id : ids)
				{
					int msgId = server.sendPacket(id, command, 1, 2, null, true, false, this);
					if (msgId < 0)
					{
						System.out.println("Could not send to: " + id);
					}
					else
					{
						System.out.println("Message beeing send. msgId: " + msgId);
					}
				}
			}
			if(command % 3 == 0 && ids.length > 0)
			{
				server.removeDevice(ids[0]);
			}
			Thread.sleep(7000);
		}
	}

	@Override
	public boolean acceptDevice(long deviceId, String deviceType, int deviceVersion)
	{
		return "TestDevice".equals(deviceType) && 1 == deviceVersion;
	}

	@Override
	public void onNewDevice(long deviceId, String deviceType, int deviceVersion)
	{
		System.out.println("New device (id: " + deviceId + ", type: " + deviceType + ", version: " + deviceVersion + ")");
		System.out.println();
	}

	@Override
	public void onDeviceConnected(long deviceId)
	{
		System.out.println("Device connected: " + deviceId);
		System.out.println();
	}

	@Override
	public void onDeviceDisconnected(long deviceId)
	{
		System.out.println("Device disconnected: " + deviceId);
		System.out.println();
	}

	@Override
	public void onDeviceLost(long deviceId)
	{
		System.out.println("Device lost: " + deviceId);
		System.out.println();
	}

	@Override
	public int onDevicePacketReceived(long deviceId, int command, int arg1, int arg2, byte[] load)
	{
		System.out.println();
		System.out.println("Packet received from device: " + deviceId);
		System.out.println("  Command: " + command);
		System.out.println("  Arg1: " + arg1);
		System.out.println("  Arg2: " + arg2);
		if(load != null)
		{
			System.out.println("  Load size: " + load.length);
			for(int i = 0; i < load.length; ++i)
			{
				System.out.println("load[" + i + "] = " + load[i]);
			}
			String s = new String(load, StandardCharsets.US_ASCII);
			System.out.println(s);
		}
		System.out.println();
		//This is the response the client will recieve..
		//We just send back the command, to see that we get it back.
		return command;
	}

	@Override
	public void onPacketDelivered(long deviceId, int msgId, int response)
	{
		System.out.println("Packet delivered to: " + deviceId + " (msgId: " + msgId + ", response: " + response + ")");
	}

	@Override
	public void onPacketCancelled(long deviceId, int msgId)
	{
		System.out.println("Packet cancelled: (deviceId: " + deviceId + ", msgId: " + msgId + ")");
	}
}


