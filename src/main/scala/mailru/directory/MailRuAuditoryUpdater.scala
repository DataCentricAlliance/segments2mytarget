package net.facetz.export.mr.mailru.directory

import mailru.helper.SimpleLogger
import net.facetz.export.mr.mailru.api._
import org.apache.commons.lang3.StringUtils

import scala.collection.mutable

trait MailRuAuditoryUpdater extends MailRuApiProvider with SimpleLogger {

  protected def dateStr: String

  private[this] var ourSegmentIdToTheirIds: mutable.MultiMap[String, Int] = null

  def run(): Unit = {
    log.info("auditory update started..")

    getAuthToken match {
      case Some(token) =>
        ourSegmentIdToTheirIds = findOurSegmentIdToTheirSegmentIds(token)
        updateRetargetingAuditory(token, ourSegmentIdToTheirIds.keys)
      case None => throw new IllegalArgumentException("can't get authToken")
    }

    log.info("auditory update finished!")
  }

  private def findOurSegmentIdToTheirSegmentIds(authToken: String): mutable.MultiMap[String, Int] = {
    val regex = s"(segment_.*_${dateStr}.*)".r
    val optResponse = getRemarketingUsersList(authToken)
    val response = optResponse match {
      case Some(r) => r
      case None => throw new RuntimeException(s"can't deserialize response to RemarketingUserListResponseItem")
    }

    val ourSegmentIdToTheirSegmentIds: mutable.MultiMap[String, Int] = new mutable.HashMap[String, mutable.Set[Int]]
      with mutable.MultiMap[String, Int]

    response
      .filter(x => regex.pattern.matcher(x.name).matches())
      .foreach(t => {
      t.name.split('_') match {
        case Array(_, ourSegmentId, _) => ourSegmentIdToTheirSegmentIds.addBinding(ourSegmentId, t.id)
        case _ => log.error(s"can't parse external file name to our id: ${t.name}")
      }
    })
    ourSegmentIdToTheirSegmentIds
  }

  //TODO test below

  protected def updateRetargetingAuditory(token: String, segmentIds: Iterable[String]) = {
    val existedRemarketingsAuditory: List[RemarketingAuditoryItem] = getRemarketingAuditories(token) match {
      case Some(response) => response
      case None => throw new RuntimeException(s"can't deserialize response to RemarketingAuditoryResponse")
    }

    // наш segmentId -> их auditoryId | их auditoryId -> List[их segmentId]
    existedRemarketingsAuditory.foreach(t => log.info(t.toString))
    val (existedAuditoryByOurSegmentId, existedSegmentByAuditory) = findLocalExternalMatchings(existedRemarketingsAuditory)
    val (existed, notExisted) = segmentIds.partition(existedAuditoryByOurSegmentId.contains)

    log.info("TODO") // TODO
    //      createNewAuditories(token, notExisted)
    //      updateOldAuditories(token, existed)


  }

  protected def createNewAuditories(token: String, segmentIds: Seq[String]): Unit = {
    val namesWithUserListItems: Seq[(String, Seq[RemarketingUserListItem])] =
      segmentIds.map(ourSegmentIdForCreate => {
        val theirIds: Seq[Int] = ourSegmentIdToTheirIds(ourSegmentIdForCreate).toSeq
        val result: Seq[RemarketingUserListItem] = theirIds.map(RemarketingUserListItem(_))
        (ourSegmentIdForCreate.toString, result)
      })
    namesWithUserListItems.foreach(tupple => {
      val (name, li) = tupple
      val request = CreateRemarketingAuditoryRequest(name, List(DisjunctionsItem(li.toList)))
      createRemarketingAuditory(token, request) match {
        // FIXME process result
        case Some(_) =>
        case None =>
      }
    })
  }

  protected def updateOldAuditories(token: String, segmentIds: Seq[Int]): Unit = {

  }

  private def findLocalExternalMatchings(remarketingsAuditories: List[RemarketingAuditoryItem])
  : (Map[String, Int], mutable.MultiMap[Int, Int]) = {

    val extSegmentByExtAuditory: mutable.MultiMap[Int, Int] = new mutable.HashMap[Int, mutable.Set[Int]] with mutable.MultiMap[Int, Int]
    val extAuditoryByOurSegmentId: mutable.Map[String, Int] = new mutable.HashMap[String, Int]

    remarketingsAuditories.foreach(t => {
      t.disjunctions.flatMap(d => {
        for (item <- d.remarketingUsersLists) yield item.remarketingUsersListId
      }).foreach(extSegmentId => {
        extSegmentByExtAuditory.addBinding(t.id, extSegmentId)
        val ourSegmentId = StringUtils.substringAfterLast(t.name, "_")
        extAuditoryByOurSegmentId(ourSegmentId) = t.id
      })
    })
    (extAuditoryByOurSegmentId.toMap, extSegmentByExtAuditory)

  }

}
