import com.lightbend.rp.internal.org.apache.log4j.Logger

object Boom {
  def main(args: Array[String]): Unit = {
    println("Boom")

    val log = Logger.getLogger("test")
    log.info("Hey")
  }
}
