package sensordata

import akka.stream.scaladsl._
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._

//tag::config-parameter1[]
class ValidMetricLogger extends AkkaStreamlet {
  //end::config-parameter1[]

  val inlet: CodecInlet[Metric]      = AvroInlet[Metric]("in")
  override val shape: StreamletShape = StreamletShape.withInlets(inlet)

  val LogLevel = RegExpConfigParameter(
    "log-level",
    "Provide one of the following log levels, debug, info, warning or error",
    "^debug|info|warning|error$",
    Some("debug")
  )

  //tag::config-parameter2[]
  val MsgPrefix = StringConfigParameter("msg-prefix", "Provide a prefix for the log lines", Some("valid-logger"))
  //end::config-parameter2[]

  override def configParameters = Vector(LogLevel, MsgPrefix)

  override def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {
    val logF: String => Unit = LogLevel.value.toLowerCase match {
      case "debug"   => system.log.debug _
      case "info"    => system.log.info _
      case "warning" => system.log.warning _
      case "error"   => system.log.error _
    }

    val msgPrefix = MsgPrefix.value

    def log(metric: Metric): Unit =
      logF(s"$msgPrefix $metric")

    def flow =
      FlowWithCommittableContext[Metric]()
        .map { validMetric =>
          log(validMetric)
          validMetric
        }

    override def runnableGraph(): RunnableGraph[_] =
      sourceWithCommittableContext(inlet).via(flow).to(committableSink)
  }
}
