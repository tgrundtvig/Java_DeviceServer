package org.abstractica.deviceserver.packetserver;

public interface DevicePacket
{
    long getDeviceId();
    int getCommand();
    int getArg1();
    int getArg2();
    boolean hasLoad();
    byte[] getLoad();
}
