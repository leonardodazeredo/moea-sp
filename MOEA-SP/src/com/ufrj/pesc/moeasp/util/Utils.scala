package com.ufrj.pesc.moeasp.util

import scala.util.Random

import org.apache.spark.SparkContext
import java.lang.reflect.Constructor

object Utils {

  def getRandomElement(list: Seq[Int], random: Random): Int = list(random.nextInt(list.length))

  def unPersistAllRdds(sc: SparkContext) {

    val rdds = sc.getPersistentRDDs
    rdds.foreach(x => x._2.unpersist())

  }

  def instantiate(className: String, args: AnyRef*) : AnyRef = {
    val clazz = Class.forName(className);
    val argTypes = args.map(a => a.getClass)
    val ctor = clazz.getConstructor(argTypes: _*);
    val o = ctor.newInstance(args: _*)
    o.asInstanceOf[AnyRef]
  }

}