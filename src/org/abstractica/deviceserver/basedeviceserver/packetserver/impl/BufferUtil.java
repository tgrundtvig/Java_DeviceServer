package org.abstractica.deviceserver.basedeviceserver.packetserver.impl;

public class BufferUtil
{
    public static void writeIntegerToBuffer(long data, byte[] buffer, int index, int size)
    {
        for (int i = size - 1; i >= 0; --i)
        {
            buffer[index + i] = (byte) ((data >> (i * 8)) & 0xFF);
        }
    }

    public static long readUnsignedIntegerFromBuffer(byte[] buffer, int index, int size)
    {
        long res = 0;
        for (int i = size - 1; i >= 0; --i)
        {
            res <<= 8;
            res += (buffer[index + i] & 0xFF);
        }
        return res;
    }
}
