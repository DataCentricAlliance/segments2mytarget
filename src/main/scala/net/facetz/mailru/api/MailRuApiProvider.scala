package net.facetz.mailru.api

import java.io.{File, FileInputStream, IOException}

import argonaut.Argonaut._
import net.facetz.mailru.helper.SimpleLogger
import org.apache.commons.io.IOUtils

import scala.util.{Failure, Try}
import scala.util.control.NonFatal
import scalaj.http._

trait MailRuApiProvider extends SimpleLogger {

  protected val apiUrl: String

  protected def clientId: String

  protected def clientSecret: String

  protected def subAccountName: Option[String]

  def clientApiUrl: String =
    subAccountName match {
      case Some(name) => s"$apiUrl/users/$name"
      case None => apiUrl
    }

  private[this] val defaultConnectionTimeout = 300 * 1000
  private[this] val defaultReadTimeout = 300 * 1000

  class MailRuRequestHelper(val request: HttpRequest) {
    def addAuthToken(token: String): HttpRequest = auth(request, token)

    def setTimeouts(connectionTimeout: Int = defaultConnectionTimeout, readTimeout: Int = defaultReadTimeout): HttpRequest =
      request.option(HttpOptions.connTimeout(defaultConnectionTimeout))
        .option(HttpOptions.readTimeout(defaultReadTimeout))

    def logError[U]: PartialFunction[Throwable, Try[U]] = {
      case NonFatal(e) =>
        log.error("Error during http request: ", e)
        Failure(e)
    }

    def tryAsString: HttpResponse[String] = {
      Try {request.asString}.recoverWith(logError).getOrElse(HttpResponse("Error occurred during request", 500, Map.empty))
    }
  }

  implicit def requestToAuthAndWithLimitsRequest(req: HttpRequest): MailRuRequestHelper = new MailRuRequestHelper(req)

  protected def auth(req: HttpRequest, token: String) = req.header("Authorization", s"Bearer $token")

  protected def getAuthToken: Option[String] = {
    val response = Http(s"$apiUrl/api/v2/oauth2/token.json")
      .headers("Content-Type" -> "application/x-www-form-urlencoded")
      .params("grant_type" -> "client_credentials", "client_id" -> clientId, "client_secret" -> clientSecret)
      .setTimeouts()
      .postForm
      .tryAsString

    if (response.isSuccess) {
      response.body.decodeOption[MailRuAuthResponse] match {
        case Some(auth) => Option(auth.access_token)
        case None => None
      }
    } else {
      log.error(response.body)
      None
    }
  }

  protected def getRemarketingUsersList(authToken: String): Option[List[RemarketingUserListResponseItem]] = {
    val findUserListsResponse = Http(s"$clientApiUrl/api/v1/remarketing_users_lists.json")
      .addAuthToken(authToken)
      .setTimeouts()
      .tryAsString

    if (findUserListsResponse.isSuccess) {
      findUserListsResponse.body.decodeOption[List[RemarketingUserListResponseItem]]
    } else {
      None
    }
  }

  protected def getRemarketingAuditories(authToken: String): Option[List[RemarketingAuditoryItem]] = {
    val existedRemarketings = Http(s"$clientApiUrl/api/v1/remarketings.json")
      .addAuthToken(authToken)
      .setTimeouts()
      .tryAsString

    existedRemarketings.body.decodeOption[List[RemarketingAuditoryItem]]
  }

  protected def createRemarketingAuditory(authToken: String,
                                          request: CreateRemarketingAuditoryRequest): Option[String] = {
    val createResponse = Http(s"$clientApiUrl/api/v1/remarketings.json")
      .postData(request.asJson.toString())
      .addAuthToken(authToken)
      .setTimeouts()
      .tryAsString

    if (createResponse.isSuccess) {
      Option(createResponse.body)
    } else {
      None
    }
  }

  protected def updateRemarketingAuditory(authToken: String,
                                          request: UpdateRemarketingAuditoryRequest): Option[String] = {
    val updateResponse = Http(s"$clientApiUrl/api/v1/remarketings/${request.id}.json")
      .postData(request.asJson.toString())
      .addAuthToken(authToken)
      .setTimeouts()
      .tryAsString

    if (updateResponse.isSuccess) {
      Option(updateResponse.body)
    } else {
      None
    }
  }


  protected def uploadSegmentFile(authToken: String, file: File, name: String): Try[Either[OverTheLimitResponse,
    RemarketingUserListResponseItem]] = {
    Try {
      val is = new FileInputStream(file)

      val result = try {
        val array: Array[Byte] = IOUtils.toByteArray(is)
        val result: HttpResponse[String] =
          Http(s"$clientApiUrl/api/v1/remarketing_users_lists.json")
            .addAuthToken(authToken)
            .setTimeouts()
            .postMulti(
              MultiPart("file", name, "application/text", array),
              MultiPart("name", "", "", name),
              MultiPart("type", "", "", "dmp_id"))
            .tryAsString
        val overLimitValidation = result.body.decodeValidation[OverTheLimitResponse]
        val goodItemValidation = result.body.decodeValidation[RemarketingUserListResponseItem]
        if (goodItemValidation.isSuccess) {
          Right(goodItemValidation.getOrElse(null))
        } else if (overLimitValidation.isSuccess) {
          Left(overLimitValidation.getOrElse(null))
        } else {
          log.error(s"${result.statusLine}-${result.body}")
          throw new RuntimeException("bad response")
        }
      } catch {
        case t: IOException if "Premature EOF".equals(t.getMessage) => Left(null)
      } finally {
        is.close()
      }
      result
    }
  }

  protected def deleteRemarketingUsersList(authToken: String, usersListId: Int): Boolean = {
    val deleteUsersListResponse = Http(s"$clientApiUrl/api/v1/remarketing_users_list/$usersListId.json")
      .method("DELETE")
      .addAuthToken(authToken)
      .setTimeouts()
      .tryAsString

    deleteUsersListResponse.code == 204
  }
}
