from numpy import trapz
from Tkinter import *
import matplotlib.pyplot as plt
import os
import sys
import statsmodels.api as sm
import time as tm

def setArgs(arg, data):
   if len(data) > 0:
      if arg == "include":
         global TO_INCLUDE
         TO_INCLUDE = data
      elif arg == "smoothen":
         global TO_SMOOTHEN
         TO_SMOOTHEN = data
      elif arg == "processes":
         global INCLUDE_PROCESSES
         if data[0] == "y":
            INCLUDE_PROCESSES = True
         elif data[0] == "n":
            INCLUDE_PROCESSES = False
            
def parseArgs(argv):
   argv = argv[1:]
   data = []
   theArg = ""
   for arg in argv:
      if arg.startswith("--"):
         setArgs(theArg, data)
         data = []
         theArg = arg.replace("--", "")
      else:
         data.append(arg)
   setArgs(theArg, data)
   
TO_INCLUDE = ['W', 'A', 'V']
TO_SMOOTHEN = []
INCLUDE_PROCESSES = True   # Display process data or not?

parseArgs(sys.argv)

# Returns time as milliseconds number
def getTime(timeInput):
        # timeInput is on the form YYYY/MM/DD HH:MM:SS.sss (s = ms)
        # Parse the last three as milliseconds
        # Use the time (except ms) to get an int since 1970...
        # Add milliseconds at the end of the time.

        ms = int(timeInput[-3:])
        timeInput = timeInput[:-4]
        base = int(tm.mktime(tm.strptime(timeInput, '%Y/%m/%d %H:%M:%S')))

        return base*1000+ms

# Returns difference between timestamp and baseTime (milliseconds number)       
def getMilliseconds(newTime, baseTime):
        # newTime is on the form YYYY/MM/DD HH:MM:SS.sss (s = ms)       
        # milliseconds is the different between rawTime and baseTime. 
        rawTime = getTime(newTime)
        return rawTime-baseTime


# Launches a GUI where the user picks a subset of processes to look at
def getProcesses(processes):

   def onclick():
      global items # Global to return after master is destroyed
      items = map(int, listbox.curselection())
      master.destroy() # Close GUI

   # Setup GUI
   master = Tk()
   master.wm_title("Select processes")
   frame = Frame(master)
   scrollbar = Scrollbar(frame, orient=VERTICAL)
   listbox = Listbox(frame, yscrollcommand=scrollbar.set, selectmode=MULTIPLE)
   scrollbar.config(command=listbox.yview)
   scrollbar.pack(side=RIGHT, fill=Y)
   listbox.pack(side=LEFT, fill=BOTH, expand=1)
   frame.pack(fill=BOTH, expand=YES)

   for item in processes:
      listbox.insert(END, item)

   b = Button(master, text="OK", command=onclick)
   b.pack()

   mainloop() # GUI loop

   # Translate selected index to process name
   chosenPs = []
   for i in items:
      chosenPs.append(processes[i])
   return chosenPs

# Prompts user to pick processes, and returns a list with (name, [processdata]).
def doProcessStuff(processes):   
   allProcesses = [] # Parse as process list
   for instantProcess in processes:
      for process in instantProcess:
         if not (process in allProcesses) and len(process) > 1:
            allProcesses.append(process)
   allProcesses.sort()
   chosen = getProcesses(allProcesses) # Get chosen process names from user GUI

   data = []
   graphVal = 1
   errorVal = 0
   for process in chosen:
      processData = []
      skip = True
      for instantProcess in processes:
         if skip:
            skip = False
            continue # Skip first value to match x-axis
         
         if process in instantProcess:
            processData.append(graphVal) # Present, add graph high value
         else:
            processData.append(errorVal)
            
      data.append((process, processData))
      graphVal += 0.5
   return data

def plot(x, y, smooth=False):
   if smooth:
      lowess = sm.nonparametric.lowess(y, x, frac=0.1)
      plt.plot(lowess[:, 0], lowess[:, 1])
   else:
      plt.plot(x, y)

#---------------------------------------------
#=============================================
#---------------------------------------------
# The main part of the script

# The variables
dataArray = []
time = []
power = []
current = []
voltage = []
processes = [[]]

firstLine = True
for line in open("powerlogger.log"):
   if firstLine:
      firstLine = False
      continue

   dataHolder = line.split(',')

   #if a row has a length less than 5, something went wrong and the
   #row cannot be analyzed, so its data point is thrown out
   if len(dataHolder) < 5:
      continue

   dataArray.append(dataHolder)

baseTimeHolder = dataArray[0][0]
baseTime = getTime(baseTimeHolder)

for entry in dataArray:
   timeHolder = entry[0]
   currentHolder = entry[1]
   voltageHolder = entry[2]
   powerHolder = entry[3]
   processHolder = entry[5]

   current.append(float(currentHolder)/1000)
   voltage.append(float(voltageHolder))
   power.append(float(powerHolder))
   time.append((getMilliseconds(timeHolder, baseTime)))
   processes.append(processHolder.split(';'))

   
if INCLUDE_PROCESSES:
   processInfo = doProcessStuff(processes)

# Calculate integral Wms
power_ms = trapz(power, x=time);
power_h = -power_ms/((60**2)*1000)

totalTime = time[len(time)-1]/1000


print "Currently used %s Wh\nThe data in the log file spanned %s minutes and %s seconds" % (power_h, totalTime/60, totalTime%60,)

hasProcessInfo = INCLUDE_PROCESSES and len(processInfo) > 0

rows = 2
if not hasProcessInfo:
   rows = 1 # Remove second subplot

legend = []
plt.rcParams.update({'font.size': 24})
plt.subplot(rows, 1, 1)
if 'W' in TO_INCLUDE:
        plot(time, power, 'W' in TO_SMOOTHEN)
        legend.append('Power   (W)')
        
if 'A' in TO_INCLUDE:
        plot(time, current, 'A' in TO_SMOOTHEN)
        legend.append('Current (A)')

if 'V' in TO_INCLUDE:
        plot(time, voltage, 'V' in TO_SMOOTHEN)
        legend.append('Voltage (V)')
        
plt.legend(legend, loc='upper left')
plt.xlabel('Time (ms)')

if hasProcessInfo: # Has info, use it to plot
   legend2 = []
   plt.subplot(2, 1, 2)
   for pname, pdata in processInfo:
      plt.plot(time, pdata)
      legend2.append(pname)
      
   plt.legend(legend2, loc='upper left')
   plt.xlabel('Time (ms)')

plt.show()
