package com.geo.elements

import scala.math._

class Segment (val a: Point, val b: Point, val osmID: String) extends Serializable {

  // a has to be the starting point of the segment and b the ending point
  // dir is the angle towards the vertex b. 0 degrees is North, 90 degrees is East, etc.
  // dir is a variable with the forward azimuth

  def this(a: Point,b: Point) = {

    this(a,b,"")

  }

  def isSegmentAligned (p: Point, t: Double): Boolean = {

    //parameter t is the tolerance in the difference of orientations

    val difference = orientationDifference(p) % 180

    if ((difference <= t ) || (difference >= 180 - t )) true else false

  }

  def distToPoint (p: Point): Double = {

    p.distToSegment(this)

  }

  def initialBearing: Double = {

    val lat1 = a.lat.toRadians
    val lon1 = a.lon.toRadians
    val lat2 = b.lat.toRadians
    val lon2 = b.lon.toRadians

    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(lon2 - lon1)
    val y = sin(lon2 - lon1) * cos(lat2)

    (atan2(y,x).toDegrees + 360) % 360

  }

  def finalBearing: Double = {

    (new Segment(b,a).initialBearing+180)%360

  }

  def orientationDifference (p: Point): Double = {

    val difference = abs(p.orientation - initialBearing)
    min(difference, 360 - difference)

  }

  def getPointInSegment (coordinate: Double, lat: Boolean): Point = {

    //if lat is true then the coordinate provided must be a latitude and if lat is false coordinate must be a longitude instead


    if (lat) {

      val num = sin(coordinate.toRadians) - a.toGeoVector.z
      val den = b.toGeoVector.z - a.toGeoVector.z

      val lambda = num / den

      val geoPoint = b.toGeoVector * lambda + a.toGeoVector * (1 - lambda)

      geoPoint.toPoint

    } else {

      val num = a.toGeoVector.y - a.toGeoVector.x * tan(coordinate.toRadians)
      val den = tan(coordinate.toRadians) * (b.toGeoVector.x - a.toGeoVector.x) - (b.toGeoVector.y - a.toGeoVector.y)

      val lambda = num / den

      val geoPoint = b.toGeoVector * lambda + a.toGeoVector * (1 - lambda)

      geoPoint.toPoint

    }


  }

  def getPointInSegment (lambda: Double): Point = {

    val geoPoint = b.toGeoVector * lambda + a.toGeoVector * (1 - lambda)

    geoPoint.toPoint

  }

}