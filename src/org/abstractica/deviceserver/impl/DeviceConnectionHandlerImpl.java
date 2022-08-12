package org.abstractica.deviceserver.impl;


import org.abstractica.javablocks.basic.Output;
import org.abstractica.deviceserver.DeviceServerListener;
import org.abstractica.deviceserver.PacketSendCallback;
import org.abstractica.deviceserver.packetserver.DevicePacketInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceConnectionHandlerImpl implements Output<DevicePacketInfo>
{
    private final Map<Long, DeviceConnectionImpl> map;
    private final DeviceServerListener listener;
    private final Output<DevicePacketInfo> packageSender;

    public DeviceConnectionHandlerImpl(Output<DevicePacketInfo> packageSender,
                                       DeviceServerListener listener)
    {
        this.packageSender = packageSender;
        this.listener = listener;
        this.map = new HashMap<>();
    }

    @Override
    public synchronized void put(DevicePacketInfo packet) throws InterruptedException
    {
        long curTime = System.currentTimeMillis();
        Long id = packet.getDeviceId();
        DeviceConnectionImpl deviceConnection = map.get(id);
        if (deviceConnection == null)
        {
            deviceConnection = new DeviceConnectionImpl(packageSender,listener,id);
            map.put(id, deviceConnection);
        }
        if(!deviceConnection.onPacket(curTime, packet))
        {
            map.remove(id);
        }
    }

    public synchronized int sendPacket(long deviceId, int command, int arg1, int arg2, byte[] load, boolean blocking, boolean forceSend, PacketSendCallback callback) throws InterruptedException
    {
        long curTime = System.currentTimeMillis();
        DeviceConnectionImpl deviceConnection = map.get(deviceId);
        if(deviceConnection == null)
        {
            return -1;
        }
        return deviceConnection.sendPacket(curTime, command, arg1, arg2, load,blocking, forceSend, callback);
    }

    public synchronized int getCurrentMsgId(long deviceId)
    {
        DeviceConnectionImpl deviceConnection = map.get(deviceId);
        if(deviceConnection == null)
        {
            return -1;
        }
        return deviceConnection.getCurrentMsgId();
    }

    public synchronized boolean readyToSendPacket(long deviceId)
    {
        DeviceConnectionImpl deviceConnection = map.get(deviceId);
        if(deviceConnection == null)
        {
            return false;
        }
        return deviceConnection.readyToSendPacket();
    }

    public synchronized long[] getAllDeviceIds()
    {
        long[] res = new long[map.size()];
        int i = 0;
        for(DeviceConnectionImpl con : map.values())
        {
            res[i++] = con.getDeviceId();
        }
        return res;
    }

    public synchronized void updateAliveness() throws InterruptedException
    {
        long curTime = System.currentTimeMillis();
        List<Long> doomedDevices = new ArrayList<>();
        for (DeviceConnectionImpl deviceConnection : map.values())
        {
            if(!deviceConnection.updateAliveness(curTime))
            {
                doomedDevices.add(deviceConnection.getDeviceId());
            }
        }
        for(Long id : doomedDevices)
        {
            map.remove(id);
        }
    }
}
