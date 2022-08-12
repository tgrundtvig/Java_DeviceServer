package org.abstractica.deviceserver;

public interface ReservedCommands
{
    //Special messages
    static final int INIT = 65535;
    static final int INITACK = 65534;
    static final int MSGACK = 65533;
    static final int PING = 65532;

    //Event serialization
    static final int ON_NEW_DEVICE = 65520;
    static final int ON_DEVICE_CONNECTED = 65519;
    static final int ON_DEVICE_DISCONNECTED = 65518;
    static final int ON_DEVICE_LOST = 65517;
    static final int ON_DEVICE_PACKET_DELIVERED = 65516;
    static final int ON_DEVICE_PACKET_CANCELLED = 65515;
}
