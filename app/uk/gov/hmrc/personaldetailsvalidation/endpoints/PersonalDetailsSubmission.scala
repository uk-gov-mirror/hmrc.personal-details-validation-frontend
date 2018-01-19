/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.personaldetailsvalidation.endpoints

import javax.inject.{Inject, Singleton}

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import play.api.mvc.Results._
import play.api.mvc.{Request, Result}
import play.twirl.api.Html
import uk.gov.hmrc.errorhandling.ProcessingError
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.logging.Logger
import uk.gov.hmrc.personaldetailsvalidation.connectors.{FuturedPersonalDetailsSender, FuturedValidationIdFetcher, PersonalDetailsSender, ValidationIdFetcher}
import uk.gov.hmrc.personaldetailsvalidation.model.CompletionUrl
import uk.gov.hmrc.personaldetailsvalidation.views.pages.PersonalDetailsPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.language.{higherKinds, implicitConversions}

@Singleton
private class FuturedPersonalDetailsSubmission @Inject()(personalDetailsPage: PersonalDetailsPage,
                                                         personalDetailsValidationConnector: FuturedPersonalDetailsSender,
                                                         validationIdFetcher: FuturedValidationIdFetcher,
                                                         redirectComposer: RedirectComposer,
                                                         logger: Logger)
  extends PersonalDetailsSubmission[Future](personalDetailsPage, personalDetailsValidationConnector, validationIdFetcher, redirectComposer, logger)

private class PersonalDetailsSubmission[Interpretation[_] : Monad](personalDetailsPage: PersonalDetailsPage,
                                                                   personalDetailsValidationConnector: PersonalDetailsSender[Interpretation],
                                                                   validationIdFetcher: ValidationIdFetcher[Interpretation],
                                                                   redirectComposer: RedirectComposer,
                                                                   logger: Logger) {

  import PersonalDetailsSubmission._
  import personalDetailsValidationConnector._
  import redirectComposer._
  import validationIdFetcher._

  def bindValidateAndRedirect(completionUrl: CompletionUrl)
                             (implicit request: Request[_],
                              headerCarrier: HeaderCarrier,
                              executionContext: ExecutionContext): Interpretation[Result] = {
    for {
      personalDetails <- pure(personalDetailsPage.bindFromRequest(request, completionUrl)) leftMap pageWithErrorToBadRequest
      validationIdFetchUri <- passToValidation(personalDetails) leftMap errorToRedirect(to = completionUrl)
      validationId <- fetchValidationId(validationIdFetchUri) leftMap errorToRedirect(to = completionUrl)
    } yield redirect(completionUrl, validationId)
      .addingToSession(validationIdSessionKey -> validationId)
  }.merge

  private val pageWithErrorToBadRequest: Html => Result = BadRequest(_)

  private def errorToRedirect(to: CompletionUrl): ProcessingError => Result = {
    error =>
      logger.error(error)
      redirectWithTechnicalErrorParameter(to)
  }

  private def pure[L, R](maybeValue: Either[L, R]): EitherT[Interpretation, L, R] =
    EitherT(implicitly[Monad[Interpretation]].pure(maybeValue))
}

private object PersonalDetailsSubmission {
  val validationIdSessionKey = "ValidationId"
}