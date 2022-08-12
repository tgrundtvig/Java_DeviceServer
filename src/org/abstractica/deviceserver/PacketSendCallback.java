package org.abstractica.deviceserver;

public interface PacketSendCallback
{
    public void onPacketDelivered(long deviceId, int msgId, int response);
    public void onPacketCancelled(long deviceId, int msgId);
}
