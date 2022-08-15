package org.abstractica.deviceserver.basedeviceserver.impl;

import org.abstractica.javablocks.basic.Output;
import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServerListener;
import org.abstractica.deviceserver.basedeviceserver.BaseDeviceServerPacketSendCallback;
import org.abstractica.deviceserver.basedeviceserver.packetserver.DevicePacketInfo;
import org.abstractica.deviceserver.basedeviceserver.packetserver.impl.DevicePacketInfoImpl;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class BaseDeviceConnectionImpl
{
    private static final int MAX_IDLE_TIME = 1000;
    private static final int CONNECTED_RESEND_INTERVAL = 1000;
    private static final int DISCONNECTED_RESEND_INTERVAL = 10000;
    private static final int CONNECTED_RESEND_COUNT = 5;
    private static final int DISCONNECTED_RESEND_COUNT = 60;

    //Protocol
    static final int INIT = 65535;
    static final int INITACK = 65534;
    static final int MSGACK = 65533;
    static final int PING = 65532;

    private final Output<DevicePacketInfo> packetSender;
    private final BaseDeviceServerListener deviceListener;
    private BaseDeviceServerPacketSendCallback callback;
    private final long deviceId;
    private String deviceType;
    private int deviceVersion;

    private InetAddress deviceAddress;
    private int devicePort;
    private DevicePacketInfo packetToSend;
    private boolean isBlocking;
    private boolean isConnected;
    private long lastPacketReceived;
    private long lastPacketSentTime;
    private int packetSentCount;
    private int curMsgId;
    private int lastReceivedMsgId;

    public BaseDeviceConnectionImpl(Output<DevicePacketInfo> packetSender,
                                    BaseDeviceServerListener deviceListener,
                                    long deviceId)
    {
        this.packetSender = packetSender;
        this.deviceListener = deviceListener;
        this.deviceId = deviceId;
        this.curMsgId = 0;
        this.deviceType = null;
        this.deviceVersion = -1;
        isConnected = false;
    }

    public synchronized long getDeviceId()
    {
        return deviceId;
    }

    public synchronized boolean updateAliveness(long curTime) throws InterruptedException
    {
        if (packetToSend != null)
        {
            long timeSinceLastSent = curTime - lastPacketSentTime;
            long sendInterval = isConnected ? CONNECTED_RESEND_INTERVAL : DISCONNECTED_RESEND_INTERVAL;
            long maxSendCount = isConnected ? CONNECTED_RESEND_COUNT : DISCONNECTED_RESEND_COUNT;
            if (timeSinceLastSent >= sendInterval)
            {
                if (packetSentCount >= maxSendCount)
                {
                    if (isConnected)
                    {
                        isConnected = false;
                        deviceListener.onDeviceDisconnected(deviceId);
                        packetSentCount = 0;
                    } else
                    {
                        deviceListener.onDeviceLost(deviceId);
                        return false;
                    }
                } else
                {
                    //System.out.println("Resending packet.");
                    doSendPacket(curTime);
                }
            }
        } else
        {
            //No packet to send
            long timeSinceLastReceive = curTime - lastPacketReceived;
            if (timeSinceLastReceive > MAX_IDLE_TIME)
            {
                //Send ping
                //System.out.println("Sending PING to " + deviceId);
                sendPacket(curTime, PING, 0, 0, null, false, false, null);
            }
        }
        return true;
    }

    public synchronized int sendPacket(long curTime, int command, int arg1, int arg2, byte[] load, boolean blocking, boolean forceSend, BaseDeviceServerPacketSendCallback callback) throws InterruptedException
    {
        if (packetToSend != null)
        {
            if (isBlocking && !forceSend)
            {
                //Current packet has priority
                return -1;
            }
            if (this.callback != null)
            {
                //The current packet is not a PING, so it needs to be reported as cancelled!
                this.callback.onPacketCancelled(deviceId, curMsgId);
                this.callback = null;
            }
        }
        ++curMsgId;
        if (curMsgId > 65535)
        {
            curMsgId = 1;
        }
        packetSentCount = 0;
        isBlocking = blocking;
        packetToSend = new DevicePacketInfoImpl(deviceId, curMsgId, command, arg1, arg2, load);
        packetToSend.setAddress(deviceAddress, devicePort);
        this.callback = callback;
        doSendPacket(curTime);
        //System.out.println("Packet sent to: " + deviceId + ", msgId: " + curMsgId + ", cmd: " + command + ", arg1: " + arg1 + ", arg2: " + arg2);
        return curMsgId;
    }

    public synchronized boolean onPacket(long curTime, DevicePacketInfo packet) throws InterruptedException
    {
        //System.out.println("Packet received. Command: " + packet.getCommand());
        if (packet.getDeviceId() != deviceId)
        {
            throw new IllegalArgumentException("Packet not for this device!");
        }
        this.lastPacketReceived = curTime;
        this.deviceAddress = packet.getDeviceAddress();
        this.devicePort = packet.getDevicePort();
        if (!initialized() && packet.getCommand() != INIT && packet.getCommand() != INITACK)
        {
            sendPacket(curTime, INIT, 0, 0, null, true, true, null);
            return true;
        }
        switch (packet.getCommand())
        {
            case INIT:
            case INITACK:
                return onInitPacket(packet);
            case MSGACK:
                onAcknowledgePacket(packet);
                return true;
            default:
                onDefaultPacket(packet);
                return true;
        }
    }

    private synchronized void onDefaultPacket(DevicePacketInfo packet) throws InterruptedException
    {
        //sendAcknowledgePacket(packet, ReservedCommands.MSGACK);
        int msgDist = packet.getMsgId() - lastReceivedMsgId;
        if (msgDist < 0) msgDist += 65536;
        if (msgDist > 0 && msgDist < 30000)
        {
            if (!isConnected)
            {
                isConnected = true;
                deviceListener.onDeviceConnected(deviceId);
            }
            //System.out.println("Last msgId: " + lastReceivedMsgId + " This msgId: " + packet.getMsgId());
            lastReceivedMsgId = packet.getMsgId();
            if (packet.getCommand() != PING)
            {
                int response = deviceListener.onDevicePacketReceived(deviceId, packet.getCommand(), packet.getArg1(), packet.getArg2(), packet.getLoad());
                sendAcknowledgePacket(packet, MSGACK, response);
                return;
            }
        } else
        {
            /*
            System.out.println("Redundant message discarded: (msgId: " + packet.getMsgId() +
                    " lastMsgId: " + lastReceivedMsgId +
                    " dist: " + msgDist + ")");

             */
        }
        sendAcknowledgePacket(packet, MSGACK, 0);
    }

    private synchronized boolean onInitPacket(DevicePacketInfo packet) throws InterruptedException
    {
        //ResetSession
        lastReceivedMsgId = 0;
        curMsgId = 0;
        //Clear send buffer
        if(callback != null)
        {
            int msgId = 0;
            if(packetToSend != null)
            {
                msgId = packetToSend.getMsgId();
            }
            callback.onPacketCancelled(deviceId, msgId);
            callback = null;
        }

        packetToSend = null;
        packetSentCount = 0;
        if(!packet.hasLoad())
        {
            return false;
        }
        String packetDeviceType = new String(packet.getLoad(), StandardCharsets.US_ASCII);
        if(!deviceListener.acceptDevice(deviceId, packetDeviceType, packet.getArg1()))
        {
            if (isConnected)
            {
                isConnected = false;
                deviceListener.onDeviceDisconnected(deviceId);
            }
            if (initialized())
            {
                deviceListener.onDeviceLost(deviceId);
            }
            return false;
        }
        //Device acceptet
        if (packet.getCommand() == INIT)
        {
            sendAcknowledgePacket(packet, INITACK, 0);
        }
        if (!packetDeviceType.equals(deviceType) || packet.getArg1() != deviceVersion)
        {
            if (isConnected)
            {
                isConnected = false;
                deviceListener.onDeviceDisconnected(deviceId);
            }
            if (initialized())
            {
                deviceListener.onDeviceLost(deviceId);
            }
            deviceType = packetDeviceType;
            deviceVersion = packet.getArg1();
            deviceListener.onNewDevice(deviceId, deviceType, deviceVersion);
        }
        if (!isConnected)
        {
            isConnected = true;
            deviceListener.onDeviceConnected(deviceId);
        }
        return true;
    }

    private synchronized void onAcknowledgePacket(DevicePacketInfo packet)
    {
        if (packetToSend != null)
        {
            if (packet.getMsgId() == packetToSend.getMsgId())
            {
                packetToSend = null;
                packetSentCount = 0;
                if(callback != null)
                {
                    callback.onPacketDelivered(deviceId, packet.getMsgId(), packet.getArg1());
                    callback = null;
                }
            }
        }
    }

    private synchronized void sendAcknowledgePacket(DevicePacketInfo packet, int command, int response) throws InterruptedException
    {
        DevicePacketInfo ack = new DevicePacketInfoImpl(deviceId, packet.getMsgId(), command, response, 0, null);
        ack.setAddress(deviceAddress, devicePort);
        packetSender.put(ack);
    }

    private synchronized void doSendPacket(long curTime) throws InterruptedException
    {
        if (packetToSend != null)
        {
            packetSender.put(packetToSend);
            lastPacketSentTime = curTime;
            ++packetSentCount;
        }
    }

    private synchronized boolean initialized()
    {
        return deviceType != null && deviceVersion >= 0;
    }

    public synchronized boolean readyToSendPacket()
    {
        return initialized() && (packetToSend == null || !isBlocking);
    }

    public synchronized int getCurrentMsgId()
    {
        if(packetToSend == null)
        {
            return 0;
        }
        return packetToSend.getMsgId();
    }

    public synchronized void removeDevice()
    {
        if(isConnected)
        {
            deviceListener.onDeviceDisconnected(deviceId);
        }
        if (initialized())
        {
            deviceListener.onDeviceLost(deviceId);
        }
    }
}
