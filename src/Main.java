/**
 * 
 */
package jp.co.patlite.lr6_usb.sample;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.usb4java.Context;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;

/**
 * @author
 *
 */
public class Main {

	public static void main(String[] args) {
		Control ctr = new Control();
		
		// Connect to NE-USB
		int ret = ctr.usb_open();
		if(ret < 0 ) {
 			System.out.println("device not found");
			return;
		}
		
		try {
			// Argument check
			String commandId = " ";
            if (args.length > 0) {
                commandId = args[0];
            }

            // Command judgment
            switch (commandId)
            {
                case "1":
                {
                  	// Specify the LED color and LED pattern to turn on and turn on the pattern
                    if (args.length >= 3)
                       	ctr.set_light((byte)Integer.parseInt(args[1]), (byte)Integer.parseInt(args[2]));
                    break;
                }

                case "2":
                {
                	// Specify the buzzer pattern and make the buzzer sound
                    if (args.length >= 3)
                    	ctr.set_buz((byte)Integer.parseInt(args[1]), (byte)Integer.parseInt(args[2]));
                    break;
                }

                case "3":
                {
                	// Change the buzzer volume by specifying the volume
                    if (args.length >= 2)
                    	ctr.set_vol((byte)Integer.parseInt(args[1]));
                    break;
                }

                case "4":
                {
                	// Sound the buzzer by specifying the buzzer pattern, number of times, and volume.
                    if (args.length >= 4)
                    	ctr.set_buz_ex((byte)Integer.parseInt(args[1]), (byte)Integer.parseInt(args[2]), (byte)Integer.parseInt(args[3]));
                    break;
                }

                case "5":
                {
                	// Change the connection display settings
                    if (args.length >= 2)
                    	ctr.set_setting((byte)Integer.parseInt(args[1]));
                    break;
                }

                case "6":
                {
                    // Get touch sensor input status
                    int state = ctr.getTouchSensorState();
                    if(state == 1){
                        System.err.println("touch sensor input = ON");
                    }
                    else if(state == 0){
                        System.err.println("touch sensor input = OFF");
                    }
                    else{
                        System.err.println("USB communication failed");
                    }
                    break;
                }

                case "7":
                {
                	// Turn off the LED and stop the buzzer
                   	ctr.reset();
                    break;
                }

            }
                    
		} finally {
			// End processing
			ctr.usb_close();
		}
	}

}

class Control {

	// Vendor ID
	private static int VENDOR_ID = 0x191A;
	// Device ID
	private static int DEVICE_ID = 0x6001;
	// Command version
	private static byte COMMAND_VERSION = 0x00;
	// Command ID
	private static byte COMMAND_ID_CONTROL = 0x00;
	private static byte COMMAND_ID_SETTING = 0x01;
	private static byte COMMAND_ID_GETSTATE = (byte)0x80;
	// Endpoint address for sending to host -> USB controlled multicolor indicator
	private static byte	ENDPOINT_ADDRESS = 0x01;
	// Endpoint address for sending to USB -> host controlled multicolor indicator
	private static byte	ENDPOINT_ADDRESS_GET = (byte)0x81;
	// Time-out time when sending a command
	private static int	SEND_TIMEOUT = 1000;
	// Protocol data area size
	private static byte	SEND_BUFFER_SIZE = 8;
	private static byte	RECV_BUFFER_SIZE = 2;
	// openings
	private static byte	BLANK = 0x00;
	
	// LED color
	private static byte LED_COLOR_OFF = 0;				// Off
	private static byte LED_COLOR_RED = 1;				// Red
	private static byte LED_COLOR_GREEN = 2;			// green
	private static byte LED_COLOR_YELLOW = 3;			// yellow
	private static byte LED_COLOR_BLUE = 4;				// Blue
	private static byte LED_COLOR_PURPLE = 5;			// purple
	private static byte LED_COLOR_LIGHTBLUE = 6;		// Sky blue
	private static byte LED_COLOR_WHITE = 7;			// White
	private static byte LED_COLOR_KEEP = 0x0F;			// Keep the current settings

	// LED pattern
	private static byte	LED_OFF = 0x00;					// Off
	private static byte	LED_ON = 0x01;					// Lit
	private static byte	LED_PATTERN1 = 0x02;			// LED pattern1
	private static byte	LED_PATTERN2 = 0x03;			// LED pattern2
	private static byte	LED_PATTERN3 = 0x04;			// LED pattern3
	private static byte	LED_PATTERN4 = 0x05;			// LED pattern4
	private static byte	LED_PATTERN5 = 0x06;			// LED pattern5
	private static byte	LED_PATTERN6 = 0x07;			// LED pattern6
	private static byte LED_PATTERN_KEEP = 0x0F;		// Keep the current settings

