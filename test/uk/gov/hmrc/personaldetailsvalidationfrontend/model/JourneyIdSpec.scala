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

package uk.gov.hmrc.personaldetailsvalidationfrontend.model

import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import uk.gov.hmrc.personaldetailsvalidationfrontend.generators.Generators._
import uk.gov.hmrc.play.test.UnitSpec

class JourneyIdSpec extends UnitSpec with GeneratorDrivenPropertyChecks {

  "JourneyId" should {

    "be instantiatable for any non-empty string" in {
      forAll(Gen.uuid.map(_.toString)) { value =>
        JourneyId(value).toString() shouldBe value
      }
    }

    "throw an exception for a non-uuid string" in {
      forAll(nonEmptyStrings) { value =>
        an[IllegalArgumentException] should be thrownBy JourneyId("")
      }
    }

    "throw an exception for an empty string" in {
      an[IllegalArgumentException] should be thrownBy JourneyId("")
    }
  }
}
