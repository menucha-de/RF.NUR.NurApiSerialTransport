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
package com.nordicid.nativeserial;

/**
 * Class that implements serial port information.
 * 
 * @author Nordic ID
 *
 * @version 1.0.0
 * 
 */
public class SerialPort 
{
	private String mOpenName = "";
	private String mFriendlyName = "";
	private int mPortNumber = -1;
	
	/**
	 * The constructor. Can be called from the native method that implements the enumeration.
	 *  
	 * @param openName		This is the name of the serial port that the native system uses to open the serial port.
	 * @param friendlyName	This is the "human readable" name of the assigned port if available.
	 * @param portNumber	Serial port's number. Not used by the native layer, but may be usable in some systems.
	 * 
	 * @see com.nordicid.nativeserial.NativeSerialTransport#enumeratePortsEx()
	 */
	public SerialPort(String openName, String friendlyName, int portNumber)
	{
		mOpenName = openName;
		mFriendlyName = friendlyName;
		mPortNumber = portNumber;
	}
	
	/**
	 * With this name the port open is called.
	 * 
	 * @return	The platform dependent name that is used when the serial port open is called. 
	 */
	public String getOpenName()
	{
		return mOpenName;
	}
	
	/**
	 * System given friendly name for the serial port.
	 * 
	 * @return A "human readable name" for the serial port e.g. "Communications port 1".
	 */
	public String getFriendlyName()
	{
		return mFriendlyName;
	}
	
	/**
	 * Port number.
	 * 
	 * @return The port number that was assigned during the port enumeration.
	 */
	public int getPortNumber()
	{
		return mPortNumber;
	}	
}

