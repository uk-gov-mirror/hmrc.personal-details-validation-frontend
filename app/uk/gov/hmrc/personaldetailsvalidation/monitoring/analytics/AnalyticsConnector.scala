/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.personaldetailsvalidation.monitoring.analytics

import akka.Done
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AnalyticsConnector @Inject() (appConfig: AppConfig, http: HttpClient) {
  def serviceUrl: String = appConfig.platformAnalyticsUrl

  private implicit val eventWrites = Json.writes[Event]
  private implicit val analyticsWrites = Json.writes[AnalyticsRequest]

  def sendEvent(request: AnalyticsRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Done] = {
    val url = s"$serviceUrl/platform-analytics/event"
    http.POST(url, request).map(_ => Done).recover {
      case _ : Throwable =>
        Logger.error(s"Couldn't send analytics event $request")
        Done
    }
  }
}
