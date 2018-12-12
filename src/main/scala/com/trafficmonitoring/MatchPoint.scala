package com.trafficmonitoring

import com.typesafe.config.ConfigFactory
import java.io.PrintWriter
import com.geo.elements._
import com.geo.data.Read._
import com.geo.data.Transform._
import com.geo.data.Write._
import com.geo.algorithms.MapMatching._

object MatchPoint {

  def main(args: Array[String]): Unit = {

    val applicationConf = ConfigFactory.load("matchpoint.conf")

    val p_lat = applicationConf.getDouble("point.lat")

    val p_lon = applicationConf.getDouble("point.lon")

    val p_angle = applicationConf.getDouble("point.angle")

    val maxLat = applicationConf.getDouble("grid.area.maxLat")

    val minLat = applicationConf.getDouble("grid.area.minLat")

    val maxLon = applicationConf.getDouble("grid.area.maxLon")

    val minLon = applicationConf.getDouble("grid.area.minLon")

    val cellway_output = applicationConf.getString("output.cellways")

    val mmresult_output = applicationConf.getString("output.mmresult")

    val p = new Point(p_lat, p_lon, p_angle)

    //We create the grid that we are going to use
    val osmBox = BoxLimits(maxLat, minLat, maxLon, minLon)

    val myGrid = new Grid(osmBox,200)

    //We load the map with to a List[Way]
    val waysData = loadMap(args(0))

    //We index the ways using the grid we just created
    val waysDataIndexed = filterIndexMap(waysData, myGrid)

    //We get the indexes of our point and print them
    val pointIndexes = myGrid.indexPoint(p).distinct

    //We filter the necessary ways
    val waysOfPoint = getWaysOfIndexes(waysDataIndexed, pointIndexes)

    println("Total number of ways: " + waysOfPoint.length)

    //We write to geojson all the ways that will we introduced in the MM algorithm
    val waysToJSON = getIndexedWaysOfIndexes(waysDataIndexed, pointIndexes)

    val pw1 = new PrintWriter(cellway_output)

    cellsToJSON(pw1, waysToJSON, myGrid)

    //We call the map matching algorithm
    val result = naiveBayesClassifierMM(p, waysOfPoint,1,1)

    //We write the result to geojson
    val pw2 = new PrintWriter(mmresult_output)

    resultsToJSON(pw2, List((p, result._2, waysOfPoint)), myGrid)

    println("OSM Id: "+ result._1.osmID)

    }

}
