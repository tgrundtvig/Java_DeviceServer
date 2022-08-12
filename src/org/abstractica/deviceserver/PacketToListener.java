package org.abstractica.deviceserver;

import org.abstractica.javablocks.basic.Output;

public interface PacketToListener extends Output<DevicePacket>
{
    public void setListener(DeviceServerListener listener);
}
