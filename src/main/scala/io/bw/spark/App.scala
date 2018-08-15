package io.bw.spark

import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author Byungwook Lee
  */
object App {

  def foo(x: Array[String]) = x.foldLeft("")((a, b) => a + b)

  def main(args: Array[String]) {
    println("Hello World!")
    println("concat arguments = " + foo(args))
    val sc = getSparkContext

    //    doZipPartitions(sc)

    sc.parallelize(1 to 100).map(_ + 1).collect().foreach(println)
  }

  def doZipPartitions(sc: SparkContext) {
    val rdd1 = sc.parallelize(List("a", "b", "c"), 3)
    val rdd2 = sc.parallelize(List(1, 2, 3), 3)
    val rdd3 = sc.parallelize(List(1, 2, 3), 3)
    val result = rdd1.zipPartitions(rdd2, rdd3) {
      (it1, it2, it3) =>
        for {
          v1 <- it1;
          v2 <- it2;
          v3 <- it3
        } yield v1 + v2 + v3
    }
    println(result.collect.mkString(", "))
  }

  def getSparkContext(): SparkContext = {
    val conf = new SparkConf()
    conf.setMaster("local[*]").setAppName("RDDOpSample")
    new SparkContext(conf)
  }

}
