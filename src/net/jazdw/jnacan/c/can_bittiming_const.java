package net.jazdw.jnacan.c;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * CAN harware-dependent bit-timing constant<br>
 * * Used for calculating and checking bit-timing parameters<br>
 * <i>native declaration : can/netlink.h:25</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class can_bittiming_const extends Structure {
	/**
	 * Name of the CAN controller hardware<br>
	 * C type : char[16]
	 */
	public byte[] name = new byte[16];
	/**
	 * Time segement 1 = prop_seg + phase_seg1<br>
	 * C type : __u32
	 */
	public int tseg1_min;
	/** C type : __u32 */
	public int tseg1_max;
	/**
	 * Time segement 2 = phase_seg2<br>
	 * C type : __u32
	 */
	public int tseg2_min;
	/** C type : __u32 */
	public int tseg2_max;
	/**
	 * Synchronisation jump width<br>
	 * C type : __u32
	 */
	public int sjw_max;
	/**
	 * Bit-rate prescaler<br>
	 * C type : __u32
	 */
	public int brp_min;
	/** C type : __u32 */
	public int brp_max;
	/** C type : __u32 */
	public int brp_inc;
	public can_bittiming_const() {
		super();
	}
	protected List<? > getFieldOrder() {
		return Arrays.asList("name", "tseg1_min", "tseg1_max", "tseg2_min", "tseg2_max", "sjw_max", "brp_min", "brp_max", "brp_inc");
	}
	/**
	 * @param name Name of the CAN controller hardware<br>
	 * C type : char[16]<br>
	 * @param tseg1_min Time segement 1 = prop_seg + phase_seg1<br>
	 * C type : __u32<br>
	 * @param tseg1_max C type : __u32<br>
	 * @param tseg2_min Time segement 2 = phase_seg2<br>
	 * C type : __u32<br>
	 * @param tseg2_max C type : __u32<br>
	 * @param sjw_max Synchronisation jump width<br>
	 * C type : __u32<br>
	 * @param brp_min Bit-rate prescaler<br>
	 * C type : __u32<br>
	 * @param brp_max C type : __u32<br>
	 * @param brp_inc C type : __u32
	 */
	public can_bittiming_const(byte name[], int tseg1_min, int tseg1_max, int tseg2_min, int tseg2_max, int sjw_max, int brp_min, int brp_max, int brp_inc) {
		super();
		if ((name.length != this.name.length)) 
			throw new IllegalArgumentException("Wrong array size !");
		this.name = name;
		this.tseg1_min = tseg1_min;
		this.tseg1_max = tseg1_max;
		this.tseg2_min = tseg2_min;
		this.tseg2_max = tseg2_max;
		this.sjw_max = sjw_max;
		this.brp_min = brp_min;
		this.brp_max = brp_max;
		this.brp_inc = brp_inc;
	}
	public static class ByReference extends can_bittiming_const implements Structure.ByReference {
		
	};
	public static class ByValue extends can_bittiming_const implements Structure.ByValue {
		
	};
}