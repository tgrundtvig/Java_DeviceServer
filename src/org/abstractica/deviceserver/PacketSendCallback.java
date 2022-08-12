package org.abstractica.deviceserver;

public interface PacketSendCallback
{
    void onPacketDelivered(long deviceId, int msgId, int response);
    void onPacketCancelled(long deviceId, int msgId);
}
