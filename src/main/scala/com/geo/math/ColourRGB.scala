package com.geo.math

class ColourRGB(val r: Double, val g: Double, val b: Double) {

  // parameters ranges:
  // r : [0,255]
  // g : [0,255]
  // b : [0,255]

  override def toString: String = {

    this.toHexadecimal + " - (" + r + "," + g + "," + b + ")"

  }

  def toHexadecimal: String = {

    // this function rounds the r,g,b values to the lower bound

    var r_h = ""

    var g_h = ""

    var b_h = ""

    if (r < 16) {

      r_h = "0" + r.toInt.toHexString

    } else {r_h = r.toInt.toHexString}

    if (g < 16) {

      g_h = "0" + g.toInt.toHexString

    } else {g_h = g.toInt.toHexString}

    if (b < 16) {

      b_h = "0" + b.toInt.toHexString

    } else {b_h = b.toInt.toHexString}

    "#"+r_h+g_h+b_h

  }

  def interpolate(c: ColourRGB, lambda: Double): ColourRGB = {

    val new_r = this.r * lambda + (1 - lambda) * c.r

    val new_g = this.g * lambda + (1 - lambda) * c.g

    val new_b = this.b * lambda + (1 - lambda) * c.b

    new ColourRGB(new_r, new_g, new_b)

  }

  def toHSV: ColourHSV = {

    var h, s, v = 0.0

    val components = List(r,g,b).map(a => a / 255)

    val min = components.min

    val max = components.max

    v = max

    val delta = max - min

    if (max != 0) {

      s = delta / max

      if (r > g && r > b) {

        h = (g - b) / (255 * delta) * 60

        if (h < 0) h = h + 360

        new ColourHSV(h, s, v)

      } else if (g > b && g > r) {

        h = ((b - r) / (255 * delta) + 2) * 60

        if (h < 0) h = h + 360

        new ColourHSV(h, s, v)

      } else if (b > r && b > g) {

        h = ((r - g) / (255 * delta) + 4) * 60

        if (h < 0) h = h + 360

        new ColourHSV(h, s, v)

      } else {

        //This code will be executed just when r=g=b

        h = 0

        new ColourHSV(h, s, v)

      }

    }

    else {

      s = 0

      h = -1

      println("Warning: r=g=b so v is undefined")

      new ColourHSV(h,s,v)

    }

  }

}
