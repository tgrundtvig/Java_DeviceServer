package org.abstractica.deviceserver;

import org.abstractica.javablocks.basic.Output;

public interface PacketToListener extends Output<DevicePacket>
{
    void setListener(DeviceServerListener listener);
}
