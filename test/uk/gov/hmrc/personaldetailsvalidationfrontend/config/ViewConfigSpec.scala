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

package uk.gov.hmrc.personaldetailsvalidationfrontend.config

import org.scalatest.prop.{TableDrivenPropertyChecks, Tables}
import play.api.Configuration
import uk.gov.hmrc.play.test.UnitSpec

class ViewConfigSpec extends UnitSpec with TableDrivenPropertyChecks {

  private val scenarios = Tables.Table(
    ("propertyName",   "propertyAccessor",                            "configKey"),
    ("analyticsHost",  (config: ViewConfig) => config.analyticsHost,  "google-analytics.host"),
    ("analyticsToken", (config: ViewConfig) => config.analyticsToken, "google-analytics.token"),
    ("assetsUrl",      (config: ViewConfig) => config.assetsUrl,      "assets.url"),
    ("assetsVersion",  (config: ViewConfig) => config.assetsVersion,  "assets.version")
  )

  forAll(scenarios) { (propertyName, propertyAccessor, configKey) =>
    s"$propertyName" should {

      s"return value associated with '$configKey'" in new Setup {
        whenConfigEntriesExists(configKey -> "some-value") { config =>
          propertyAccessor(config) shouldBe "some-value"
        }
      }

      s"throw a runtime exception when there's no value for '$configKey'" in new Setup {
        whenConfigEntriesExists() { config =>
          a[RuntimeException] should be thrownBy propertyAccessor(config)
        }
      }
    }
  }

  "optimizelyBaseUrl" should {

    "return value associated with 'optimizely.url'" in new Setup {
      whenConfigEntriesExists("optimizely.url" -> "some-value") { config =>
        config.optimizelyBaseUrl shouldBe "some-value"
      }
    }

    "return empty String if there's no value for 'optimizely.url'" in new Setup {
      whenConfigEntriesExists() { config =>
        config.optimizelyBaseUrl shouldBe ""
      }
    }
  }

  "optimizelyProjectId" should {

    "return value associated with 'optimizely.projectId'" in new Setup {
      whenConfigEntriesExists("optimizely.projectId" -> "some-value") { config =>
        config.optimizelyProjectId shouldBe Some("some-value")
      }
    }

    "return None if there's no value for 'optimizely.projectId'" in new Setup {
      whenConfigEntriesExists() { config =>
        config.optimizelyProjectId shouldBe None
      }
    }
  }

  private trait Setup {

    def whenConfigEntriesExists(entries: (String, String)*)
                               (testBody: ViewConfig => Unit): Unit =
      testBody(new ViewConfig(Configuration.from(entries.toMap)))
  }
}
