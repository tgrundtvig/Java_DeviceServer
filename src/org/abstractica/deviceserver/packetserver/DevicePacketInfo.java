package org.abstractica.deviceserver.packetserver;

import org.abstractica.deviceserver.DevicePacket;

import java.net.InetAddress;

public interface DevicePacketInfo extends DevicePacket
{
    void setAddress(InetAddress address, int port);
    InetAddress getDeviceAddress();
    int getDevicePort();
    int getMsgId();
}