	// Number of buzzers
	private static byte BUZZER_COUNT_CONTINUE = 0x00;	// Continuous operation
	private static byte BUZZER_COUNT_KEEP = 0x0F;		// Keep the current settings

	// Buzzer pattern
	private static byte BUZZER_OFF = 0x00;				// Stop
	private static byte BUZZER_ON = 0x01;				// Blow (continuous)
	private static byte BUZZER_SWEEP = 0x02;			// Sweep sound
	private static byte BUZZER_INTERMITTENT = 0x03;		// Intermittent sound
	private static byte BUZZER_WEEK_ATTENTION = 0x04;	// Weak caution sound
	private static byte BUZZER_STRONG_ATTENTION = 0x05;	// Strong attention sound
	private static byte BUZZER_SHINING_STAR = 0x06;		// shining star
	private static byte BUZZER_LONDON_BRIDGE = 0x07;	// London bridge
	private static byte	BUZZER_KEEP = 0x0F;				// Keep the current settings

	// Buzzer volume
	private static byte BUZZER_VOLUME_OFF = 0x00;		// Mute
	private static byte BUZZER_VOLUME_MAX = 0x0A;		// Maximum volume
	private static byte BUZZER_VOLUME_KEEP = 0x0F;		// Keep the current settings

	// Setting
	private static byte SETTING_OFF = 0x00;				// OFF
	private static byte SETTING_ON = 0x01;				// ON
	
	// Internal variables
	Context context;
	DeviceHandle devHandle;
	
	/**
	 * constructor
	 * 
	 */
	Control() {
		context = null;
		devHandle = null;
	}
	
	/**
	 * Connect to server
	 * 
	 * @return Success: 0, Failure: Other than 0
	 */
	public int usb_open() {
 		int ret = 0;
		// Initialization process
 		context = new Context();
 		int initret = LibUsb.init(context);
 		if(initret != LibUsb.SUCCESS) {
 			return -1;
 		}
 		
 		// Device open
 		devHandle = LibUsb.openDeviceWithVidPid(context, (short)VENDOR_ID, (short)DEVICE_ID);
 		if(devHandle == null) {
 			return -1;
 		}
 		
 		// Interface acquisition
 		int IntRet = LibUsb.claimInterface(devHandle, 0);
 		if(IntRet != 0) {
 			return -2;
 		}

		return ret;
	}

	/**
	 * Close socket
	 */
	public void usb_close() {
		// End processing
		LibUsb.close(devHandle);
		LibUsb.exit(context);
	}

	/**
	 * Send command
	 * 
	 * @param sendData Transmission data
	 * @return Send result (number of bytes sent, negative value is error)
	 */
	private int send_command(final byte[] sendData) {
		int ret = 0;
		
		// Convert data
		ByteBuffer setData = ByteBuffer.allocateDirect(SEND_BUFFER_SIZE);
		setData.put(sendData);
		
		// Check the handle
		if(devHandle == null) {
			return 0;
		}
		
		// data transfer
		IntBuffer sendLength = IntBuffer.allocate(SEND_BUFFER_SIZE);
		int TranRet = LibUsb.interruptTransfer(devHandle, ENDPOINT_ADDRESS, setData, sendLength, (long)SEND_TIMEOUT);
		if(TranRet == 0) {
			ret = sendLength.get();
		}else{
			ret = -1;
		}
		
		return ret;
	}

	/**
	 * Specify the LED color and LED pattern to turn on and turn on the pattern<br>
	 * Buzzer pattern and buzzer volume maintain their current state
	 * 
	 * @param color LED color to control (off: 0, red: 1, green: 2, yellow: 3, blue: 4, purple: 5, sky blue: 6, white: 7, maintain the current settings: 0x08 to 0x0F)
	 * @param state LED pattern (off: 0x00, on: 0x01, LED pattern 1: 0x02, LED pattern 2: 0x03, LED pattern 3: 0x04, LED pattern 4: 0x05, LED pattern 5: 0x06, LED pattern 6: 0x07, current settings Maintain: 0x08-0x0F)
	 * @return Success: 0, Failure: Other than 0
	 */

