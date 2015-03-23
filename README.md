# powerlogger
This is powerlogger, a logging tool that measures energy consumption on Android smartphones.
Most phones are not supported, just try it and see. If it works it works.
Reading current data is taken from CurrentWidget (see https://code.google.com/p/currentwidget/)

Optimized for Samsung GT-S7275R.

Settings include:
* Milliseconds timeout (minimum = 1, no guarantees that the timeout is exact, can be more due to system calls etc)
* Change log file location
* Log/don't log running apps as well
* Delete existing log file

Log file is a csv file consisting of: date, current, voltage, power, battery percentage and processes
