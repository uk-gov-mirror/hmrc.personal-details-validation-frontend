/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.personaldetailsvalidationfrontend.binders

import org.scalacheck.Gen
import uk.gov.hmrc.personaldetailsvalidationfrontend.generators.Generators.Implicits._
import uk.gov.hmrc.personaldetailsvalidationfrontend.generators.ValuesGenerators.journeyIds
import uk.gov.hmrc.play.test.UnitSpec

class JourneyIdQueryBindableSpec extends UnitSpec {

  "journeyIdQueryBindable.bind" should {

    "return Some JourneyId if 'journeyId' is present in params" in {
      journeyIdQueryBindable.bind("journeyId", Map("journeyId" -> Seq(journeyIds.generateOne.toString)))
    }

    "return Left with error if 'journeyId' is non-uuid" in {
      journeyIdQueryBindable.bind("journeyId", Map("journeyId" -> Seq(Gen.alphaStr.generateOne)))
    }

    "return Left with error if 'journeyId' is not given" in {
      journeyIdQueryBindable.bind("journeyId", Map.empty)
    }
  }

  "journeyIdQueryBindable.unbind" should {

    "return JourneyId String representation" in {
      val journeyId = journeyIds.generateOne
      journeyIdQueryBindable.unbind("journeyId", journeyId) shouldBe journeyId.toString()
    }
  }
}