	public int set_light(final byte color, final byte state) {
		// Argument range check
		if((0x0F < color) || (0x0F < state)) {
			return -1;
		}
		
		byte[] sendData = new byte[Control.SEND_BUFFER_SIZE];

		// Command version
		sendData[0] = Control.COMMAND_VERSION;

		// Command ID
		sendData[1] = Control.COMMAND_ID_CONTROL;

		// Buzzer control
		sendData[2] = (byte) ((Control.BUZZER_COUNT_KEEP << 4) | Control.BUZZER_KEEP);

		// Buzzer volume
		sendData[3] = Control.BUZZER_VOLUME_KEEP;
		
		// LED
		sendData[4] = (byte) ((color << 4) | state);
		
		// openings
		sendData[5] = Control.BLANK;
		sendData[6] = Control.BLANK;
		sendData[7] = Control.BLANK;
	
		// Send command
		int ret = this.send_command(sendData);
		if (ret <= 0) {
			System.err.println("failed to send data");
		}

		return ret;
	}

	/**
	 * Specify the buzzer pattern and buzzer.
	 * The LED and buzzer volume remain in their current state.
	 * 
	 * @param buz_state Buzzer pattern (stop: 0x00, continuous sound: 0x01, sweep sound: 0x02, intermittent sound: 0x03, weak caution sound: 0x04, strong caution sound: 0x05, glitter star: 0x06, London Bridge: 0x07, maintain the current settings: 0x08-0x0F)
	 * @param limit Continuous operation: 0, Number of operations: 1 to 14, Maintain current settings: 0xF
	 * @return Success: 0, Failure: Other than 0
	 */

	public int set_buz(final byte buz_state, final byte limit ) {
		// Argument range check
		if((0x0F < buz_state) || (0x0F < limit)) {
			return -1;
		}
		
		byte[] sendData = new byte[Control.SEND_BUFFER_SIZE];

		// Command version
		sendData[0] = Control.COMMAND_VERSION;

		// Command ID
		sendData[1] = Control.COMMAND_ID_CONTROL;

		// Buzzer control
		sendData[2] = (byte) ((limit << 4) | buz_state);

		// Buzzer volume
		sendData[3] = Control.BUZZER_VOLUME_KEEP;
		
		// LED
		sendData[4] = (byte) ((Control.LED_COLOR_KEEP << 4) | Control.LED_PATTERN_KEEP);
		
		// openings
		sendData[5] = Control.BLANK;
		sendData[6] = Control.BLANK;
		sendData[7] = Control.BLANK;
	

		// Send command
		int ret = this.send_command(sendData);
		if (ret <= 0) {
			System.err.println("failed to send data");
		}

		return ret;
	}

	/**
	 * Change the buzzer volume by specifying the volume
	 * LED and buzzer patterns maintain their current state
	 * 
	 * @param volume The volume (setting of sound deadening :0x00, stage volume :0x01-0x09, the biggest volume :0x0A and the current state is maintained: 0x0B-0x0F)
	 * @return Success: 0, Failure: Other than 0
	 */

	public int set_vol(final byte volume) {
		// Argument range check
		if(0x0F < volume) {
			return -1;
		}
		
		byte[] sendData = new byte[Control.SEND_BUFFER_SIZE];

		// Command version
		sendData[0] = Control.COMMAND_VERSION;

		// Command ID
		sendData[1] = Control.COMMAND_ID_CONTROL;

		// Buzzer control
		sendData[2] = (byte) ((Control.BUZZER_COUNT_KEEP << 4) | Control.BUZZER_KEEP);

		// Buzzer volume
		sendData[3] = volume;
		
		// LED
		sendData[4] = (byte) ((Control.LED_COLOR_KEEP << 4) | Control.LED_PATTERN_KEEP);
		
		// openings
		sendData[5] = Control.BLANK;
		sendData[6] = Control.BLANK;
		sendData[7] = Control.BLANK;
	

		// Send command
		int ret = this.send_command(sendData);
		if (ret <= 0) {
			System.err.println("failed to send data");
		}

		return ret;
	}

	/**
	 * A buzzer is controlled by the buzzer pattern and a scale.<br>
	 * The buzzer pattern, the number of times and the volume are designated and a buzzer is blown.
	 * An LED maintains the present state.
	 * 
	 * @param buz_state The buzzer pattern (setting of :0x06 of stars twinkling in the sky, London bridge :0x07 and the current state is maintained: 0x08-0x0F)
	 * @param limit Continued movement :0 and frequency movement: Setting of 1-14 and the current state is maintained: 0xF
	 * @param volume[in]: The volume (setting of sound deadening :0x00, stage volume :0x01-0x09, the biggest volume :0x0A and the current state is maintained: 0x0B-0x0F)
	 * @return Success: 0, Failure: Other than 0
	 */

