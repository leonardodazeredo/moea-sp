package com.pesc.tebdi.util

import scala.util.Random

import org.moeaframework.analysis.plot.Plot
import org.moeaframework.core.Population

object Utils {

  def getRandomElement(list: Seq[Int], random: Random): Int =
    list(random.nextInt(list.length))

}