package org.softlang.qegal

import java.io.File

import com.google.common.base.Charsets
import org.json4s.JsonAST.{JArray, JString}
import org.softlang.jutils.{JUtils, SUtils}
import org.softlang.jutils.collectors.SGitAPIs

import collection.JavaConverters._

/**
  * Created by Johannes on 27.11.2017.
  */
object QueryGitProgram {

  def segmentsFile = new File("data/segments.csv")

  def repositoriesFile = new File("data/repositories.csv")

  def query = "https://api.github.com/search/code?q=EClass+extension:ecore"

  def main(args: Array[String]): Unit = {
    printSize()
  }

  def printSize(): Unit = {
    // At 28.11.17 result count of 68844, one day before gathered results of 68601. May indexed size working.
    println(SGitAPIs.size(query))
  }

  def queryFull(): Unit = {
    val segments = SUtils.readCsv(segmentsFile).map(x => (x("low").toInt, x("high").toInt, x("size").toInt))
    val csvSink = JUtils.asCSVSink(repositoriesFile, Charsets.UTF_8, "low", "high", "page", "repository", "uri")

    val pages = segments.flatMap { case (low, high, size) => (1 to ((size + 99) / 100)).map(page => (low, high, size, page)) }

    for ((low, high, size, page) <- pages) {
      println("Querying page from " + low + " to " + high + " bits on size " + size + " page " + page)
      val json = SGitAPIs.collect(query, low = low, high = high, page = page)
      val JArray(items) = json \ "items"

      for (item <- items) {
        val JString(url) = item \ "html_url"
        val JString(repository) = item \ "repository" \ "full_name"
        val content = "https://raw.githubusercontent.com/" + repository + url.substring(18 + repository.length + 6)
        csvSink.write(String.valueOf(low), String.valueOf(high), String.valueOf(page), repository, content)
      }
    }

    csvSink.close()
  }

  def querySegements(): Unit = {
    val segments = SGitAPIs.getLHSizes(query, 0, SGitAPIs.GIT_INDEXED_MAX_SIZE).map(_.productIterator.toSeq)

    SUtils.writeCsv(segmentsFile, Seq(Seq("low", "high", "size")) ++ segments)

  }
}
