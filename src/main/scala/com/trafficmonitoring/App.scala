package com.trafficmonitoring

import java.io.PrintWriter

import org.apache.spark.sql.SparkSession
import com.geo.elements._
import com.geo.algorithms.MapMatching._
import com.geo.data.Read._
import com.geo.data.Transform._
import com.geo.data.Write._

object App {

  def main(args: Array[String]): Unit = {

    println("Traffic Monitoring")

    val spark = SparkSession.builder
      .appName(s"Traffic Monitoring")
      .master("local[*]") // spark url
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val sc = spark.sparkContext

    import spark.implicits._

    println("======================================")
    println("|         Simple Map Matching        |")
    println("======================================")

    //We first set the region on the map in which we want to perform map matching

    val osmBox = BoxLimits(40.647, 40.643, 22.937, 22.933)

    val myGrid = new Grid(osmBox,150,100)

    println("Number of latitude divisions: " + myGrid.latDivisions)

    println("Number of longitude divisions: " + myGrid.lonDivisions)

    //We load and index the GPS data

    val gpsData = loadGPSPointsSpark(sc, "/home/pablo/DE/DataSets/testDataSets/input_gps_08122017.csv")

    val gpsDataIndexed = filterIndexGPSPointsSpark(gpsData, myGrid)

    //We load and index and group the Ways data

    val waysData = loadMapSpark(sc, "/home/pablo/DE/DataSets/testDataSets/mapcsv.csv")

    println("number of ways " + waysData.count())

    val waysDataIndexed = filterIndexMapSpark(waysData, myGrid)

    //Now we are going to write a file with some sample data of ways already indexed

    waysDataIndexed.persist()

    val cells = waysDataIndexed.take(10).toList

    val pw1 = new PrintWriter("/home/pablo/DE/GeoJSON/cellWaysRDD.json")

    cellsToJSON(pw1,cells,myGrid)

    waysDataIndexed.unpersist()

    //The last step is to join both RDDs

    val mergedData = joinIndexedMapPointsSpark(gpsDataIndexed, waysDataIndexed)

    //Now we want to visualize some of the inputs for the Map Matching algorithm

    mergedData.persist()

    val sample = mergedData.collect.toList

    val pw2 = new PrintWriter("/home/pablo/DE/GeoJSON/mergedDataRDD.json")

    indexedDataToJSON(pw2,sample,myGrid)

    mergedData.unpersist()

    //The last step is to pass the resulting RDD to the map matching algorithm

    val matchedData = mergedData
      .map{case (p: Point, waysLst: List[Way]) => (naiveBayesClassifierMM(p,waysLst,1,1),p,waysLst)}
      .map{case ((way, new_p), p, ways) => (way, p, new_p, ways)}

    //Once we carry out the Map Matching computation we write the results in a csv

    matchedData.persist()

    matchedData.map{case (way, p, new_p, _) => (way.osmID, p.id, p.lat, p.lon, p.orientation, new_p.lat, new_p.lon)}
      .toDF("wayID","pointID","latitude","longitude","orientation","matched latitude","matched longitude")
      .coalesce(1).write.csv("/home/pablo/DE/DataSets/testDataSets/results")

    matchedData.unpersist()

    //We can also write some results in geojson format for visualization purposes

    val someResults = matchedData.collect.toList

    val pw3 = new PrintWriter("/home/pablo/DE/GeoJSON/MMResultsRDD.json")

    val jsonResults = someResults
      .map(a => (a._2, a._3, a._4))

    resultsToJSON(pw3, jsonResults, myGrid)

  }

}