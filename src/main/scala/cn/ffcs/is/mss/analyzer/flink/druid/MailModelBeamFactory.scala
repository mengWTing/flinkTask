package cn.ffcs.is.mss.analyzer.flink.druid

import cn.ffcs.is.mss.analyzer.druid.model.scala.{MailModel, OperationModel}
import com.metamx.common.Granularity
import com.metamx.tranquility.beam.{Beam, ClusteredBeamTuning}
import com.metamx.tranquility.druid._
import com.metamx.tranquility.flink.BeamFactory
import io.druid.data.input.impl.TimestampSpec
import io.druid.granularity.PeriodGranularity
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.BoundedExponentialBackoffRetry
import org.joda.time.{DateTime, DateTimeZone, Period}

/**
  * @title MailModelBeamFactory
  * @author liangzhaosuo
  * @date 2020-11-20 17:30
  * @description
  * @update [no][date YYYY-MM-DD][name][description]
  */


class MailModelBeamFactory(conf: collection.mutable.HashMap[String, String]) extends BeamFactory[MailModel] {
  lazy val makeBeam: Beam[MailModel] = {
    val curator = CuratorFrameworkFactory.newClient(
      conf("tranquility.zk.connect"),
      new BoundedExponentialBackoffRetry(100, 3000, 5)
    )
    curator.start()

    val indexService = "druid/overlord" // Your overlord's druid.service, with slashes replaced by colons.
    val discoveryPath = "/druid/discovery" // Your overlord's druid.discovery.curator.path
    val dataSource = conf("druid.mail.source")
    val dimensions = MailModel.Columns
    val aggregators = MailModel.Metrics

    val beams = DruidBeams
      .builder((mailModel: MailModel) => new DateTime(mailModel.timeStamp))
      .timestampSpec(new TimestampSpec("timeStamp", "auto", null))
      .curator(curator)
      .discoveryPath(discoveryPath)
      .location(DruidLocation.create(indexService, dataSource))
      .rollup(DruidRollup(SpecificDruidDimensions(dimensions), aggregators, new PeriodGranularity(Period.parse
      ("PT1S"), null, DateTimeZone.forID(MailModel.druidFormat))))
      
      .tuning(
        ClusteredBeamTuning
          .builder.segmentGranularity(Granularity.DAY)
          .windowPeriod(new Period("P6M"))
          .build)
      .druidBeamConfig(DruidBeamConfig.builder().randomizeTaskId(true).build()).buildBeam()

    println(beams.toString)
    beams
  }
}
