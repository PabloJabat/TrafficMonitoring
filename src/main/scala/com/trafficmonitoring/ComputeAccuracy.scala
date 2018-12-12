package com.trafficmonitoring

import scala.io.Source
import scala.util.Random
import com.geo.elements.Point
import com.geo.data.Write._
import java.io._

object ComputeAccuracy {

  def main(args: Array[String]): Unit = {

    def extractPoints(a: Array[String]): (Point, Point) = {

      // the first point is the original one and the second is the matched point

      (new Point(a(2).toDouble, a(3).toDouble, a(4).toDouble, a(1)), new Point(a(5).toDouble, a(6).toDouble, a(1)))

    }

    val path = "/home/pablo/DE/DataSets/testDataSets/"

    val reference = Source.fromFile(path+"trainingSet.csv").getLines().map(_.split(",")).map(a => (a(1),a(7))).toList

    val output = Source.fromFile(path+"Experiments/data_1_1_BC.csv").getLines().map(_.split(",")).map(a => (a(1),a(0))).toList

    val joinedData = reference ++ output

    val groupedData = joinedData.groupBy(_._1).mapValues(_.map(_._2).toList)

    val correctMatches = groupedData.count(a => a._2(0) == a._2(1)).toDouble

    val incorrectMatches = groupedData.count(a => a._2(0) != a._2(1)).toDouble

    val incorrectMatchesKeys = groupedData.filter(a => a._2(0) != a._2(1)).keys.toList

    println("Accuracy: "+ correctMatches/(incorrectMatches + correctMatches))

    println("Incorrect Matches: "+ incorrectMatches)

    println("Correct Matches: "+ correctMatches)

    val r = new Random

    val points = Source.fromFile(path+"Experiments/data_1_1_BC.csv").getLines().map(_.split(","))
      .filter(a => incorrectMatchesKeys.contains(a(1)))
      .map(extractPoints).toList

    val somePoints = r.shuffle(points).take(13)

    val pw = new PrintWriter("/home/pablo/DE/DataSets/testDataSets/Experiments/Points_1_1_BC.json")

    pointsToJSON(pw, somePoints)

  }

}