	public int set_buz_ex(final byte buz_state, final byte limit, final byte volume) {
		// Argument range check
		if((0x0F < buz_state) || (0x0F < limit) || (0x0F < volume)) {
			return -1;
		}

		byte[] sendData = new byte[Control.SEND_BUFFER_SIZE];

		// Command version
		sendData[0] = Control.COMMAND_VERSION;

		// Command ID
		sendData[1] = Control.COMMAND_ID_CONTROL;

		// Buzzer control
		sendData[2] = (byte) ((limit << 4) | buz_state);

		// Buzzer volume
		sendData[3] = volume;
		
		// LED
		sendData[4] = (byte) ((Control.LED_COLOR_KEEP << 4) | Control.LED_PATTERN_KEEP);
		
		// openings
		sendData[5] = Control.BLANK;
		sendData[6] = Control.BLANK;
		sendData[7] = Control.BLANK;
	

		// Send command
		int ret = this.send_command(sendData);
		if (ret <= 0) {
			System.err.println("failed to send data");
		}

		return ret;
	}

	/**
	 * The setting of connection indication is changed.
	 * 
	 * @param setting Setting(OFF：0x00、ON：0x01)
	 * @return Success: 0, Failure: Other than 0
	 */

	public int set_setting(final byte setting) {
		// Argument range check
		if((SETTING_OFF != setting) && (SETTING_ON != setting)) {
			return -1;
		}

		byte[] sendData = new byte[Control.SEND_BUFFER_SIZE];

		// Command version
		sendData[0] = Control.COMMAND_VERSION;

		// Command ID
		sendData[1] = Control.COMMAND_ID_SETTING;

		// Setting
		sendData[2] = setting;

		// openings
		sendData[3] = Control.BLANK;
		sendData[4] = Control.BLANK;
		sendData[5] = Control.BLANK;
		sendData[6] = Control.BLANK;
		sendData[7] = Control.BLANK;
	

		// Send command
		int ret = this.send_command(sendData);
		if (ret <= 0) {
			System.err.println("failed to send data");
		}

		return ret;
	}

	/**
	 * Get touch sensor input status<br>
	 * 
	 * @return Acquisition failure:-1, Touch sensor input OFF:0、Touch sensor input ON:1
	 */

	public int getTouchSensorState() {
		byte[] sendData = new byte[Control.SEND_BUFFER_SIZE];

		// Command version
		sendData[0] = Control.COMMAND_VERSION;

		// Command ID
		sendData[1] = Control.COMMAND_ID_GETSTATE;
		
		// Blank
		sendData[2] = Control.BLANK;
		sendData[3] = Control.BLANK;
		sendData[4] = Control.BLANK;
		sendData[5] = Control.BLANK;
		sendData[6] = Control.BLANK;
		sendData[7] = Control.BLANK;

		// Send command
		int ret = this.send_command(sendData);
		if (ret <= 0) {
			System.err.println("failed to send data");
			return -1;
		}
		
		// Receive response
		ByteBuffer getData = ByteBuffer.allocateDirect(RECV_BUFFER_SIZE);
		IntBuffer sendLength = IntBuffer.allocate(RECV_BUFFER_SIZE);
		ret = LibUsb.interruptTransfer(devHandle, ENDPOINT_ADDRESS_GET, getData, sendLength, (long)SEND_TIMEOUT);
		if (ret != 0) {
			System.err.println("failed to receive data");
			return -1;
		}
		
		if ((getData.get(1) & 1) == 1) {
			return 1;
		}
		return 0;
	}

	/**
	 * Reset<br>
	 * The LED unit is turned off completely and a buzzer is stopped.
	 * 
	 * @return Success: 1 or more, Failure: 0 or less
	 */

	public int reset() {
		byte[] sendData = new byte[Control.SEND_BUFFER_SIZE];

		// Command version
		sendData[0] = Control.COMMAND_VERSION;

		// Command ID
		sendData[1] = Control.COMMAND_ID_CONTROL;

		// Buzzer control
		sendData[2] = (byte) ((Control.BUZZER_COUNT_KEEP << 4) | Control.BUZZER_OFF);

		// Buzzer volume
		sendData[3] = Control.BUZZER_VOLUME_KEEP;
		
		// LED
		sendData[4] = (byte) ((Control.LED_COLOR_OFF << 4) | Control.LED_OFF);
		
		// openings
		sendData[5] = Control.BLANK;
		sendData[6] = Control.BLANK;
		sendData[7] = Control.BLANK;

		// Send command
		int ret = this.send_command(sendData);
		if (ret <= 0) {
			System.err.println("failed to send data");
		}

		return ret;
	}

}