/*
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */

package net.jazdw.jnacan;

import static net.jazdw.jnacan.c.CLibrary.SIOCGIFINDEX;
import static net.jazdw.jnacan.c.CLibrary.SIOCGIFMTU;
import static net.jazdw.jnacan.c.CLibrary.SIOCGIFNAME;

import java.io.IOException;
import java.net.SocketAddress;

import com.sun.jna.Native;

import net.jazdw.jnacan.c.ifreq;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Copyright (C) 2014 Jared Wiltshire. All rights reserved.
 * @author Jared Wiltshire
 */
@Getter
@Setter
@ToString
public class CanInterface extends SocketAddress {
    private static final long serialVersionUID = 4959491385594877740L;
    
    public static final CanInterface ALL = new CanInterface(0, "All");
    
    Integer index = null;
    String name = null;
    Integer mtu = null;
    
    public CanInterface(int index) {
        this.index = index;
    }
    
    public CanInterface(String name) {
        this.name = name;
    }
    
    public CanInterface(int index, String name) {
        this.index = index;
        this.name = name;
    }
    
    public boolean isAllInterface() {
        return index == 0;
    }
    
    public void resolveName(CanSocket socket) throws IOException {
        if (name != null)
            return;
        if (index == null)
            throw new IllegalArgumentException("Index must be resolved");
        
        if (index == ALL.index) {
            name = ALL.name;
            return;
        }
        
        ifreq ifr = new ifreq();
        ifr.ifr_ifru = new ifreq.ifr_ifru_union(index);
        if (socket.ioctl(SIOCGIFNAME, ifr) != 0) {
            throw new IOException("Could not find name of interface " + index);
        }
        name = Native.toString(ifr.ifr_ifrn.ifrn_name);
    }
    
    public void resolveIndex(CanSocket socket) throws IOException {
        if (index != null)
            return;
        if (name == null)
            resolveName(socket);
        
        ifreq ifr = new ifreq();
        ifr.ifr_ifrn = new ifreq.ifr_ifrn_union(CanSocket.toFixedLengthByteArray(name, 16));
        if (socket.ioctl(SIOCGIFINDEX, ifr) != 0) {
            throw new IOException("Could not find interface " + name);
        }
        index = ifr.ifr_ifru.ifru_ivalue;
    }
    
    public void resolveMTU(CanSocket socket) throws IOException {
        if (mtu != null)
            return;
        if (index == ALL.index)
            throw new IllegalArgumentException("Can't get MTU for 'All' interface");
        if (name == null)
            resolveName(socket);
        
        ifreq ifr = new ifreq();
        ifr.ifr_ifrn = new ifreq.ifr_ifrn_union(CanSocket.toFixedLengthByteArray(name, 16));
        if (socket.ioctl(SIOCGIFMTU, ifr) != 0)
            throw new IOException("Could not get MTU of interface " + name);
        mtu = ifr.ifr_ifru.ifru_mtu;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CanInterface other = (CanInterface) obj;
        if (index == null) {
            if (other.index != null)
                return false;
        } else if (!index.equals(other.index))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((index == null) ? 0 : index.hashCode());
        return result;
    }
}
