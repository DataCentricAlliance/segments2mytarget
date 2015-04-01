package net.facetz.export.mr.mailru.api

import argonaut.Argonaut._
import argonaut._

case class MailRuAuthResponse(access_token: String)

object MailRuAuthResponse {
  implicit def MailRuAuthResponseCodecJson: CodecJson[MailRuAuthResponse] =
    casecodec1(MailRuAuthResponse.apply, MailRuAuthResponse.unapply)("access_token")
}


case class RemarketingUserListItem(remarketingUsersListId: Int,
                                   `type`: String = "positive")

object RemarketingUserListItem {
  implicit def RemarketingUserListEncodeJson: EncodeJson[RemarketingUserListItem] =
    jencode2L((r: RemarketingUserListItem) => (r.remarketingUsersListId, r.`type`))("remarketing_users_list_id", "type")

  implicit def RemarketingUserListItemCodecJson: CodecJson[RemarketingUserListItem] =
    casecodec2(RemarketingUserListItem.apply, RemarketingUserListItem.unapply)("remarketing_users_list_id", "type")
}


case class DisjunctionsItem(remarketingUsersLists: List[RemarketingUserListItem])

object DisjunctionsItem {
  implicit def DisjunctionsItemEncodeJson: EncodeJson[DisjunctionsItem] =
    jencode1L((r: DisjunctionsItem) => r.remarketingUsersLists)("remarketing_users_lists")

  implicit def DisjunctionsItemCodecJson: CodecJson[DisjunctionsItem] =
    casecodec1(DisjunctionsItem.apply, DisjunctionsItem.unapply)("remarketing_users_lists")

}


case class CreateRemarketingAuditoryRequest(name: String,
                                            disjunctions: List[DisjunctionsItem])

object CreateRemarketingAuditoryRequest {
  implicit def RemarketingAuditoryRequestEncodeJson: EncodeJson[CreateRemarketingAuditoryRequest] =
    jencode2L((r: CreateRemarketingAuditoryRequest) => (r.name, r.disjunctions))("name", "disjunctions")
}


//[{"campaigns": [], "disjunctions": [{"remarketing_users_lists": [{"remarketing_users_list_id": 26717, "type": "positive"}], "remarketing_groups": [], "remarketing_game_players": [], "remarketing_payers": [], "remarketing_context_phrases": [], "remarketing_counters": [], "remarketing_mobile_apps": [], "remarketing_game_payers": [], "remarketing_pricelists": [], "remarketing_players": []}], "flags": ["cross_device"], "id": 43851, "name": "auditory_947"}]
case class RemarketingAuditoryItem(id: Int,
                                   name: String,
                                   disjunctions: List[DisjunctionsItem])

object RemarketingAuditoryItem {
  implicit def RemarketingAuditoryItemCodecJson: CodecJson[RemarketingAuditoryItem] = codec3(
    (id: Int, name: String, disjunctions: List[DisjunctionsItem]) => RemarketingAuditoryItem(id, name, disjunctions),
    (item: RemarketingAuditoryItem) => (item.id, item.name, item.disjunctions)
  )("id", "name", "disjunctions")
}

//{"status": "loading", "users_count": 0, "type": "dmp_id", "id": 26717, "name": "segment_947_20150304_1"}
case class RemarketingUserListResponseItem(id: Int,
                                           name: String,
                                           status: String,
                                           usersCount: Int,
                                           `type`: String)

object RemarketingUserListResponseItem {
  implicit def RemarketingUserListResponseItemCodecJson: CodecJson[RemarketingUserListResponseItem] = codec5(
    (id: Int, name: String, status: String, usersCount: Int, `type`: String) =>
      RemarketingUserListResponseItem(id, name, status, usersCount, `type`),
    (item: RemarketingUserListResponseItem) => (item.id, item.name, item.status, item.usersCount, item.`type`)
  )("id", "name", "status", "users_count", "type")
}

//{"remaining": {"3600": 0}, "limits": {"3600": 3}}
case class LimitItem(`3600`: Int)

object LimitItem {
  implicit def LimitItemCodecJson: CodecJson[LimitItem] = codec1(
    (`3600`: Int) => LimitItem(`3600`),
    (item: LimitItem) => item.`3600`
  )("3600")
}


case class OverTheLimitResponse(remaining: LimitItem, limits: LimitItem)

object OverTheLimitResponse {
  implicit def OverTheLimitResponseCodecJson: CodecJson[OverTheLimitResponse] = codec2(
    (remaining: LimitItem, limits: LimitItem) => OverTheLimitResponse(remaining, limits),
    (item: OverTheLimitResponse) => (item.remaining, item.limits)
  )("remaining", "limits")
}