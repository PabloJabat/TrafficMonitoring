### Traffic Monitoring

This repository contains a series of applications for map matching GPS points into road segments defined by OpenStreetMap.
We have two main folders inside src/main/scala/com:
- geo (contains all the objects and functions that will be used in the map matching)
- trafficmonitoring (contains three applications that uses the functionality of geo):
    - MapMatchingApp: Uses spark to map match a big file containing GPS data
    - MatchPoint: Takes one GPS point and matches is to a road
    - MonitoringApp: Takes the output of MapMatchingApp and outputs a geojson file with all the roads in a color code
    depending on the traffic in each road in a given time frame
    
    ####MatchPoint
    
    In order to make this app work you will first need to download the map from OpenStreetMap. To do that you can use 
    The data must be in .csv format. The following repository provides a simple command to download the
    map: 
    
    Repository: https://github.com/PabloJabat/OSMDataRetriever
    Commad: `./getMapDataCSV`
    
    You will need to set the size of the box containing the area of roads that you want to consider in the map matching.
    Make sure that the area contains the point that you want to map match. You will need to set all the parameters of the 
    map matching in the file src/main/resources/matchpoint.conf. There you will provide the data point, the grid area and 
    other settings. Once you are done with the .conf file you will be ready to run the code using the following command: 
    
    `scala -classpath out/artifacts/MatchPoint.jar MatchApp <map_path>`
    
    The last step is to visualize the results using <http://geojson.io/>. There you will be able to upload the output and 
    see the result of the map matching.