package org.abstractica.deviceserver.packetserver;

import java.net.InetAddress;

public interface DevicePacketInfo extends DevicePacket
{
    void setAddress(InetAddress address, int port);
    InetAddress getDeviceAddress();
    int getDevicePort();
    int getMsgId();
}
