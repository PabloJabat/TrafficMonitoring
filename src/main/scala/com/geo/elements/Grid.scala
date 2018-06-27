package com.geo.elements

import com.geo.math.Formulas._
import math.Pi

class Grid(box: BoxLimits, resolution: Double, clearance: Double = 50, radius: Double = 30) extends Serializable {

  def computeLatDivisions: Int = {

    val latitudeDifference = box.maxLat - box.minLat
    val latitudeResolution = metersToDegrees(resolution)

    (latitudeDifference/latitudeResolution).ceil.toInt

  }

  def computeLonDivisions: Int = {

    val longitudeDifference = box.maxLon - box.minLon
    val longitudeResolution = metersToDegrees(resolution)

    (longitudeDifference/longitudeResolution).ceil.toInt

  }

  def clearanceBoxHasPoint(p: Point): Boolean = {

    val R = 6371e3
    val degreeClearance = clearance*360/(2*Pi*R)

    val latTest = p.lat >= box.minLat + degreeClearance && p.lat <= box.maxLat - degreeClearance
    val lonTest = p.lon >= box.minLon + degreeClearance && p.lon <= box.maxLon - degreeClearance

    val test = latTest && lonTest
    test

  }

  def hasPoint(p:Point): Boolean = {

    val latTest = p.lat >= box.minLat && p.lat <= box.maxLat
    val lonTest = p.lon >= box.minLon && p.lon <= box.maxLon

    val test = latTest && lonTest
    test

  }

  def hasWay(w: Way): Boolean = {

    w.points.exists(p => hasPoint(p))

  }

  def indexPointInCell(p: Point): String = {

    val latTest = (i: Int) => (p.lat >= ((i - 1) * latResolution + box.minLat)) && (p.lat <= (i * latResolution + box.minLat))
    val lonTest = (j: Int) => (p.lon >= ((j - 1) * lonResolution + box.minLon)) && (p.lon <= (j * lonResolution + box.minLon))

    val latIndex = latIndexes.filter(latTest)
    val lonIndex = lonIndexes.filter(lonTest)

    if (latIndex.isEmpty || lonIndex.isEmpty) "NaN-NaN" else latIndex.head + "-" + lonIndex.head

  }

  def indexPoint(p: Point): List[String] = {

    val increment = metersToDegrees(radius)

    val a = new Point(p.lat + increment, p.lon - increment)
    val b = new Point(p.lat + increment, p.lon + increment)
    val c = new Point(p.lat - increment, p.lon - increment)
    val d = new Point(p.lat - increment, p.lon + increment)

    val listPoints = List(a, b, c, d)

    listPoints
      .map(indexPointInCell)
      .distinct
      .filter(_ != "NaN-NaN")

  }

  def indexSegment(s: Segment): List[String] = {

    List(indexPointInCell(s.a), indexPointInCell(s.b))
      .distinct

  }

  def indexWay(w: Way): List[String] = {

    w.points
      .map(indexPointInCell)
      .filter(_ != "NaN-NaN")
      .distinct

  }

  def getCellCoordinates(index: String): List[Point] = {

    val pattern = "([0-9]+)-([0-9]+)".r
    val pattern(lat,lon) = index
    val (latIndex,lonIndex) = (lat.toInt,lon.toInt)

    val minLat = (latIndex - 1) * latResolution + box.minLat
    val maxLat = latIndex * latResolution + box.minLat
    val minLon = (lonIndex - 1) * lonResolution + box.minLon
    val maxLon = lonIndex * lonResolution + box.minLon

    val a = new Point(minLat,minLon)
    val b = new Point(minLat,maxLon)
    val c = new Point(maxLat,maxLon)
    val d = new Point(maxLat,minLon)

    List(a,b,c,d,a)

  }

  val latDivisions: Int = computeLatDivisions

  val lonDivisions: Int = computeLonDivisions

  val latResolution: Double = (box.maxLat-box.minLat)/latDivisions

  val lonResolution: Double = (box.maxLon-box.minLon)/lonDivisions

  val latIndexes: List[Int] = (1 to latDivisions).toList

  val lonIndexes: List[Int] = (1 to lonDivisions).toList

}