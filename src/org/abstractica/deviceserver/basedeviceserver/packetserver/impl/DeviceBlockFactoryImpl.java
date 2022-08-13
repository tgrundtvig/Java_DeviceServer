package org.abstractica.deviceserver.basedeviceserver.packetserver.impl;

import org.abstractica.javablocks.network.SocketBlock;
import org.abstractica.javablocks.network.impl.ErrorLogImpl;
import org.abstractica.deviceserver.basedeviceserver.packetserver.DeviceBlockFactory;
import org.abstractica.deviceserver.basedeviceserver.packetserver.DevicePacketInfo;

import java.net.SocketException;
import java.net.UnknownHostException;

public class DeviceBlockFactoryImpl implements DeviceBlockFactory
{
    private static DeviceBlockFactory instance = null;

    public static DeviceBlockFactory getInstance()
    {
        if(instance == null)
        {
            instance = new DeviceBlockFactoryImpl();
        }
        return instance;
    }

    private DeviceBlockFactoryImpl() {}

    @Override
    public SocketBlock<DevicePacketInfo> getDevicePacketSocket(int port, int maxPackageSize) throws SocketException, UnknownHostException
    {
        return new DevicePacketUDPSocketBlockImpl(ErrorLogImpl.getInstance(), port, maxPackageSize);
    }
}
