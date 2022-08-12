package org.abstractica.deviceserver;

public interface SendInfo
{
    public boolean isBlocking();
    public boolean forceSend();
    public DevicePacket getPacket();
    public PacketSendCallback getCallback();
}
