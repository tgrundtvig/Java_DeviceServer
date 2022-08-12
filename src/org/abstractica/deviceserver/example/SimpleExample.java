package org.abstractica.deviceserver.example;

import org.abstractica.deviceserver.impl.DeviceServerImpl;
import org.abstractica.deviceserver.PacketSendCallback;
import org.abstractica.deviceserver.DeviceServer;
import org.abstractica.deviceserver.DeviceServerListener;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class SimpleExample
{
	public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException
	{
		DeviceServer deviceServer = new DeviceServerImpl(3377, 1024, 10, 1000, new SimpleListener());
		deviceServer.start();
		int command = 0;
		while(true)
		{
			long[] ids = deviceServer.getAllDeviceIds();
			if(ids.length > 0)
			{
				++command;
				System.out.println("Sending command: " + command + " to all devices.");
				for (long id : ids)
				{
					int msgId = deviceServer.sendPacket(id, command, 1, 2, null, true, false, new SimplePacketSendCallback());
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
			Thread.sleep(7000);
		}
	}

	private static class SimpleListener implements DeviceServerListener
	{

		@Override
		public boolean acceptDevice(long deviceId, String deviceType, int deviceVersion)
		{
			return true;
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
			return 0;
		}
	}

	private static class SimplePacketSendCallback implements PacketSendCallback
	{

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
}


