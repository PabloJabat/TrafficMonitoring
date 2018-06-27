package com.geo.math

import scala.math._
import com.geo.elements.Point
import com.geo.elements.Segment
import com.geo.elements.Way

object Formulas {

  def degreeToMeters(degreeIncr: Double): Double = {

    //We consider the earth a sphere

    val R = 6371e3

    2*Pi*R*degreeIncr/360

  }

  def metersToDegrees(metersIncr: Double): Double = {

    //We consider the earth a sphere
    val R = 6371e3

    metersIncr*360/(2*Pi*R)

  }

  def haversineFormula (a: Point, b: Point): Double = {

    val R = 6371e3

    val sigma1 = a.lat.toRadians
    val sigma2 = b.lat.toRadians
    val sigmaDelta = (b.lat-a.lat).toRadians
    val lambdaDelta = (b.lon-a.lon).toRadians

    val A = sin(sigmaDelta/2)*sin(sigmaDelta/2) + cos(sigma1)*cos(sigma2)*sin(lambdaDelta/2)*sin(lambdaDelta/2)
    val C = 2*atan2(sqrt(A), sqrt(1-A))

    R * C

  }

  def naiveBayesClassifier (p: Point, s: Segment, stdev_b: Double, stdev_deltaPhi: Double): Double = {

    val b = p.distToSegment(s)
    val deltaPhi = p.orientationDifference(s)

    pow(b,2)/pow(stdev_b,2) + pow(deltaPhi,2)/pow(stdev_deltaPhi,2)

  }

  def naiveBayesClassifier (p: Point, w: Way, stdev_b: Double, stdev_deltaPhi: Double): Double = {

    w.toSegmentsList
      .map(s => naiveBayesClassifier(p,s,stdev_b,stdev_deltaPhi))
      .min

  }

}
