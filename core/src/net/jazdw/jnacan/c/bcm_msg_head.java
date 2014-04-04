package net.jazdw.jnacan.c;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * struct bcm_msg_head - head of messages to/from the broadcast manager<br>
 * @opcode:    opcode, see enum below.<br>
 * @flags:     special flags, see below.<br>
 * @count:     number of frames to send before changing interval.<br>
 * @ival1:     interval for the first @count frames.<br>
 * @ival2:     interval for the following frames.<br>
 * @can_id:    CAN ID of frames to be sent or received.<br>
 * @nframes:   number of frames appended to the message head.<br>
 * @frames:    array of CAN frames.<br>
 * <i>native declaration : can/bcm.h:13</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class bcm_msg_head extends Structure {
	/** C type : __u32 */
	public int opcode;
	/** C type : __u32 */
	public int flags;
	/** C type : __u32 */
	public int count;
	/** C type : timeval */
	public timeval ival1;
	/** C type : timeval */
	public timeval ival2;
	/** C type : canid_t */
	public int can_id;
	/** C type : __u32 */
	public int nframes;
	/** C type : can_frame[0] */
	public can_frame[] frames = new can_frame[0];
	public bcm_msg_head() {
		super();
	}
	protected List<? > getFieldOrder() {
		return Arrays.asList("opcode", "flags", "count", "ival1", "ival2", "can_id", "nframes", "frames");
	}
	/**
	 * @param opcode C type : __u32<br>
	 * @param flags C type : __u32<br>
	 * @param count C type : __u32<br>
	 * @param ival1 C type : timeval<br>
	 * @param ival2 C type : timeval<br>
	 * @param can_id C type : canid_t<br>
	 * @param nframes C type : __u32<br>
	 * @param frames C type : can_frame[0]
	 */
	public bcm_msg_head(int opcode, int flags, int count, timeval ival1, timeval ival2, int can_id, int nframes, can_frame frames[]) {
		super();
		this.opcode = opcode;
		this.flags = flags;
		this.count = count;
		this.ival1 = ival1;
		this.ival2 = ival2;
		this.can_id = can_id;
		this.nframes = nframes;
		this.frames = frames;
	}
	public static class ByReference extends bcm_msg_head implements Structure.ByReference {
		
	};
	public static class ByValue extends bcm_msg_head implements Structure.ByValue {
		
	};
}
