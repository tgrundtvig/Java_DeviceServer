package org.abstractica.deviceserver.impl;

import org.abstractica.deviceserver.PacketSendCallback;

public class PacketSendResponse implements PacketSendCallback
{
    private int response;

    public PacketSendResponse()
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
