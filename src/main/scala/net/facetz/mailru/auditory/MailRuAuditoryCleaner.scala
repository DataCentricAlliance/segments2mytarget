package net.facetz.mailru.auditory

import net.facetz.mailru.api.{DisjunctionsItem, MailRuApiProvider, UpdateRemarketingAuditoryRequest}
import net.facetz.mailru.helper.SimpleLogger
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

trait MailRuAuditoryCleaner extends MailRuApiProvider with SimpleLogger {

  protected val periodInDays: Int
  protected val auditoryPattern = """(facetz-auditory|Auditory)_\d+""".r

  def run(): Unit = {
    log.info(s"use period: $periodInDays")
    log.info("auditory cleaner started..")

    getAuthToken match {
      case Some(token) =>
        val ids = getExpiredRemarketingUsersIds(token)
        updateAuditoriesWithExpiredUsers(token, ids)
      case None => throw new IllegalArgumentException("can't get authToken")
    }

    log.info("auditory cleaner finished!")
  }

  protected def getExpiredRemarketingUsersIds(authToken: String): Set[Int] = {
    val regex = """.*segment.*_(\d+)_(\d{8})_.*""".r
    val dateParser = DateTimeFormat.forPattern("yyyyMMdd")
    val optResponse = getRemarketingUsersList(authToken)
    val response = optResponse match {
      case Some(r) => r
      case None => throw new RuntimeException(s"can't deserialize response to RemarketingUserListResponseItem")
    }

    val latestDate = DateTime.now().minusDays(periodInDays)
    response
      .filter(x => x.name match {
        case regex(id, date) => dateParser.parseDateTime(date).isBefore(latestDate)
        case _ => false
      })
      .map(x => {log.info(s"${x.name} is expired"); x})
      .map(_.id).toSet
  }

  protected def updateAuditoriesWithExpiredUsers(token: String, expiredUsersIds: Set[Int]): Unit = {
    val existedRemarketingsAuditory = getRemarketingAuditories(token) match {
      case Some(response) => response
      case None => throw new RuntimeException(s"can't deserialize response to RemarketingAuditoryResponse")
    }

    val expiredIdsInAuditories = existedRemarketingsAuditory
      .filter(auditory => auditoryPattern.pattern.matcher(auditory.name).matches())
      .foldLeft(Set.empty[Int])((filteredIds, auditory) => {
        val expiredIdsInAuditory = auditory.disjunctions.flatMap(
          _.remarketingUsersLists.map(_.remarketingUsersListId).filter(expiredUsersIds.contains))

        if (expiredIdsInAuditory.nonEmpty) {
          val newDisjunctions = auditory.disjunctions.flatMap(disjunction => {
            val filteredLists = disjunction.remarketingUsersLists.filterNot(
              userItem => expiredIdsInAuditory.contains(userItem.remarketingUsersListId))
            if (filteredLists.nonEmpty) {
              Some(DisjunctionsItem(filteredLists))
            } else {
              None
            }
          })
          val request = UpdateRemarketingAuditoryRequest(auditory.id, auditory.name, newDisjunctions)

          log.info(s"update auditory: {id: ${auditory.id}, name: ${auditory.name}}")

          updateRemarketingAuditory(token, request) match {
            case Some(_) => log.info(s"${auditory.name} updated")
            case None => log.error(s"can't update auditory ${auditory.name}")
          }
        }
        filteredIds ++ expiredIdsInAuditory
    })

    log.info(s"${expiredIdsInAuditories.size} expired files in auditories")

    val unattachedIds = existedRemarketingsAuditory
      .foldLeft(expiredUsersIds)((filteredIds, auditory) => {
      val expiredIdsInAuditory = auditory.disjunctions.flatMap(
        _.remarketingUsersLists.map(_.remarketingUsersListId).filter(expiredUsersIds.contains))
      filteredIds -- expiredIdsInAuditory
    })

    log.info(s"${unattachedIds.size} unattached expired files")

    deleteExpiredUsers(token, expiredIdsInAuditories)
    deleteExpiredUsers(token, unattachedIds)
  }

  protected def deleteExpiredUsers(token: String, expiredUsersIds: Set[Int]): Unit = {
    expiredUsersIds.foreach(id => {
      log.info(s"deleting usersList: $id")
      val success = deleteRemarketingUsersList(token, id)
      if (!success) {
        log.error(s"can't delete users list: $id")
      }
    })
  }

}
