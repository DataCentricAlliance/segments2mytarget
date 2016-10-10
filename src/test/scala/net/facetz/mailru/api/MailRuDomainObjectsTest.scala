package net.facetz.mailru.api

import org.scalatest.{FunSuite, Matchers}
import argonaut.Argonaut._

class MailRuDomainObjectsTest extends FunSuite with Matchers {

  test("MailRuAuthResponse should be read from json correct") {
    val json =
      """
        |{"access_token": "token", "token_type": "Bearer", "expires_in": 86400, "refresh_token": "refresh_token", "tokens_left": 3}
      """.stripMargin

    val resultOpt = json.decodeOption[MailRuAuthResponse]

    resultOpt should be('defined)

    val result = resultOpt.get
    result.access_token should equal("token")
    result.token_type should equal("Bearer")
    result.expires_in should equal(86400)
    result.refresh_token should equal("refresh_token")
    result.tokens_left should equal(Some(3))
  }

}
