package org.abstractica.deviceserver.basedeviceserver;

public interface BaseDeviceServerPacketSendCallback
{
    void onPacketDelivered(long deviceId, int msgId, int response);
    void onPacketCancelled(long deviceId, int msgId);
}
