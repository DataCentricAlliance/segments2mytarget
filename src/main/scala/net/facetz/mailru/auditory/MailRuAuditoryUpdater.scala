package net.facetz.mailru.auditory

import net.facetz.mailru.helper.SimpleLogger
import net.facetz.mailru.api._
import org.apache.commons.lang3.StringUtils

import scala.collection.mutable

trait MailRuAuditoryUpdater extends MailRuApiProvider with SimpleLogger {

  protected def dateStr: String

  private[this] var ourSegmentIdToTheirIds: mutable.MultiMap[String, Int] = null

  def run(): Unit = {
    log.info(s"use date: $dateStr")
    log.info("auditory update started..")

    getAuthToken match {
      case Some(token) =>
        ourSegmentIdToTheirIds = findOurSegmentIdToTheirSegmentIds(token)
        if(ourSegmentIdToTheirIds.nonEmpty) {
          updateRetargetingAuditory(token, ourSegmentIdToTheirIds.keys)
        } else {
          log.info("nothing to update")
        }
      case None => throw new IllegalArgumentException("can't get authToken")
    }

    log.info("auditory update finished!")
  }

  protected def findOurSegmentIdToTheirSegmentIds(authToken: String): mutable.MultiMap[String, Int] = {
    val regex = s"(.*segment_.*${dateStr}.*)".r
    val optResponse = getRemarketingUsersList(authToken)
    val response = optResponse match {
      case Some(r) => r
      case None => throw new RuntimeException(s"can't deserialize response to RemarketingUserListResponseItem")
    }

    val ourSegmentIdToTheirSegmentIds = new mutable.HashMap[String, mutable.Set[Int]] with mutable.MultiMap[String, Int]

    response
      .filter(x => regex.pattern.matcher(x.name).matches())
      .foreach(t => {
      t.name.split('_') match {
        case Array(_, ourSegmentId, _, _) => ourSegmentIdToTheirSegmentIds.addBinding(ourSegmentId, t.id)
        case _ => log.error(s"can't parse external file name to our id: ${t.name}")
      }
    })
    ourSegmentIdToTheirSegmentIds
  }

  protected def updateRetargetingAuditory(token: String, segmentIds: Iterable[String]) = {
    val existedRemarketingsAuditory = getRemarketingAuditories(token) match {
      case Some(response) => response
      case None => throw new RuntimeException(s"can't deserialize response to RemarketingAuditoryResponse")
    }

    // ext segmentId -> our auditoryId | ext auditoryId -> List[ext segmentId]
    val (existedAuditoryByOurSegmentId, extSegmentByExtAuditory) = findLocalExternalMatchings(existedRemarketingsAuditory)
    val (existed, notExisted) = segmentIds.partition(existedAuditoryByOurSegmentId.contains)

    createNewAuditories(token, notExisted.toSeq)
    val auditoryById = existedRemarketingsAuditory.map(t => (t.id, t)).toMap
    updateOldAuditories(token, auditoryById, existedAuditoryByOurSegmentId, extSegmentByExtAuditory)
  }

  protected def createNewAuditories(token: String, segmentIds: Seq[String]): Unit = {
    val namesWithUserListItems = segmentIds.map(ourSegmentIdForCreate => {
      val theirIds = ourSegmentIdToTheirIds.getOrElse(ourSegmentIdForCreate, Set.empty[Int])
      val result = theirIds.map(segmentIdToDisjunctionItem)
      (ourSegmentIdForCreate.toString, result)
    })

    for((name, disjunctionsItems) <- namesWithUserListItems) {
      val newAuditoryName = segmentToName(name)
      val request = CreateRemarketingAuditoryRequest(newAuditoryName, disjunctionsItems.toList)
      createRemarketingAuditory(token, request) match {
        case Some(_) => log.info(s"$newAuditoryName created")
        case None => log.error(s"can't create auditory $newAuditoryName")
      }
    }
  }

  protected def updateOldAuditories(token: String,
                          existedAuditoryById: Map[Int, RemarketingAuditoryItem],
                          existedAuditoryByOurSegmentId: Map[String, Int],
                          existedExtSegmentByExtAuditory: mutable.MultiMap[Int, Int]): Unit = {
    val newExtSegmentsByExtAuditory = existedAuditoryByOurSegmentId.map({
      case (ourSegmentId, auditoryId) => (auditoryId, ourSegmentIdToTheirIds.getOrElse(ourSegmentId, Set.empty[Int]))
    })

    for((auditoryId, segments) <- existedExtSegmentByExtAuditory) {
      val newSegments = newExtSegmentsByExtAuditory.getOrElse(auditoryId, Set.empty[Int])
      if (newSegments.nonEmpty) {
        val resultSegments = segments.union(newSegments)
        val disjunctionsItems = resultSegments.map(segmentIdToDisjunctionItem)
        val name = existedAuditoryById(auditoryId).name
        val request = UpdateRemarketingAuditoryRequest(auditoryId, name, disjunctionsItems.toList)

        updateRemarketingAuditory(token, request) match {
          case Some(_) => log.info(s"$name updated")
          case None => log.error(s"can't update auditory $name")
        }
      }
    }
  }


  private[this] def segmentToName(segmentId: String) = s"facetz-auditory_$segmentId"

  private[this] def segmentIdToDisjunctionItem(id: Int) = DisjunctionsItem(List(RemarketingUserListItem(id)))

  private[this] def findLocalExternalMatchings(remarketingsAuditories: List[RemarketingAuditoryItem]) : (Map[String, Int], mutable.MultiMap[Int, Int]) = {

    val extSegmentByExtAuditory: mutable.MultiMap[Int, Int] = new mutable.HashMap[Int, mutable.Set[Int]] with mutable.MultiMap[Int, Int]
    val extAuditoryByOurSegmentId: mutable.Map[String, Int] = new mutable.HashMap[String, Int]

    remarketingsAuditories.foreach(auditory => {
      auditory.disjunctions
        .flatMap(_.remarketingUsersLists.map(_.remarketingUsersListId).toSeq)
        .foreach(extSegmentId => {
          extSegmentByExtAuditory.addBinding(auditory.id, extSegmentId)
          val ourSegmentId = StringUtils.substringAfterLast(auditory.name, "_")
          if(StringUtils.isNoneEmpty(ourSegmentId)) {
            extAuditoryByOurSegmentId(ourSegmentId) = auditory.id
          }
        })
    })
    (extAuditoryByOurSegmentId.toMap, extSegmentByExtAuditory)
  }

}
