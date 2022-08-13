package org.abstractica.deviceserver;

import java.util.Collection;

public interface DeviceServer
{
    Device createDevice(long deviceId, String deviceType, long deviceVersion);
    void start();
    void stop() throws InterruptedException;
    boolean addDevice(Device device);
    boolean removeDevice(Device device);
    Collection<Device> getAllDevices();
    boolean isAllDevicesConnected();
    void waitForAllDevicesToConnect() throws InterruptedException;
}
