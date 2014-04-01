jnaCan
======
CAN bus for Java, uses JNA to access the Linux SocketCan API.

This software is Alpha quality, please do not use in production.

Through the use of Java Native Access, no native code needs to be written to access the SocketCan API. This project is pure Java (well technically the JNA jar has some small ready made native binary libraries included).

The library currently only supports the raw can mode. The read and write calls will block. However there is socket option which sets the SO_RCVTIMEO value so that the read will timeout.

A Netty 4.0 channel implementation is included.

jnaerator switches
==================
Note that some of constants defined in the linux C header files may be different on your platform and so the generated Java code might not work. I have tested the bindings on x86_64 and arm7l (BeagleBone).

If you need to create your own bindings, this is the command I used with jnaerator to generate the Java files in the JNA "CLibrary" binding package. Some small modifications were made afterwards, I addded padding to can_frame and fixed an error in ifreq.

java -jar jnaerator-0.12-SNAPSHOT-20130727.jar -D__s8=char -runtime JNA -mode Directory -I/usr/include/x86_64-linux-gnu/ -I/usr/include/linux/ -library c /usr/include/net/if.h /usr/include/linux/can.h /usr/include/x86_64-linux-gnu/sys/socket.h /usr/include/x86_64-linux-gnu/bits/socket.h /usr/include/asm-generic/socket.h /usr/include/unistd.h /usr/include/x86_64-linux-gnu/sys/ioctl.h /usr/include/linux/can/raw.h /usr/include/linux/can/error.h /usr/include/linux/can/gw.h /usr/include/linux/can/bcm.h /usr/include/linux/can/netlink.h
