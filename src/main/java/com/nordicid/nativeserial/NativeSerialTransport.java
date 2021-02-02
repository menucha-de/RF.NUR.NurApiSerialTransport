/* 
  Copyright (c) 2016- Nordic ID 
  NORDIC ID SOFTWARE DISCLAIMER

  You are about to use Nordic ID Demo Software ("Software"). 
  It is explicitly stated that Nordic ID does not give any kind of warranties, 
  expressed or implied, for this Software. Software is provided "as is" and with 
  all faults. Under no circumstances is Nordic ID liable for any direct, special, 
  incidental or indirect damages or for any economic consequential damages to you 
  or to any third party.

  The use of this software indicates your complete and unconditional understanding 
  of the terms of this disclaimer. 
  
  IF YOU DO NOT AGREE OF THE TERMS OF THIS DISCLAIMER, DO NOT USE THE SOFTWARE.  
*/
/*
 * Nordic ID's implementation of the native serial transport used by NUR modules.
 */
package com.nordicid.nativeserial;

import java.io.IOException;

import com.nordicid.nurapi.NurApiException;
import com.nordicid.nurapi.NurApiTransport;

/**
 * Class that implements a native interface to a serial port.
 * 
 * @author Nordic ID
 * @version 1.8.0
 */
public class NativeSerialTransport implements NurApiTransport
{
	/*
	 * Native part of this transport's implementation.
	 */
	private static native String[] enumeratePortsNative() throws NurApiException;
	private static native SerialPort[] enumaratePortsNativeEx() throws NurApiException;
	
	private native long connectNative(String portName, int baudrate)  throws NurApiException;
	private native void disconnectNative(long hTransport);
	
	private native int getBaudrateNative(long hTransport) throws NurApiException;
	private native boolean setBaudrateNative(long hTransport, int baudrate)  throws NurApiException;
	private native int readDataNative(long hTransport, byte[] buffer, int bufferLen) throws IOException;	
	private native int writeDataNative(long hTransport, byte[] buffer, int offset, int bufferLen) throws IOException;
	private native boolean isConnectedNative(long hTransport);
		
	/*
	 * - NativeSerialTransport.dll		(Win32 & 64)
	 * - NativeSerialTransport.so		(Linux)
	 * - NativeSerialTransport.jnilib	(Mac OS)
	 */
	static 
	{
		System.loadLibrary("NativeSerialTransport"); 
	}

	
	private final long DISCONNECTED = -1;
	private long hTransport = DISCONNECTED;
	private String mOpenName = "";
	private String mFriendlyName = "";	
	private int mBaudrate = DEFAULT_BAUDRATE;
	private boolean mConnected = false;
	private boolean mDisconnecting = false;
	
	private boolean transportNotValid()
	{
		return (hTransport==DISCONNECTED || hTransport==0);
	}
	
	/** NUR module baudrate, 115200 bps. */
	public static final int BAUDRATE_115200 = 115200;
	/** NUR module baudrate, 230400 bps. */
	public static final int BAUDRATE_230400 = 230400;
	/** NUR module baudrate, 500000 bps. */
	public static final int BAUDRATE_500000 = 500000;
	/** NUR module baudrate, 1000000 bps. */
	public static final int BAUDRATE_1000000 = 1000000;
	/** NUR module baudrate, 1500000 bps (use with care). */
	public static final int BAUDRATE_1500000 = 1500000;		
	/** NUR module baudrate, 38400 bps. */
	public static final int BAUDRATE_38400 = 38400;
	/** NUR module baudrate, 9600 bps. */
	public static final int BAUDRATE_9600 = 9600;	
	/** Default NUR module baudrate, currently {@link #BAUDRATE_115200}. */
	public static final int DEFAULT_BAUDRATE = BAUDRATE_115200;
	
	private String bdrToString(int bdr)
	{
		return Integer.toString(bdr);
	}
	/**
	 * Native serial transport constructor.
	 * 
	 * @param openName	Serial port name as required per platform.
	 * @param baudrate	Baudrate for the serial port.
	 */
	public NativeSerialTransport(String openName, int baudrate) 
	{		
		mOpenName = openName;
		mFriendlyName = openName;
		mBaudrate = baudrate;
			
		mDisconnecting = false;
	}	
	
	/**
	 * Native serial transport constructor using default baudrate {@link #DEFAULT_BAUDRATE}.
	 * 
	 * @param openName	Serial port name as required per platform.
	 */
	public NativeSerialTransport(String openName) 
	{		
		this(openName, DEFAULT_BAUDRATE);
	}	
	
