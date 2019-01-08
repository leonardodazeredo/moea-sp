package com.pesc.moeasp.util

import scala.util.Random

import org.apache.spark.SparkContext

object Utils {

  def getRandomElement(list: Seq[Int], random: Random): Int = list(random.nextInt(list.length))

  def unPersistAllRdds(sc: SparkContext) {

    val rdds = sc.getPersistentRDDs
    rdds.foreach(x => x._2.unpersist())

  }

}