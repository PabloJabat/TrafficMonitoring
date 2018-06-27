package com.geo.elements

import scala.math._

class GeoVector (val x: Double, val y: Double, val z: Double) extends Serializable {

  override def toString: String = "GeoVector(" + x + "," + y + "," + z + ")"

  def x (v: GeoVector): GeoVector = {

    new GeoVector(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x)

  }

  def * (v: GeoVector): Double = {

    x*v.x + y*v.y + z*v.z

  }

  def * (alpha: Double): GeoVector = {

    new GeoVector(x*alpha, y*alpha, z*alpha)

  }

  def + (v: GeoVector): GeoVector = {

    new GeoVector(x + v.x, y + v.y, z + v.z)

  }

  def - (v: GeoVector): GeoVector = {

    new GeoVector(x - v.x, y - v.y, z - v.z)

  }

  def modulus: Double = {

    sqrt(pow(x,2) + pow(y,2)+ pow(z,2))

  }

  def toPoint: Point= {

    val lat = asin(z).toDegrees
    val lon = atan2(y,x).toDegrees

    new Point(lat, lon)

  }

  def normalize: GeoVector = {

    new GeoVector(x/modulus, y/modulus, z/modulus)

  }

}

