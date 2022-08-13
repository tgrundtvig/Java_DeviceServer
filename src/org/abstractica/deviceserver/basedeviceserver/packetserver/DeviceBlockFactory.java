package org.abstractica.deviceserver.basedeviceserver.packetserver;

import org.abstractica.javablocks.network.SocketBlock;

import java.net.SocketException;
import java.net.UnknownHostException;


public interface DeviceBlockFactory
{
    SocketBlock<DevicePacketInfo> getDevicePacketSocket(int port, int maxPacketSize) throws SocketException, UnknownHostException;
}
