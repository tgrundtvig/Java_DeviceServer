package org.abstractica.deviceserver;

public interface Response
{
	boolean isReady();
	int getResponse() throws InterruptedException;
}
