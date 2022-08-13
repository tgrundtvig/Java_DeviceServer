package org.abstractica.deviceserver.basedeviceserver.impl;


import org.abstractica.javablocks.basic.Output;
import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServerListener;
import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServerPacketSendCallback;
import org.abstractica.deviceserver.basedeviceserver.packetserver.DevicePacketInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseDeviceConnectionHandlerImpl implements Output<DevicePacketInfo>
{
    private final Map<Long, BaseDeviceConnectionImpl> map;
    private final BaseDeviceServerListener listener;
    private final Output<DevicePacketInfo> packageSender;

    public BaseDeviceConnectionHandlerImpl(Output<DevicePacketInfo> packageSender,
                                           BaseDeviceServerListener listener)
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
        BaseDeviceConnectionImpl deviceConnection = map.get(id);
        if (deviceConnection == null)
        {
            deviceConnection = new BaseDeviceConnectionImpl(packageSender,listener,id);
            if(deviceConnection.onPacket(curTime, packet))
            {
                map.put(id, deviceConnection);
            }
        }
        else if(!deviceConnection.onPacket(curTime, packet))
        {
            map.remove(id);
        }
    }

    public synchronized int sendPacket(long deviceId, int command, int arg1, int arg2, byte[] load, boolean blocking, boolean forceSend, BaseDeviceServerPacketSendCallback callback) throws InterruptedException
    {
        long curTime = System.currentTimeMillis();
        BaseDeviceConnectionImpl deviceConnection = map.get(deviceId);
        if(deviceConnection == null)
        {
            return -1;
        }
        return deviceConnection.sendPacket(curTime, command, arg1, arg2, load,blocking, forceSend, callback);
    }

    public synchronized int getCurrentMsgId(long deviceId)
    {
        BaseDeviceConnectionImpl deviceConnection = map.get(deviceId);
        if(deviceConnection == null)
        {
            return -1;
        }
        return deviceConnection.getCurrentMsgId();
    }

    public synchronized boolean readyToSendPacket(long deviceId)
    {
        BaseDeviceConnectionImpl deviceConnection = map.get(deviceId);
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
        for(BaseDeviceConnectionImpl con : map.values())
        {
            res[i++] = con.getDeviceId();
        }
        return res;
    }

    public synchronized void updateAliveness() throws InterruptedException
    {
        long curTime = System.currentTimeMillis();
        List<Long> doomedDevices = new ArrayList<>();
        for (BaseDeviceConnectionImpl deviceConnection : map.values())
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

	public synchronized void removeDevice(long deviceId)
	{
        BaseDeviceConnectionImpl con = map.get(deviceId);
        if(con != null)
        {
            map.remove(deviceId);
            con.removeDevice();
        }
	}
}
