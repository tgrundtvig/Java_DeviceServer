package org.abstractica.deviceserver.impl;

import org.abstractica.deviceserver.Response;
import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServerPacketSendCallback;

public class ResponseImpl implements Response, BaseDeviceServerPacketSendCallback
{
	private int response;

	public ResponseImpl()
	{
		this.response = -2;
	}

	@Override
	public synchronized void onPacketDelivered(long deviceId, int msgId, int response)
	{
		this.response = response;
		notifyAll();
	}

	@Override
	public synchronized void onPacketCancelled(long deviceId, int msgId)
	{
		this.response = -1;
		notifyAll();
	}

	@Override
	public synchronized boolean isReady()
	{
		return response != -2;
	}

	public synchronized int getResponse() throws InterruptedException
	{
		while (response == -2)
		{
			wait();
		}
		return response;
	}
}

