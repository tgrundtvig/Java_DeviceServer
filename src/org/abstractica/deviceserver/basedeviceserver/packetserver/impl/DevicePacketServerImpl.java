package org.abstractica.deviceserver.basedeviceserver.packetserver.impl;


import org.abstractica.javablocks.basic.*;
import org.abstractica.javablocks.basic.impl.BasicBlockFactoryImpl;
import org.abstractica.javablocks.network.SocketBlock;
import org.abstractica.deviceserver.basedeviceserver.packetserver.DeviceBlockFactory;
import org.abstractica.deviceserver.basedeviceserver.packetserver.DevicePacketInfo;
import org.abstractica.deviceserver.basedeviceserver.packetserver.DevicePacketServer;

import java.net.SocketException;
import java.net.UnknownHostException;

public class DevicePacketServerImpl implements DevicePacketServer
{
    private Output<DevicePacketInfo> sendConnection;
    private Input<DevicePacketInfo> receiveConnection;
    private ThreadControl receiveCtrl;
    private ThreadControl sendCtrl;

    public DevicePacketServerImpl(int port, int maxPacketSize, int bufferSize) throws SocketException, UnknownHostException
    {
        BasicBlockFactory basicFactory = BasicBlockFactoryImpl.getInstance();
        DeviceBlockFactory remoteDeviceFactory = DeviceBlockFactoryImpl.getInstance();
        SocketBlock<DevicePacketInfo> socket = remoteDeviceFactory.getDevicePacketSocket(port, maxPacketSize);
        ThreadBlock<DevicePacketInfo> receiveThread = basicFactory.getThreadBlock();
        BufferBlock<DevicePacketInfo> receiveBuffer = basicFactory.getBufferBlock(bufferSize);
        ThreadBlock<DevicePacketInfo> sendThread = basicFactory.getThreadBlock();
        BufferBlock<DevicePacketInfo> sendBuffer = basicFactory.getBufferBlock(bufferSize);

        //Hookup
        receiveThread.setInput(socket);
        receiveThread.setOutput(receiveBuffer);
        receiveConnection = receiveBuffer;

        sendConnection = sendBuffer;
        sendThread.setInput(sendBuffer);
        sendThread.setOutput(socket);

        receiveCtrl = receiveThread;
        sendCtrl = sendThread;
    }

    @Override
    public void start()
    {
        sendCtrl.start();
        receiveCtrl.start();
    }

    @Override
    public void stopGracefully() throws InterruptedException
    {
        receiveCtrl.stopGracefully();
        sendCtrl.stopGracefully();
    }

    @Override
    public void stopNow() throws InterruptedException
    {
        receiveCtrl.stopNow();
        sendCtrl.stopNow();
    }

    @Override
    public boolean isRunning()
    {
        return sendCtrl.isRunning();
    }

    @Override
    public DevicePacketInfo get() throws InterruptedException
    {
        return receiveConnection.get();
    }

    @Override
    public void put(DevicePacketInfo packet) throws InterruptedException
    {
        sendConnection.put(packet);
    }
}
