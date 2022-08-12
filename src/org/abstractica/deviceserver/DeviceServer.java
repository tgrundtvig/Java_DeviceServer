package org.abstractica.deviceserver;

import org.abstractica.javablocks.basic.ThreadControl;

public interface DeviceServer extends ThreadControl
{
    long[] getAllDeviceIds();
    boolean readyToSendPacket(long deviceId);
    int sendPacket(   long deviceId,
                      int command,
                      int arg1,
                      int arg2,
                      byte[] packet,
                      boolean blocking,
                      boolean forceSend,
                      PacketSendCallback callback   ) throws InterruptedException;
}
