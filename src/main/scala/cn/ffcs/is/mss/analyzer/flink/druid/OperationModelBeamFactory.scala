package cn.ffcs.is.mss.analyzer.flink.druid

import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import com.metamx.common.Granularity
import com.metamx.tranquility.beam.{Beam, ClusteredBeamTuning}
import com.metamx.tranquility.druid._
import com.metamx.tranquility.flink.BeamFactory
import io.druid.data.input.impl.TimestampSpec
import io.druid.granularity.PeriodGranularity
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.BoundedExponentialBackoffRetry
import org.joda.time.{DateTime, DateTimeZone, Period}

class OperationModelBeamFactory (conf: collection.mutable.HashMap[String,String]) extends BeamFactory[OperationModel]{
  lazy val makeBeam: Beam[OperationModel] = {
    // Tranquility uses ZooKeeper (through Curator framework) for coordination.
    val curator = CuratorFrameworkFactory.newClient(
      conf("tranquility.zk.connect"),
      new BoundedExponentialBackoffRetry(100, 3000, 5)
    )
    curator.start()

    val indexService = "druid/overlord" // Your overlord's druid.service, with slashes replaced by colons.
    val discoveryPath = "/druid/discovery" // Your overlord's druid.discovery.curator.path
    val dataSource = conf("druid.operation.source")
    val dimensions = OperationModel.Columns
    val aggregators = OperationModel.Metrics

    // Expects simpleEvent.timestamp to return a Joda DateTime object.
    DruidBeams
      .builder((operationModel: OperationModel) => new DateTime(operationModel.timeStamp))
      .timestampSpec(new TimestampSpec("timeStamp","auto",null))
      .curator(curator)
      .discoveryPath(discoveryPath)
      .location(DruidLocation.create(indexService, dataSource))
      .rollup(DruidRollup(SpecificDruidDimensions(dimensions), aggregators, new PeriodGranularity(Period.parse("PT1S"), null, DateTimeZone.forID(OperationModel.druidFormat))))
      .tuning(
        ClusteredBeamTuning
          .builder.segmentGranularity(Granularity.HOUR)
          .windowPeriod(new Period("PT15M"))
          .build)
      .druidBeamConfig(DruidBeamConfig.builder().randomizeTaskId(true).build()).buildBeam()

  }
}
