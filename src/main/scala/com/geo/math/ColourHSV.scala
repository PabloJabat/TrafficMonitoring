package com.geo.math

class ColourHSV(val h: Double, val s: Double, val v: Double) {

  // parameters ranges:
  // h : [0,360]
  // s : [0,1]
  // v : [0,1]

  override def toString: String = {

    "(" + h + "," + s + "," + v + ")"

  }

  def interpolate(c: ColourHSV, lambda: Double): ColourHSV = {

    val new_h = this.h * lambda + (1 - lambda) * c.h

    val new_s = this.s * lambda + (1 - lambda) * c.s

    val new_v = this.v * lambda + (1 - lambda) * c.v

    new ColourHSV(new_h, new_s, new_v)

  }

  def toRGB: ColourRGB = {

    val v_s = v * 255

    val p = v_s * (1.0 - s)

    val q = v_s * (1.0 - s*((h % 60) / 60))

    val t = v_s * (1.0 - s*(1.0-((h % 60) / 60)))

    val i = (h/60).toInt % 6

    if (s == 0.0) {

      new ColourRGB(v_s, v_s, v_s)

    } else if (i == 0) {

      new ColourRGB(v_s, t, p)

    } else if (i == 1) {

      new ColourRGB(q, v_s, p)

    } else if (i == 2) {

      new ColourRGB(p, v_s, t)

    } else if (i == 3) {

      new ColourRGB(p, q, v_s)

    } else if (i == 4) {

      new ColourRGB(t, p, v_s)

    } else if (i == 5) {

      new ColourRGB(v_s, p, q)

    } else {

      println("Something wrong happened")

      new ColourRGB(0, 0, 0)

    }

  }

}
