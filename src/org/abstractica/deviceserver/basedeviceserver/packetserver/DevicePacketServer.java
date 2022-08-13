package org.abstractica.deviceserver.basedeviceserver.packetserver;

import org.abstractica.javablocks.basic.Input;
import org.abstractica.javablocks.basic.Output;
import org.abstractica.javablocks.basic.ThreadControl;

public interface DevicePacketServer extends Output<DevicePacketInfo>, Input<DevicePacketInfo>, ThreadControl
{
}
