### Traffic Monitoring

This repository contains a series of applications for map matching GPS points into road segments defined by OpenStreeMap.
We have two main folders inside src/main/scala/com:
- geo (contains all the objects and functions that will be used in the map matching)
- trafficmonitoring (contains three applications that uses the functionality of geo):
    - MapMatchingApp: Uses spark to map match a big file containing GPS data
    - MatchPoint: Takes one GPS point and matches is to a road
    - MonitoringApp: Takes the output of MapMatchingApp and outputs a geojson file with all the roads in a color code
    depending on the traffic in each road in a given time frame