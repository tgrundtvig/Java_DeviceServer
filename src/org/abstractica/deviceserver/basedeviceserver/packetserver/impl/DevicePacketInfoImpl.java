package org.abstractica.deviceserver.basedeviceserver.packetserver.impl;

import org.abstractica.deviceserver.basedeviceserver.packetserver.DevicePacketInfo;

import java.net.InetAddress;

public class DevicePacketInfoImpl implements DevicePacketInfo
{
    private int port;
    private InetAddress address;
    private final long deviceId;
    private final int msgId;
    private final int command;
    private final int arg1;
    private final int arg2;

    private final byte[] load;

    public DevicePacketInfoImpl(long deviceId, int msgId, int command, int arg1, int arg2, byte[] load)
    {
        this.deviceId = deviceId;
        this.msgId = msgId;
        this.command = command;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.load = load;
    }

    @Override
    public void setAddress(InetAddress address, int port)
    {
        this.address = address;
        this.port = port;
    }

    @Override
    public InetAddress getDeviceAddress()
    {
        return address;
    }

    @Override
    public int getDevicePort()
    {
        return port;
    }

    @Override
    public long getDeviceId()
    {
        return deviceId;
    }

    @Override
    public int getMsgId()
    {
        return msgId;
    }

    @Override
    public int getCommand()
    {
        return command;
    }

    @Override
    public int getArg1()
    {
        return arg1;
    }

    @Override
    public int getArg2()
    {
        return arg2;
    }

    @Override
    public boolean hasLoad()
    {
        return load != null;
    }

    @Override
    public byte[] getLoad()
    {
        return load;
    }


    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("Device id: " + deviceId + "\n");
        buf.append("Message id: " + msgId + "\n");
        buf.append("Command: " + command + "\n");
        buf.append("Argument 1: " + arg1 + "\n");
        buf.append("Argument 2: " + arg2 + "\n");
        buf.append("Load size: " + (load == null ? 0 : load.length) + "\n");
        return buf.toString();
    }
}
