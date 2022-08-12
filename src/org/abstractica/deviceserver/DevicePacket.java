package org.abstractica.deviceserver;

public interface DevicePacket
{
    public long getDeviceId();
    public int getCommand();
    public int getArg1();
    public int getArg2();
    public boolean hasLoad();
    public byte[] getLoad();
}
