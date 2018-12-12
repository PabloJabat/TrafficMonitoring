package com.trafficmonitoring

import com.geo.data.Read._
import com.geo.data.Write._
import java.io.PrintWriter

object GetTrainingSet {

  def main(args: Array[String]): Unit = {

    val data = loadResultsData("/home/pablo/results_training/part-00000-952e125b-4afb-4eec-ab19-0dba244a53d0-c000.csv")

    val pw = new PrintWriter("/home/pablo/GeoJSON/TestSet.json")

    trainingSetToJSON(pw,data.slice(160,185))

  }

}
