package com.geo.elements

import scala.math._
import com.geo.math.Formulas._

class Point (val lat: Double, val lon: Double, val orientation: Double, val id: String) extends Serializable{

  //if we don't need information about the orientation we can use this constructor
  def this(lat: Double, lon: Double, id: String) = {

    this(lat,lon,-1,id)

  }

  //if the point has no direction then it is assigned a -1
  def this(lat: Double, lon: Double) = {

    this(lat,lon,-1,"")

  }

  //if the point doesn't need an id, then, this variable takes the value or ''
  def this(lat: Double, lon: Double, dir: Double) = {

    this(lat,lon,dir,"")

  }

  //Type transformation methods
  override def toString: String = "Point(" + lat + "," + lon + ")"

  override def hashCode(): Int = {

    val pattern = "([0-9])".r

    pattern.findFirstIn(id).get.toInt

  }

  override def equals(that: Any): Boolean = {

    that match {

      case that: Point => (this.lat == that.lat) && (this.lon == that.lon) && (this.id == that.id)
      case _ => false

    }

  }

  def toList: List[Double] = {

    //we invert the order of lat, lon because is the format supported by geojson

    List(lon, lat)
  }

  def toGeoVector: GeoVector = {

    val x = cos(lat.toRadians)*cos(lon.toRadians)
    val y = cos(lat.toRadians)*sin(lon.toRadians)
    val z = sin(lat.toRadians)

    new GeoVector(x,y,z)

  }

  //Distance and projection methods
  def distToPoint (p: Point): Double = {

    haversineFormula(this,p)

  }

  def distToSegment (s: Segment): Double = {

    distToPoint(projectToSegment(s))

  }

  def projectionInSegment (s: Segment): Boolean = {

    val a = s.a.toGeoVector
    val b = s.b.toGeoVector
    val c = this.toGeoVector

    val u = (a x b).normalize

    val u_a = (u x a).normalize
    val u_b = (b x u).normalize

    if ((c * u_a).signum == 0) false
    else if ((c * u_b).signum == 0) false
    else (c * u_a).signum == (c * u_b).signum

  }

  def projectToSegment (s: Segment): Point = {

    if (projectionInSegment(s)) {

      val x = this.toGeoVector
      val a = s.a.toGeoVector
      val b = s.b.toGeoVector

      val v = a x b
      val u = v.normalize

      val x_p = x - u * (x * u)
      val x_pp = x_p.normalize

      x_pp.toPoint

    } else {

      if (distToPoint(s.a) < distToPoint(s.b)) s.a else s.b

    }

  }

  //Orientation computations methods
  def isPointAligned (s: Segment, t: Double): Boolean = {

    //parameter t is the tolerance in the difference of orientations

    val difference = orientationDifference(s) % 180

    if ((difference <= t ) || (difference >= 180 - t )) true else false

  }

  def orientationDifference (s: Segment): Double = {

    val difference = abs(orientation - s.initialBearing)
    min(difference, 360 - difference)

  }

  def computePointDistanceBearing (distance: Double): Point = {

    val R = 6371e3
    val latitude = asin(sin(lat.toRadians) * cos(distance / R) + cos(lat.toRadians) * sin(distance / R) * cos(orientation.toRadians))
    val longitude = lon.toRadians + atan2(sin(orientation.toRadians) * sin(distance / R) * cos(lat.toRadians), cos(distance / R) - sin(lat.toRadians) * sin(latitude))

    new Point(latitude.toDegrees, longitude.toDegrees)

  }

  //Indexing methods
  def isInGridCell(osmBox: (Double, Double, Double, Double), clearance: Double = 0): Boolean = {

    val R = 6371e3
    val degreeClearance = clearance*360/(2*Pi*R)

    val minOsmLat = osmBox._1
    val maxOsmLat = osmBox._2
    val minOsmLon = osmBox._3
    val maxOsmLon = osmBox._4

    val latTest = lat >= minOsmLat + degreeClearance && lat <= maxOsmLat - degreeClearance
    val lonTest = lon >= minOsmLon + degreeClearance && lon <= maxOsmLon - degreeClearance

    val test = latTest && lonTest
    test

  }

  //Point operators
  def - (p:Point): (Double, Double) = {

    (lat - p.lat, lon - p.lon)

  }

}
