package org.abstractica.deviceserver.basedeviceserver.impl;

import org.abstractica.javablocks.basic.BasicBlockFactory;
import org.abstractica.javablocks.basic.ThreadBlock;
import org.abstractica.javablocks.basic.ThreadControl;
import org.abstractica.javablocks.basic.impl.BasicBlockFactoryImpl;
import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServerPacketSendCallback;
import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServer;
import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServerListener;
import org.abstractica.deviceserver.basedeviceserver.packetserver.DevicePacketInfo;
import org.abstractica.deviceserver.basedeviceserver.packetserver.DevicePacketServer;
import org.abstractica.deviceserver.basedeviceserver.packetserver.impl.DevicePacketServerImpl;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class BaseDeviceServerImpl implements BaseDeviceServer, Runnable
{
    private final DevicePacketServer packetServer;
    private final BaseDeviceConnectionHandlerImpl connectionHandler;
    private final ThreadControl threadControl;
    private final long updateInterval;
    private final int port;
    private final InetAddress address;
    //private RemoteDeviceServerListener listener;
    private Thread alivenessThread;
    private volatile boolean running;

    public BaseDeviceServerImpl(int port,
                                int maxPacketSize,
                                int bufferSize,
                                long updateInterval,
                                BaseDeviceServerListener listener) throws SocketException, UnknownHostException
    {
        this.packetServer = new DevicePacketServerImpl(port, maxPacketSize, bufferSize);
        this.connectionHandler = new BaseDeviceConnectionHandlerImpl(packetServer, listener);
        BasicBlockFactory basicFactory = BasicBlockFactoryImpl.getInstance();
        ThreadBlock<DevicePacketInfo> threadBlock = basicFactory.getThreadBlock();
        threadBlock.setInput(packetServer);
        threadBlock.setOutput(connectionHandler);
        threadControl = threadBlock;
        this.updateInterval = updateInterval;
        this.address = InetAddress.getLocalHost();
        this.port = port;
        this.running = false;
    }

    @Override
    public synchronized boolean readyToSendPacket(long deviceId)
    {
        return connectionHandler.readyToSendPacket(deviceId);
    }

    @Override
    public synchronized int sendPacket(long deviceId,
                                       int command,
                                       int arg1,
                                       int arg2,
                                       byte[] load,
                                       boolean blocking,
                                       boolean forceSend,
                                       BaseDeviceServerPacketSendCallback callback) throws InterruptedException
    {
        return connectionHandler.sendPacket(deviceId, command, arg1, arg2, load, blocking, forceSend, callback);
    }

    @Override
    public void removeDevice(long deviceId)
    {
        connectionHandler.removeDevice(deviceId);
    }

    @Override
    public synchronized long[] getAllDeviceIds()
    {
        return connectionHandler.getAllDeviceIds();
    }

    @Override
    public synchronized void start()
    {
        if (!running)
        {
            running = true;
            threadControl.start();
            packetServer.start();
            alivenessThread = new Thread(this);
            alivenessThread.start();
            System.out.println(this.getClass().getSimpleName() + " -> Remote device server started on: " + address + ":" + port);
        }
    }

    @Override
    public void stopGracefully() throws InterruptedException
    {
        if (running)
        {
            running = false;
            synchronized(this)
            {
                alivenessThread.interrupt();
                packetServer.stopGracefully();
                threadControl.stopGracefully();
            }
            alivenessThread.join();
            System.out.println("Remote device server stopped gracefully.");
        }
    }

    @Override
    public void stopNow() throws InterruptedException
    {
        if (running)
        {
            running = false;
            synchronized(this)
            {
                alivenessThread.interrupt();
                packetServer.stopNow();
                threadControl.stopNow();
            }
            alivenessThread.join();
            System.out.println("Remote device server stopped.");
        }
    }

    @Override
    public synchronized boolean isRunning()
    {
        return running;
    }

    @Override
    public synchronized void run()
    {
        while (running)
        {
            try
            {
                connectionHandler.updateAliveness();
                wait(updateInterval);
            } catch (InterruptedException e)
            {

            }
        }
    }
}
