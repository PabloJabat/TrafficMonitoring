package com.trafficmonitoring

import java.io.PrintWriter

import com.typesafe.config.ConfigFactory

import org.apache.spark.sql.SparkSession
import com.geo.elements._
import com.geo.algorithms.MapMatching._
import com.geo.data.Read._
import com.geo.data.Transform._
import com.geo.data.Write._

object MapMatchingApp {

  /** MapMatchingApp
    *
    * args:
    *
    * All the arguments needed for this app have to be set in application.conf
    *
    */

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder
      .appName(s"Traffic Monitoring")
      .master("local[*]") // spark url
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val sc = spark.sparkContext

    import spark.implicits._

    println("======================================")
    println("|          Map Matching App          |")
    println("======================================")

    //We load all the parameters needed in the application fromm the configuration file

    val applicationConf = ConfigFactory.load("application.conf")

    val mapMatchingConf = ConfigFactory.load("mapmatching.conf")

    val maxLat = mapMatchingConf.getDouble("boxlimits.maxLat")

    val minLat = mapMatchingConf.getDouble("boxlimits.minLat")

    val maxLon = mapMatchingConf.getDouble("boxlimits.maxLon")

    val minLon = mapMatchingConf.getDouble("boxlimits.minLon")

    val resolution = mapMatchingConf.getDouble("grid.resolution")

    val clearance = mapMatchingConf.getDouble("grid.clearance")

    val radius = mapMatchingConf.getDouble("grid.radius")

    val stdev_deltaPhi = mapMatchingConf.getDouble("stdev.deltaPhi")

    val stdev_b = mapMatchingConf.getDouble("stdev.b")

    val gpsDataPath = applicationConf.getString("data.gps")

    val mapDataPath = applicationConf.getString("data.map")

    val outputPath = applicationConf.getString("data.output")

    //We first set the region on the map in which we want to perform map matching

    val osmBox = BoxLimits(maxLat, minLat, maxLon, minLon)

    val myGrid = new Grid(osmBox, resolution, clearance, radius)

    println("Number of latitude divisions: " + myGrid.latDivisions)

    println("Number of longitude divisions: " + myGrid.lonDivisions)

    //We load and index the GPS data

    val gpsData = loadGPSPointsSpark(sc, gpsDataPath)

    val gpsDataIndexed = filterIndexGPSPointsSpark(gpsData, myGrid)

    //We load and index and group the Ways data

    val waysData = loadMapSpark(sc, mapDataPath)

    val waysDataIndexed = filterIndexMapSpark(waysData, myGrid)

    //Now we are going to write a file with some sample data of ways already indexed

    waysDataIndexed.persist()

    val cells = waysDataIndexed.take(10).toList

    val pw1 = new PrintWriter(outputPath + "/cellWaysRDD.json")

    cellsToJSON(pw1,cells,myGrid)

    println("cellWaysRDD.json created")

    waysDataIndexed.unpersist()

    //The last step is to join both RDDs

    val mergedData = joinIndexedMapPointsSpark(gpsDataIndexed, waysDataIndexed)

    //Now we want to visualize some of the inputs for the Map Matching algorithm

    mergedData.persist()

    val sample = mergedData.collect.toList

    val pw2 = new PrintWriter(outputPath + "/mergedDataRDD.json")

    indexedDataToJSON(pw2,sample,myGrid)

    println("mergedDataRDD.json created")

    mergedData.unpersist()

    //The last step is to pass the resulting RDD to the map matching algorithm

    val matchedData = mergedData
      .map{case (p: Point, waysLst: List[Way]) => (naiveBayesClassifierMM(p,waysLst,stdev_b,stdev_deltaPhi),p,waysLst)}
      .map{case ((way, new_p), p, ways) => (way, p, new_p, ways)}

    //Once we carry out the Map Matching computation we write the results in a csv

    matchedData.persist()

    matchedData.map{case (way, p, new_p, _) => (way.osmID, findCarID(p.id), findTimestamp(p.id), p.lat, p.lon, p.orientation, new_p.lat, new_p.lon)}
      .toDF("wayID","carID","timestamp","latitude","longitude","orientation","matched latitude","matched longitude")
      .coalesce(1).write.csv(outputPath + "/MMresults")

    matchedData.unpersist()

    println("MMresults folder containing matched data was created")

    //We can also write some results in geojson format for visualization purposes

    val someResults = matchedData.collect.toList

    val pw3 = new PrintWriter(outputPath + "/MMResultsRDD.json")

    val jsonResults = someResults
      .map(a => (a._2, a._3, a._4))

    resultsToJSON(pw3, jsonResults, myGrid)

    println("MMResultsRDD.json created")

    println("Map Matching App successfully finished")

  }

}