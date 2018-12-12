package com.trafficmonitoring

import better.files._
import better.files.File._


object prueba {

  def main(args: Array[String]): Unit = {

    val outputPath = "/home/pablo/DE/GeoJSON"

    try {

      val file = File.apply(outputPath + "/MMresults")

      file.delete()

    } catch {

      case _:java.nio.file.NoSuchFileException => println("No previous results found. No need to delete folder. ")

    }

  }

}
