package org.abstractica.deviceserver.impl;

import org.abstractica.deviceserver.*;
import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServer;
import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServerListener;
import org.abstractica.deviceserver.basedeviceserver.impl.BaseDeviceServerImpl;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class DeviceServerImpl implements DeviceServer, BaseDeviceServerListener
{
	private final BaseDeviceServer baseServer;
	private final DeviceServerListener listener;
	private final Map<Long, DeviceImpl> map;

	public DeviceServerImpl(int port,
	                        int maxPacketSize,
	                        int bufferSize,
	                        long updateInterval,
	                        DeviceServerListener listener) throws SocketException, UnknownHostException
	{
		this.listener = listener;
		this.map = new HashMap<>();
		baseServer = new BaseDeviceServerImpl(  port,
												maxPacketSize,
												bufferSize,
												updateInterval,
											this);
	}

	@Override
	public Device createDevice(long deviceId, String deviceType, long deviceVersion)
	{
		return new DeviceImpl(deviceId, deviceType, deviceVersion);
	}

	@Override
	public void start()
	{
		baseServer.start();
	}

	@Override
	public void stop() throws InterruptedException
	{
		baseServer.stopGracefully();
	}

	@Override
	public boolean addDevice(Device device)
	{
		synchronized (map)
		{
			Device dev = map.get(device.getDeviceId());
			if (dev != null)
			{
				if (dev.getDeviceType().equals(device.getDeviceType()) &&
						dev.getDeviceVersion() == device.getDeviceVersion())
				{
					return false;
				}
				map.remove(dev.getDeviceId());
				listener.onDeviceRemoved(dev);
			}
			map.put(device.getDeviceId(), (DeviceImpl) device);
			listener.onDeviceAdded(device);
			map.notifyAll();
			return true;
		}
	}

	@Override
	public boolean removeDevice(Device device)
	{
		synchronized(map)
		{
			Device dev = map.get(device.getDeviceId());
			if (dev == null)
			{
				return false;
			}
			map.remove(device.getDeviceId());
			listener.onDeviceRemoved(device);
			map.notifyAll();
			return true;
		}
	}

	@Override
	public Collection<Device> getAllDevices()
	{
		synchronized(map)
		{
			//Defensive copy
			return new ArrayList<>(map.values());
		}
	}

	@Override
	public boolean isAllDevicesConnected()
	{
		synchronized (map)
		{
			for(Device d : map.values())
			{
				if(!d.isConnected())
				{
					return false;
				}
			}
			return true;
		}
	}

	@Override
	public void waitForAllDevicesToConnect() throws InterruptedException
	{
		synchronized(map)
		{
			while (!isAllDevicesConnected())
			{
				map.wait();
			}
		}
	}

	@Override
	public boolean acceptDevice(long deviceId, String deviceType, int deviceVersion)
	{
		synchronized(map)
		{
			DeviceImpl dev = map.get(deviceId);
			if(dev != null)
			{
				if (dev.getDeviceType().equals(deviceType) && dev.getDeviceVersion() == deviceVersion)
				{
					if(dev.isInitialized())
					{
						return true;
					}
					return listener.acceptAndInitializeNewDevice(dev);
				}
				map.remove(dev.getDeviceId());
				listener.onDeviceRemoved(dev);
			}
			DeviceImpl newDevice = new DeviceImpl(deviceId, deviceType, deviceVersion);
			boolean accept = listener.acceptAndInitializeNewDevice(newDevice);
			if(!accept)
			{
				return false;
			}
			map.put(newDevice.getDeviceId(), newDevice);
			listener.onDeviceAdded(newDevice);
			map.notifyAll();
			return true;
		}
	}

	@Override
	public void onNewDevice(long deviceId, String deviceType, int deviceVersion)
	{
		synchronized (map)
		{
			DeviceImpl device = map.get(deviceId);
			if (device == null)
			{
				throw new RuntimeException("Device is not in the map!");
			}
			device.onCreated();
			map.notifyAll();
		}
	}

	@Override
	public void onDeviceConnected(long deviceId)
	{
		synchronized (map)
		{
			DeviceImpl device = map.get(deviceId);
			if (device == null)
			{
				throw new RuntimeException("Device is not in the map!");
			}
			device.onConnected();
			map.notifyAll();
		}
	}

	@Override
	public void onDeviceDisconnected(long deviceId)
	{
		synchronized (map)
		{
			DeviceImpl device = map.get(deviceId);
			if (device == null)
			{
				throw new RuntimeException("Device is not in the map!");
			}
			device.onDisconnected();
			map.notifyAll();
		}
	}

	@Override
	public void onDeviceLost(long deviceId)
	{
		DeviceImpl dev;
		synchronized(map)
		{
			dev = map.get(deviceId);
		}
		if(dev != null)
		{
			dev.onLost();
		}
	}

	@Override
	public int onDevicePacketReceived(long deviceId, int command, int arg1, int arg2, byte[] load)
	{
		DeviceImpl dev;
		synchronized(map)
		{
			dev = map.get(deviceId);
		}
		if(dev == null)
		{
			return 1;
		}
		return dev.onPacketReceived(command, arg1, arg2, load);
	}

	private class DeviceImpl implements Device
	{
		private final long id;
		private final String type;
		private final long version;
		private boolean isConnected;
		private DevicePacketHandler packetHandler;
		private final Set<DeviceConnectionListener> listeners;

		public DeviceImpl(long id, String type, long version)
		{
			this.id = id;
			this.type = type;
			this.version = version;
			this.isConnected = false;
			this.listeners = new HashSet<>();
		}

		@Override
		public long getDeviceId()
		{
			return id;
		}

		@Override
		public String getDeviceType()
		{
			return type;
		}

		@Override
		public long getDeviceVersion()
		{
			return version;
		}

		@Override
		public void setPacketHandler(DevicePacketHandler packetHandler)
		{
			this.packetHandler = packetHandler;
		}

		@Override
		public synchronized boolean addConnectionListener(DeviceConnectionListener listener)
		{
			return listeners.add(listener);
		}

		@Override
		public synchronized boolean removeConnectionListener(DeviceConnectionListener listener)
		{
			return listeners.remove(listener);
		}

		@Override
		public synchronized boolean isConnected()
		{
			return isConnected;
		}

		@Override
		public synchronized void waitForConnection() throws InterruptedException
		{
			while(!isConnected)
			{
				wait();
			}
		}

		@Override
		public synchronized Response sendPacket(int command, int arg1, int arg2, byte[] packet, boolean blocking, boolean forceSend) throws InterruptedException
		{
			ResponseImpl response = new ResponseImpl();
			int res = baseServer.sendPacket(id, command, arg1, arg2, packet, blocking, forceSend, response);
			if(res < 0)
			{
				return null;
			}
			return response;
		}

		@Override
		public String toString()
		{
			return "Device (" +
					"id=" + id +
					", type='" + type + '\'' +
					", version=" + version +
					')';
		}

		private synchronized void onCreated()
		{
			for(DeviceConnectionListener listener : listeners)
			{
				listener.onCreated();
			}
			//notifyAll();
		}

		private synchronized void onConnected()
		{
			isConnected = true;
			for(DeviceConnectionListener listener : listeners)
			{
				listener.onConnected();
			}
			notifyAll();
		}

		private synchronized void onDisconnected()
		{
			isConnected = false;
			for(DeviceConnectionListener listener : listeners)
			{
				listener.onDisconnected();
			}
			notifyAll();
		}

		private synchronized int onPacketReceived(int command, int arg1, int arg2, byte[] load)
		{
			if(packetHandler == null)
			{
				System.out.println("Packet received but no packethandler is set!");
				System.out.println("    Command: " + command);
				System.out.println("    Arg1: " + arg1);
				System.out.println("    Arg2: " + arg2);
				return 1;
			}
			return packetHandler.onPacket(command, arg1, arg2, load);
		}

		private synchronized void onLost()
		{
			for(DeviceConnectionListener listener : listeners)
			{
				listener.onLost();
			}
		}

		private synchronized boolean isInitialized()
		{
			return packetHandler != null;
		}
	}
}