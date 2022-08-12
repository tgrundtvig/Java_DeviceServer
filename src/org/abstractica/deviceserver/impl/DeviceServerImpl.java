package org.abstractica.deviceserver.impl;

import org.abstractica.javablocks.basic.BasicBlockFactory;
import org.abstractica.javablocks.basic.ThreadBlock;
import org.abstractica.javablocks.basic.ThreadControl;
import org.abstractica.javablocks.basic.impl.BasicBlockFactoryImpl;
import org.abstractica.deviceserver.PacketSendCallback;
import org.abstractica.deviceserver.DeviceServer;
import org.abstractica.deviceserver.DeviceServerListener;
import org.abstractica.deviceserver.packetserver.DevicePacketInfo;
import org.abstractica.deviceserver.packetserver.DevicePacketServer;
import org.abstractica.deviceserver.packetserver.impl.RemoteDevicePacketServerImpl;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class DeviceServerImpl implements DeviceServer, Runnable
{
    private final DevicePacketServer packetServer;
    private final DeviceConnectionHandlerImpl connectionHandler;
    private final ThreadControl threadControl;
    private final long updateInterval;
    private final int port;
    private final InetAddress address;
    //private RemoteDeviceServerListener listener;
    private Thread alivenessThread;
    private volatile boolean running;

    public DeviceServerImpl(int port,
                            int maxPacketSize,
                            int bufferSize,
                            long updateInterval,
                            DeviceServerListener listener) throws SocketException, UnknownHostException
    {
        this.packetServer = new RemoteDevicePacketServerImpl(port, maxPacketSize, bufferSize);
        this.connectionHandler = new DeviceConnectionHandlerImpl(packetServer, listener);
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
                                       PacketSendCallback callback) throws InterruptedException
    {
        return connectionHandler.sendPacket(deviceId, command, arg1, arg2, load, blocking, forceSend, callback);
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
