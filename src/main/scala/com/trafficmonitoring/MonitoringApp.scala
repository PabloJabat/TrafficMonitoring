package com.trafficmonitoring

import java.io.PrintWriter
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import com.geo.data.Read._
import com.geo.data.Write._

object MonitoringApp {

  /** Monitoring App
    *
    * args:
    *   args(0): location with the data from the map matching algorithm
    *   args(1): starting minute to evaluate
    *   args(2): finishing minute to evaluate
    *
    * This app writes a file in /home/pablo/DE/GeoJSON/MonitoringResults.json containing all the data needed
    * to plot a map in geojson.io
   */

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder
      .appName(s"Traffic Monitoring")
      .master("local[*]") // spark url
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val sc = spark.sparkContext

    import spark.implicits._

    val mapDataLocation = "/home/pablo/DE/DataSets/mapcsv.csv"

    //starting minute
    val startingTime = args(1)

    //finishing minute
    val finishingTime = args(2)

    //hour, in case no arg is provided for this variable we will assign 14 by default
    val hourTime = try {args(3)} catch {case _:java.lang.ArrayIndexOutOfBoundsException => "14"}

    //We name the columns of our dataframe and get the two columns we are interested in
    val colNames = List("wayID","carID","timestamp","latitude","longitude","orientation","matched latitude","matched longitude")

    val rawData = spark
      .read
      .csv(args(0))
      .toDF(colNames: _*) //here we name the columns
      .select($"wayID".cast(IntegerType), $"carID".cast(IntegerType), $"timestamp".cast(TimestampType)) //here we change the datatype

    //We include two new columns, namely, hour and minute
    val data = rawData
      .withColumn("hour", hour($"timestamp"))
      .withColumn("minute", minute($"timestamp"))

    //We group our data according to the time range we specified and count the number of different cars in a way
    val groupedData = data
      .select($"wayID",$"carID")
      .where(($"hour"===hourTime)&&($"minute">startingTime)&&($"minute"<finishingTime))
      .distinct() //this line of code is necessary to drop repeated cars
      .groupBy($"wayID")
      .count()

    //We load the map data as a dataframe with wayID and wayData as columns
    val mapData = loadStringMapSpark(sc, spark, mapDataLocation)

    //We join both dataframes
    val joinedData = groupedData.join(mapData, "wayID")

    val RDData = joinedData.map(a => (a.getAs[Int]("wayID"),a.getAs[Long]("count"),a.getAs[String]("wayData")))

    //We show some of the results in the command line
    RDData.show(20)

    //We write the results to a file after transforming the data into geojson format
    val pw = new PrintWriter("/home/pablo/DE/GeoJSON/MonitoringResults.json")

    trafficDataToJSON(pw,RDData.collect().toList)

  }

}
