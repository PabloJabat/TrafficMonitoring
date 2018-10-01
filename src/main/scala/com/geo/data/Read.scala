package com.geo.data

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.sql._
import com.geo.elements._
import com.geo.data.Transform._

import scala.io.Source

object Read {

  def loadMapSpark(sc: SparkContext, mapPath: String): RDD[Way] = {

    val mapData = sc.textFile(mapPath)

    mapData.map(_.split("-")).map(line => new Way(lineStringToPointArray(line(1)).toList, line(0), stringToBoolean(line(2))))

  }

  def loadStringMapSpark(sc: SparkContext, spark: SparkSession, mapPath: String): DataFrame = {

    //This function loads the way data in string format, both the wayID and the wayData containing the coordinates

    import spark.implicits._

    val mapData = sc.textFile(mapPath)

    mapData.map(_.split("-")).map(a => (a(0),a(1))).toDF("wayID","wayData")

  }

  def loadGPSPointsSpark(sc: SparkContext, gpsDataPath: String): RDD[Point] = {

    def pointExtraction(list: List[String]): Point = {
      //We first put the 4th entry as it is the latitude and we want the LatLon array
      new Point(list(3).toDouble, list(2).toDouble, list(6).toDouble, list.head + " " + list(1))

    }

    val pattern = """([^";]+)""".r

    sc.textFile(gpsDataPath)
      .map(line => pattern.findAllIn(line).toList)
      .map(pointExtraction)

  }

  def loadMap(mapPath: String): List[Way] = {

    val osmData = Source.fromFile(mapPath).getLines().toList

    osmData.map(_.split("-")).map(a => new Way(lineStringToPointArray(a(1)).toList, a(0), stringToBoolean(a(2))))

  }

  def loadResultsData(matchedGPSDataPath: String): List[(Point,(String,Point))] = {

    def extractPoints(line: Array[String]): (Point,(String,Point)) = {

      val originalPoint = new Point(line(2).toDouble, line(3).toDouble, line(4).toDouble, line(1))

      val matchedPoint = new Point(line(5).toDouble, line(6).toDouble)

      (originalPoint,(line(0),matchedPoint))

    }

    val osmData = Source.fromFile(matchedGPSDataPath).getLines().toList

    osmData.map(_.split(",")).map(extractPoints)

  }

}
