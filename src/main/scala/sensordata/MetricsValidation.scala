package sensordata

import akka.stream.scaladsl.RunnableGraph
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl._
import cloudflow.akkastream.util.scaladsl._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._

class MetricsValidation extends AkkaStreamlet {
  val in: CodecInlet[Metric]              = AvroInlet[Metric]("in")
  val invalid: CodecOutlet[InvalidMetric] = AvroOutlet[InvalidMetric]("invalid").withPartitioner(metric => metric.metric.deviceId.toString)
  val valid: CodecOutlet[Metric]          = AvroOutlet[Metric]("valid").withPartitioner(RoundRobinPartitioner)
  override val shape: StreamletShape      = StreamletShape(in).withOutlets(invalid, valid)

  override def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {
    override def runnableGraph(): RunnableGraph[_] = sourceWithCommittableContext(in).to(Splitter.sink(flow, invalid, valid))
    def flow =
      FlowWithCommittableContext[Metric]()
        .map { metric =>
          if (!SensorDataUtils.isValidMetric(metric)) Left(InvalidMetric(metric, "All measurements must be positive numbers!"))
          else Right(metric)
        }
  }
}