	/**
	 * Constructor that takes in {@link com.nordicid.nativeserial.SerialPort } class.
	 * 
	 * @param sp Serial port definition as {@link com.nordicid.nativeserial.SerialPort } class.
	 * @param baudRate	Baud rate.
	 * @see com.nordicid.nativeserial.SerialPort
	 */
	public NativeSerialTransport(SerialPort sp, int baudRate)
	{
		mOpenName = sp.getOpenName();
		mFriendlyName = sp.getFriendlyName();
		mBaudrate = baudRate; 
				
		mDisconnecting = false;
	}
	
	/**
	 * Constructor that takes in {@link com.nordicid.nativeserial.SerialPort } class.
	 * Default baud rate {@link #DEFAULT_BAUDRATE} is used.
	 * 
	 * @param sp Serial port definition as {@link com.nordicid.nativeserial.SerialPort } class.
	 * @see com.nordicid.nativeserial.SerialPort
	 */
	public NativeSerialTransport(SerialPort sp)
	{
		this(sp, DEFAULT_BAUDRATE);
	}
		
	/**
	 * Enumerate available serial ports on system. The names are "open names". 
	 * @return String Array of enumerated ports. Return null when there's no ports available.
	 * @throws NurApiException
	 * @see com.nordicid.nurapi.NurApiException
	 */
	public static String[] enumeratePorts() throws NurApiException 
	{ 
		return enumeratePortsNative(); 
	}
	
	/**
	 * Enumerate ports with return SerialPort array.
	 * 
	 * @return SerialPort array containing open names (used in open) and friendly names.
	 * 
	 * @throws NurApiException
	 * @see com.nordicid.nurapi.NurApiException
	 */
	public static SerialPort []enumeratePortsEx() throws NurApiException
	{
		return enumaratePortsNativeEx();
	}
			
	/**
	 * Return the current baudrate of the open serial port.
	 * 
	 * @return	Baudrate as bps.
	 * @throws Exception May throw an exception when the 
	 */
	public int getBaudrate() throws Exception 
	{ 
		return getBaudrateNative(hTransport); 
	}
	
	/**
	 * Change the baudrate of the serial connection.
	 * NOTE 1: the API needs to set the serial speed to the module also.
	 * NOTE 2: see the values for baudrate; platform itself may require a conversion on the native side. 
	 * @param baudrate		New baudrate.
	 * @throws Exception	Exception is thrown when the baudrate is invalid or the connection is not up.
	 * {@link #BAUDRATE_38400}
	 * {@link #BAUDRATE_115200}
	 * {@link #BAUDRATE_230400}
	 * {@link #BAUDRATE_500000}
	 * {@link #BAUDRATE_1000000} 
	 * {@link #BAUDRATE_1500000}
	 * {@link #BAUDRATE_9600}
	 */
	public boolean setBaudrate(int baudrate) throws Exception 
	{ 
		return setBaudrateNative(hTransport, baudrate); 
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect() throws Exception
	{
		mDisconnecting = false;
		if(mConnected)
		{
			return;
		}

		if (mOpenName.equals("")) 
		{
			throw new IllegalArgumentException("Port name is not set.", null);
		}
		
		hTransport = connectNative(mOpenName, mBaudrate);
		mConnected = (hTransport != DISCONNECTED && hTransport!=0);
		
		if (!isConnected())
		{
			throw new IOException("Connecting to \"" + mOpenName + "\" failed.");
		}
	}	

	/**
	 * {@inheritDoc}
	 */	
	@Override
	public void disconnect()
	{
		mDisconnecting = true;
		if (mConnected)
			disconnectNative(hTransport);

		hTransport = DISCONNECTED;
		mConnected = false;
		mBaudrate = DEFAULT_BAUDRATE;
	}

	/**
	 * {@inheritDoc}
	 */	
	@Override
	public int readData(byte[] buffer) throws IOException
	{	
		int read = 0;
		
		if (mDisconnecting)
			return 0;
		
		if (!mConnected || transportNotValid())
		{			
			return -1;
		}
		
		read = readDataNative(hTransport, buffer, buffer.length);
			
		return read;
	}

	/**
	 * {@inheritDoc}
	 */	
	@Override
	public int writeData(byte[] buffer, int len) throws IOException
	{
		if (mDisconnecting)
			return len;

		if (!mConnected || transportNotValid())
		{
			return -1;
		}
				
		if ((len % 64) == 0)
		{
			buffer[len] = (byte)0xFF;
			writeDataNative(hTransport, buffer, 0, len + 1);
		}
		else
			writeDataNative(hTransport, buffer, 0, len);
		
		return len;
	}

	/**
	 * {@inheritDoc}
	 */	
	@Override
	public boolean isConnected() 
	{ 
		return mConnected && !mDisconnecting;
	}

	/**
	 * {@inheritDoc}
	 */	
	@Override
	public boolean disableAck() {
		// see: https://github.com/NordicID/nur_sample_java/blob/master/transports/NurApiSerialTransport/src/com/nordicid/nurapi/NurApiSerialTransport.java
		return false;
	}		
}

