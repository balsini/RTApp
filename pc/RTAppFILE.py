#!/usr/bin/python

from __future__ import print_function

import subprocess
import csv
import os
import random
import math
import matplotlib.pyplot as plt

VERSION = "0.1"

#------------------------------------------

print("RTAppPC v" + VERSION)

#------------------------------------------

columns = 0 # to be initialized
rows = 0    # to be initialized

max_x = 0

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
  #print("Plotting data for Task_" + str(task_id))
  ax = axes[task_id / columns][task_id % columns]
  
  if len(task_data) == 0 :
    return
  
  x, y = generate_axes(task_data)
  
  global max_x
  if max_x < len(x) :
    max_x = len(x)
  
  plt.axis([0,max_x,0,1])
  #ax.clear()
  ax.plot(x, y)
  ax.set_title("Task_" + str(task_id))
  ax.set_xlabel("Response Time (ms)")
  ax.set_ylabel("Samples distribution")
  plt.draw()
  #plt.pause(0.0001)
  #print("DONE")

def getAdbFile(filename, destination) :
  cmd = "adb pull " + filename + " " + destination
  s = subprocess.check_output(cmd.split())
  
#------------------------------------------

go_root = subprocess.check_output("adb root".split())

app_folder = "/data/user/0/it.sssup.retis.alessiobalsini.rtapp/files"
cmd = "adb shell ls " + app_folder
ls_results = subprocess.check_output(cmd.split()).splitlines()

print("Number of plots: {}".format(len(ls_results)))

columns = int(math.ceil(math.sqrt(len(ls_results)))) # to be initialized
rows = int(math.ceil(float(len(ls_results)) / columns))    # to be initialized

print("Columns and rows: {} {}".format(columns, rows))

#####################

files = []

f, axes = plt.subplots(rows, columns, sharey=True)

plt.title("Cumulative distribution")
plt.axis([0,1,0,1])
plt.ion()
plt.show()

#####################


count = 0
for i in ls_results :
  filename = "/tmp/" + i + ".csv"
  files.append(filename)
  print("Downloading files from ADB...", end="")
  getAdbFile(os.path.join(app_folder, i), filename)
  print("Done")
  print("Plotting file [" + filename + "]")
  with open(filename, 'rb') as csvfile:
    spamreader = csv.reader(csvfile, delimiter=',', quotechar='|')
    for row in spamreader:
      data = [float(numeric_string) for numeric_string in row[0:-1]]
      plot_data(count, data)
  count = count + 1

v = input("Click enter to quit...")
