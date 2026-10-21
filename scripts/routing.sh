#!/bin/bash

ip addr del 192.168.43.99/24 dev wlan0
iptables -t nat -F
iptables -F


# 1. Enable IP forwarding
echo 1 > /proc/sys/net/ipv4/ip_forward

# 2. Add a virtual IP alias to wlan0 that won't be consumed locally
ip addr add 192.168.43.99/24 dev wlan0

# 3. DNAT: redirect incoming traffic to Limelight
iptables -t nat -A PREROUTING -i wlan0 -d 192.168.43.99 -p tcp -m tcp --dport 5800:5810 -j DNAT --to-destination 172.29.0.1

# 4. SNAT (MASQUERADE): ensure replies from Limelight go back through Control Hub
iptables -t nat -A POSTROUTING -o eth0 -p tcp -d 172.29.0.1 --dport 5800:5810 -j MASQUERADE

# 5. FORWARD: allow the forwarded traffic
iptables -A FORWARD -i wlan0 -o eth0 -p tcp -d 172.29.0.1 --dport 5800:5810 -j ACCEPT