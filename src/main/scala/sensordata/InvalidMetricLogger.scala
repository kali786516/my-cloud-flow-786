package sensordata

import akka.stream.scaladsl._
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._


class InvalidMetricLogger extends AkkaStreamlet {
  val inlet: CodecInlet[InvalidMetric] = AvroInlet[InvalidMetric]("in")
  override val shape: StreamletShape   = StreamletShape.withInlets(inlet)

  override def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {
    val flow = FlowWithCommittableContext[InvalidMetric]()
      .map { invalidMetric =>
        system.log.warning(s"Invalid metric detected! $invalidMetric")
        invalidMetric
      }

    override def runnableGraph(): RunnableGraph[_] =
      sourceWithCommittableContext(inlet).via(flow).to(committableSink)
  }

}
