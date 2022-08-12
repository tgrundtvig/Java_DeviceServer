package org.abstractica.deviceserver.packetserver;

import org.abstractica.deviceserver.DevicePacket;

import java.net.InetAddress;

public interface DevicePacketInfo extends DevicePacket
{
    public void setAddress(InetAddress address, int port);
    public InetAddress getDeviceAddress();
    public int getDevicePort();
    public int getMsgId();
}
