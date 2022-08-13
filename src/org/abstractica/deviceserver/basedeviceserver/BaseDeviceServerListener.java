package org.abstractica.deviceserver.basedeviceserver;

public interface BaseDeviceServerListener
{
    boolean acceptDevice(long deviceId, String deviceType, int deviceVersion);
    void onNewDevice(long deviceId, String deviceType, int deviceVersion);
    void onDeviceConnected(long deviceId);
    void onDeviceDisconnected(long deviceId);
    void onDeviceLost(long deviceId);
    int onDevicePacketReceived(long deviceId, int command, int arg1, int arg2, byte[] load);
}
