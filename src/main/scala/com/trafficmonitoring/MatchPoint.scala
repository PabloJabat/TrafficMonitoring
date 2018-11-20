package com.trafficmonitoring

import java.io.PrintWriter
import com.geo.elements._
import com.geo.data.Read._
import com.geo.data.Transform._
import com.geo.data.Write._
import com.geo.algorithms.MapMatching._

object MatchPoint {

  def main(args: Array[String]): Unit = {

    val p = new Point(40.6444883333, 22.9351966667, 3.2999999523)

    //We create the grid that we are going to use
    val osmBox = BoxLimits(40.65, 40.64, 22.94, 22.93)

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

    val pw1 = new PrintWriter("/home/pablo/DE/GeoJSON/CellWays.json")

    cellsToJSON(pw1, waysToJSON, myGrid)

    //We call the map matching algorithm
    val result = naiveBayesClassifierMM(p, waysOfPoint,1,1)

    //We write the result to geojson
    val pw2 = new PrintWriter("/home/pablo/DE/GeoJSON/MMResult.json")

    resultsToJSON(pw2, List((p, result._2, waysOfPoint)), myGrid)

    println("OSM Id: "+ result._1.osmID)

    }

}
