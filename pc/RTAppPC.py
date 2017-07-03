#!/usr/bin/python

from __future__ import print_function

import socket
import struct

import random
import matplotlib.pyplot as plt

VERSION = "0.1"
#UDP_IP = "127.0.0.1"
UDP_IP = ""
UDP_PORT = 5505

#------------------------------------------

print("RTAppPC v" + VERSION)

#------------------------------------------

columns = 4
rows = 2

f, axes = plt.subplots(rows, columns, sharey=True)

plt.axis([0,1,0,1])
plt.ion()
plt.show()

#def generate_axes(data) :
#  return range(len(data)), data

def generate_axes(data) :
  ds = sorted(data)
  
  x = range(0, int(ds[len(ds) - 1]))
  y = []
  
  ds_i = 0
  
  for i in x :
    y.append(0)
    while ds[ds_i] <= i :
      ds_i = ds_i + 1
    y[i] = float(float(ds_i) / len(ds))
  
  return x, y

def plot_data(task_id, task_data) :
  print("Plotting data for Task_" + str(task_id))
  ax = axes[task_id / columns][task_id % columns]
  
  if len(task_data) == 0 :
    return
  
  x, y = generate_axes(task_data)
  
  #print("x:", x)
  #print("y:", y)
  
  #axes[task_id].axis([0,40,0,40])
  
  plt.axis([0,len(x),0,1])
  #ax.clear()
  ax.plot(x, y)
  ax.set_title("Task_" + str(task_id))
  plt.draw()
  plt.pause(0.001)
  
  print("DONE")

def parse_message(msg) :
  print("Parsing message...")
  
  offset = 0
  
  protocol = struct.unpack(">i", msg[offset:(offset + 4)])[0]
  offset = offset + 4
  task = struct.unpack(">i", msg[offset:(offset + 4)])[0]
  offset = offset + 4
  dataSize = struct.unpack(">i", msg[offset:(offset + 4)])[0]
  offset = offset + 4
  length = struct.unpack(">i", msg[offset:(offset + 4)])[0]
  offset = offset + 4
  
  print("Protocol:", protocol)
  print("TaskID:", task)
  print("Data Size:", dataSize)
  print("Length:", length)
  print("Data:", length / dataSize)
  
  data = []
  
  try:
    for i in xrange(length) :
      value = struct.unpack(">d", msg[offset:(offset + dataSize)])[0]
      data.append(value)
      offset = offset + dataSize
    
    print(data)
    plot_data(task_id = task, task_data = data)
  except struct.error:
    print("struct.error, going on..." * 10000)
    pass
  
#------------------------------------------

print("Opening socket - " + UDP_IP + ":" + str(UDP_PORT))

sock = socket.socket(socket.AF_INET,    # Internet
                     socket.SOCK_DGRAM) # UDP
sock.bind((UDP_IP, UDP_PORT))

print("DONE")

while True:
    data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
    print("Message received, size: " + str(len(data)))
    #print("---")
    #print(data)
    #print("---")
    parse_message(data)
