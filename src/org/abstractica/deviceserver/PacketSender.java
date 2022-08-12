package org.abstractica.deviceserver;

public interface PacketSender
{
    public int sendPacket(DevicePacket packet,
                          boolean blocking,
                          boolean forceSend,
                          PacketSendCallback callback) throws InterruptedException;
}
