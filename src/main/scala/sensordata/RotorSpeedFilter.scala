package sensordata

import akka.stream.scaladsl.RunnableGraph
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._

class RotorSpeedFilter extends AkkaStreamlet {
  val in: CodecInlet[Metric]         = AvroInlet[Metric]("in")
  val out: CodecOutlet[Metric]       = AvroOutlet[Metric]("out").withPartitioner(RoundRobinPartitioner)
  override val shape: StreamletShape = StreamletShape(in, out)

  override def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {
    def flow = FlowWithCommittableContext[Metric]().filter(_.name == "rotorSpeed")

    override def runnableGraph(): RunnableGraph[_] = sourceWithCommittableContext(in).via(flow).to(committableSink(out))
  }
}