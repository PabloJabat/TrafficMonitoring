package com.geo.data

import java.io._
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write
import com.geo.elements._

object Write {

  case class Empty()
  case class Geometry(geometry: Any, properties: Any = Empty(),`type`: String = "Feature")

  case class PolygonProperties(stroke: String,`stroke-width`: Int,`stroke-opacity`: Int,fill: String, `fill-opacity`: Int)
  case class LineStringProperties(stroke: String = "#555555",`stroke-width`: Int = 2,`stroke-opacity`: Int = 1, `osm-id`: String = "")
  case class MarkerProperties(`marker-color`: String,`marker-size`: String, `marker-symbol`: String = "", `timestamp`: String = "")

  case class PointGeoJSON(coordinates: List[Double],`type`: String = "Point")
  case class WayGeoJSON(coordinates: List[List[Double]],`type`: String = "LineString")
  case class BoxGeoJSON(coordinates: List[List[List[Double]]],`type`: String = "Polygon")

  implicit val formats: DefaultFormats = DefaultFormats

  private def orientedPointToJSON(p: Point, timestamp: String): String = {

    val vector = List(p.toList,p.computePointDistanceBearing(15).toList)

    pointToJSON(p,"#7e7e7e",timestamp) + "," + write(Geometry(WayGeoJSON(vector)))

  }

  private def pointToJSON (p: Point, color: String, timestamp: String): String = {

    val myProperties = MarkerProperties(color,"medium",`timestamp` = timestamp)

    write(Geometry(PointGeoJSON(p.toList), myProperties))

  }

  private def wayToJSON (w: Way, properties: Any = Empty()): String = {

    write(Geometry(WayGeoJSON(w.toListList), LineStringProperties(`osm-id` = w.osmID)))

  }

  private def boxToJSON (points: List[Point]): String = {

    val myProperties = PolygonProperties("#555555",2,1,"#555555",0)
    val listPoints = List(points.map(_.toList))
    write(Geometry(BoxGeoJSON(listPoints),properties = myProperties))

  }

  private def insertComma (lines: List[String]): List[String] = {

    lines.init.map(_ + ",") ++ List(lines.last)

  }

  private def writeToFile (pw: PrintWriter, lines: List[String]): Unit = {

    pw.println("{\"type\": \"FeatureCollection\" ,\"features\":[")

    insertComma(lines).foreach(line => pw.println(line))

    pw.println("]}")

    pw.close()

  }

  def indexedDataToJSON (pw: PrintWriter, indexedData: List[(Point,List[Way])], grid: Grid): Unit = {

    val points = indexedData
      .map(r => orientedPointToJSON(r._1, r._1.id))

    val cells = indexedData
      .map(r => r._1)
      .flatMap(p => grid.indexPoint(p))
      .map(index => grid.getCellCoordinates(index))
      .map(cell => boxToJSON(cell))

    val ways = indexedData
      .flatMap(r => r._2)
      .map(w => wayToJSON(w))

    writeToFile(pw, points ++ cells ++ ways)

  }

  def incorrectMatchesToJSON (pw: PrintWriter, indexedData: List[(Point,List[Way],(Point, Point))], grid: Grid): Unit = {

    //the first point in the Tuple2 of points is the correct one and the second is the one obtained in the MM algorithm

    val points = indexedData
      .map(r => orientedPointToJSON(r._1, r._1.id))

    val cells = indexedData
      .map(r => r._1)
      .flatMap(p => grid.indexPoint(p))
      .distinct
      .map(index => grid.getCellCoordinates(index))
      .map(cell => boxToJSON(cell))

    val ways = indexedData
      .flatMap(r => r._2)
      .distinct
      .map(w => wayToJSON(w))

    val correctPoints = indexedData
      .map(r => r._3._1)
      .map(p => pointToJSON(p,"#13a71a",p.id))

    val matchedPoints = indexedData
      .map(r => r._3._2)
      .map(p => pointToJSON(p,"#131aa7",p.id))

    //.map(w => wayToJSON(w))

    writeToFile(pw, points ++ cells ++ ways ++ correctPoints ++ matchedPoints)

  }

  def resultsToJSON (pw: PrintWriter, results: List[(Point, Point, List[Way])], grid: Grid): Unit = {

    val points = results
      .map(r => orientedPointToJSON(r._1, r._1.id))

    val matchedPoints = results
      .map(r => pointToJSON(r._2, "#a71313",r._2.id))

    val cells = results
      .map(r => r._1)
      .flatMap(p => grid.indexPoint(p))
      .distinct
      .map(index => grid.getCellCoordinates(index))
      .map(cell => boxToJSON(cell))

    val ways = results
      .flatMap(r => r._3)
      .distinct
      .map(w => wayToJSON(w))

    writeToFile(pw, points ++ cells ++ ways ++ matchedPoints)

  }

  def cellsToJSON (pw: PrintWriter, waysIndexed: List[(String, List[Way])], grid: Grid): Unit = {

    val cells = waysIndexed
      .map(w => w._1)
      .map(index => grid.getCellCoordinates(index))
      .map(cell => boxToJSON(cell))

    val ways = waysIndexed
      .flatMap(w => w._2)
      .map(w => wayToJSON(w))

    writeToFile(pw, cells ++ ways)

  }

  def trainingSetToJSON (pw: PrintWriter, trainingSet: List[(Point,(String,Point))]): Unit = {

    val points = trainingSet
      .map(ts => orientedPointToJSON(ts._1,ts._1.id))

    val matchedPoints = trainingSet
      .map(ts => pointToJSON(ts._2._2,"#a71313",ts._1.id))

    writeToFile(pw, points ++ matchedPoints)

  }

  def pointsToJSON (pw: PrintWriter, lstPoints: List[(Point,Point)]): Unit = {

    val originalPoints = lstPoints.map(p => orientedPointToJSON(p._1,p._1.id))

    val matchedPoints = lstPoints.map(p =>  pointToJSON(p._2,"#a71313",p._2.id))

    writeToFile(pw, originalPoints ++ matchedPoints)

  }

}
