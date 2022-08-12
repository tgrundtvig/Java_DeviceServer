package org.abstractica.deviceserver;


import org.abstractica.javablocks.basic.Output;

public interface ListenerToPacket extends DeviceServerListener
{
    public void setOutput(Output<DevicePacket> packetOutput);
}
