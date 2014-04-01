#!/bin/sh

modprobe can
modprobe can_raw
modprobe vcan
ip link add dev vcan0 type vcan
ip link set up vcan0
