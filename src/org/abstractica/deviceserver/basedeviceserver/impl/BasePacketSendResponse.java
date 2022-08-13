package org.abstractica.deviceserver.basedeviceserver.impl;

import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServerPacketSendCallback;

public class BasePacketSendResponse implements BaseDeviceServerPacketSendCallback
{
    private int response;

    public BasePacketSendResponse()
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

    public synchronized int getResponse() throws InterruptedException
    {
        while(response == -2)
        {
            wait();
        }
        return response;
    }
}
