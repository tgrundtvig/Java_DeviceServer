package org.abstractica.deviceserver.basedeviceserver.packetserver.impl;

import org.abstractica.javablocks.network.ErrorLog;
import org.abstractica.javablocks.network.SocketBlock;
import org.abstractica.deviceserver.basedeviceserver.packetserver.DevicePacketInfo;

import java.io.IOException;
import java.net.*;

public class DevicePacketUDPSocketBlockImpl implements SocketBlock<DevicePacketInfo>
{
    private final ErrorLog err;
    private final DatagramSocket socket;
    private final InetAddress address;
    private final DatagramPacket receivePacket;
    private final DatagramPacket sendPacket;

    public DevicePacketUDPSocketBlockImpl(ErrorLog err, int port, int maxPacketSize) throws SocketException, UnknownHostException
    {
        this.err = err;
        this.socket = new DatagramSocket(port);
        this.address = InetAddress.getLocalHost();
        receivePacket = new DatagramPacket(new byte[maxPacketSize], maxPacketSize);
        sendPacket = new DatagramPacket(new byte[maxPacketSize], maxPacketSize);
    }

    @Override
    public int getPort()
    {
        return socket.getPort();
    }

    @Override
    public InetAddress getAddress()
    {
        return address;
    }

    @Override
    public DevicePacketInfo get() throws InterruptedException
    {
        while(true)
        {
            try
            {
                socket.setSoTimeout(500);
                socket.receive(receivePacket);
                return toRemoteDevicePacket(receivePacket);
            }
            catch(SocketTimeoutException e)
            {
                Thread.sleep(1);
            }
            catch (IOException e)
            {
                err.reportIOException(e);
                Thread.sleep(5000);
            }
        }
    }

    @Override
    public void put(DevicePacketInfo packet) throws InterruptedException
    {
        while(true)
        {
            try
            {
                fromRemoteDevicePacket(packet, sendPacket);
                if(sendPacket.getData() == null)
                {
                    throw new RuntimeException("Data is null");
                }
                if(sendPacket.getAddress() == null)
                {
                    throw new RuntimeException("Address is null");
                }
                socket.send(sendPacket);
                return;
            } catch (IOException e)
            {
                err.reportIOException(e);
                Thread.sleep(5000);
            }
        }
    }

    private DevicePacketInfo toRemoteDevicePacket(DatagramPacket datagramPacket)
    {
        InetAddress address = datagramPacket.getAddress();
        int port = datagramPacket.getPort();
        int offset = datagramPacket.getOffset();
        int length = datagramPacket.getLength();
        byte[] data = datagramPacket.getData();


        //Data format:
        // deviceID:
        long deviceId = BufferUtil.readUnsignedIntegerFromBuffer(data, 0, 8);
        int msgId = (int) BufferUtil.readUnsignedIntegerFromBuffer(data, 8, 2);
        int command = (int) BufferUtil.readUnsignedIntegerFromBuffer(data, 10, 2);
        int arg1 = (int) BufferUtil.readUnsignedIntegerFromBuffer(data, 12, 2);
        int arg2 = (int) BufferUtil.readUnsignedIntegerFromBuffer(data, 14, 2);

        byte[] load = null;
        if(length > 16)
        {
            load = new byte[length - 16];
            for (int i = 16; i < length; ++i)
            {
                load[i - 16] = data[i + offset];
            }
        }
        DevicePacketInfoImpl res = new DevicePacketInfoImpl(deviceId, msgId, command, arg1, arg2, load);
        res.setAddress(address, port);
        return res;
    }

    private void fromRemoteDevicePacket(DevicePacketInfo remoteDevicePacketInfo, DatagramPacket datagramPacket)
    {
        int size = 16;
        byte[] load = null;
        if(remoteDevicePacketInfo.hasLoad())
        {
            load = remoteDevicePacketInfo.getLoad();
            size += load.length;
        }
        byte[] data = new byte[size];
        BufferUtil.writeIntegerToBuffer(remoteDevicePacketInfo.getDeviceId(), data, 0, 8);
        BufferUtil.writeIntegerToBuffer(remoteDevicePacketInfo.getMsgId(), data, 8, 2);
        BufferUtil.writeIntegerToBuffer(remoteDevicePacketInfo.getCommand(), data, 10, 2);
        BufferUtil.writeIntegerToBuffer(remoteDevicePacketInfo.getArg1(), data, 12, 2);
        BufferUtil.writeIntegerToBuffer(remoteDevicePacketInfo.getArg2(), data, 14, 2);
        if(load != null)
        {
            for (int i = 0; i < load.length; ++i)
            {
                data[16 + i] = load[i];
            }
        }
        byte[] dataBuf = sendPacket.getData();
        if(dataBuf.length < data.length)
        {
            throw new IllegalArgumentException("Packet larger than maxPacketSize!");
        }
        for(int i = 0; i < data.length; ++i)
        {
            dataBuf[i] = data[i];
        }
        sendPacket.setLength(data.length);
        sendPacket.setAddress(remoteDevicePacketInfo.getDeviceAddress());
        sendPacket.setPort(remoteDevicePacketInfo.getDevicePort());
    }

}
