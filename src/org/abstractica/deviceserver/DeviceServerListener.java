package org.abstractica.deviceserver;

public interface DeviceServerListener
{
    public boolean acceptDevice(long deviceId, String deviceType, int deviceVersion);
    public void onNewDevice(long deviceId, String deviceType, int deviceVersion);
    public void onDeviceConnected(long deviceId);
    public void onDeviceDisconnected(long deviceId);
    public void onDeviceLost(long deviceId);
    public int onDevicePacketReceived(long deviceId, int command, int arg1, int arg2, byte[] load);
}
