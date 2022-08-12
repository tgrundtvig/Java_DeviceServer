package org.abstractica.deviceserver;

public interface PacketSender
{
    int sendPacket(   DevicePacket packet,
                      boolean blocking,
                      boolean forceSend,
                      PacketSendCallback callback   ) throws InterruptedException;
}
