package org.abstractica.deviceserver.packetserver;

import org.abstractica.javablocks.network.SocketBlock;

import java.net.SocketException;
import java.net.UnknownHostException;


public interface DeviceBlockFactory
{
    public SocketBlock<DevicePacketInfo> getRemoteDevicePacketSocket(int port, int maxPacketSize) throws SocketException, UnknownHostException;
}
