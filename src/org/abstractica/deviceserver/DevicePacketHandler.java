package org.abstractica.deviceserver;

public interface DevicePacketHandler
{
	int onPacket(int command, int arg1, int arg2, byte[] load);
}
