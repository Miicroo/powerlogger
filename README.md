# PowerLogger
This is PowerLogger, a logging tool that measures energy consumption on Android smartphones.
Most phones are not supported, just try it and see. If it works it works.
Reading current data is taken from CurrentWidget (see https://code.google.com/p/currentwidget/)

Optimized for Samsung GT-S7275R.

Settings include:
* Milliseconds timeout (minimum = 1, no guarantees that the timeout is exact, can be more due to system calls etc)
* Change log file location
* Log/don't log running apps as well
* Delete existing log file

Log file is a csv file consisting of: date, current, voltage, power, battery percentage and processes

Note that PowerLogger cannot monitor the system when it is idle, since the system is... idle.

Currently working version 1.0.

To parse log files use battery_estimator.py
Requires python 2.7 with libraries:
dateutil (required for mpl)
matplotlib
numpy (required for mpl)
pandas (required for statsmodels)
patsy (required for statsmodels)
pyparsing (required for mpl)
pytz (required for mpl)
setuptools (required for mpl)
six (required for mpl)
statsmodels.api

Usage python battery_estimator.py --include W A V --smoothen W A V --processes y/n
W A V stands for power, current, voltage.
