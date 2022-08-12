package org.abstractica.deviceserver.packetserver.impl;

import org.abstractica.javablocks.network.SocketBlock;
import org.abstractica.javablocks.network.impl.ErrorLogImpl;
import org.abstractica.deviceserver.packetserver.DeviceBlockFactory;
import org.abstractica.deviceserver.packetserver.DevicePacketInfo;

import java.net.SocketException;
import java.net.UnknownHostException;

public class RemoteDeviceBlockFactoryImpl implements DeviceBlockFactory
{
    private static DeviceBlockFactory instance = null;

    public static DeviceBlockFactory getInstance()
    {
        if(instance == null)
        {
            instance = new RemoteDeviceBlockFactoryImpl();
        }
        return instance;
    }

    private RemoteDeviceBlockFactoryImpl() {}

    @Override
    public SocketBlock<DevicePacketInfo> getRemoteDevicePacketSocket(int port, int maxPackageSize) throws SocketException, UnknownHostException
    {
        return new RemoteDevicePacketUDPSocketBlockImpl(ErrorLogImpl.getInstance(), port, maxPackageSize);
    }
}
