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

package uk.gov.hmrc.views

import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.{TableDrivenPropertyChecks, Tables}
import play.api.http.HttpConfiguration
import play.api.i18n.{DefaultLangsProvider, Lang}
import play.api.{Configuration, Environment}
import setups.configs.ConfigSetup
import support.UnitSpec
import uk.gov.hmrc.config.DwpMessagesApiProvider

class ViewConfigSpec
  extends UnitSpec
    with TableDrivenPropertyChecks
    with MockFactory {

  private val scenarios = Tables.Table(
    ("propertyName",   "propertyAccessor",                            "configKey"),
    ("analyticsHost",  (config: ViewConfig) => config.analyticsHost,  "google-analytics.host"),
    ("analyticsToken", (config: ViewConfig) => config.analyticsToken, "google-analytics.token")
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

  "languagesMap" should {

    "return a map of language descriptions associated with Lang objects" in new Setup {
      expectMessagesFilesExistsFor("default", "cy")

      whenConfigEntriesExists(
        "play.i18n.langs" -> List("en", "cy"),
        "play.i18n.descriptions" -> Map("en" -> "english", "cy" -> "cymraeg")
      ) { config =>
        config.languageMap shouldBe Map(
          "english" -> Lang("en"),
          "cymraeg" -> Lang("cy")
        )
      }
    }

    "throw a runtime exception when there's no messages file defined for a code from 'play.i18n.langs'" in new Setup2 {
      expectMessagesFilesExistsFor("default")

      whenConfigEntriesExists(
        "play.i18n.langs" -> List("en", "cy"),
        "play.i18n.descriptions" -> Map("en" -> "english", "cy" -> "cymraeg")
      ) { config =>
        a[RuntimeException] should be thrownBy config.languageMap
      }
    }

    "throw a runtime exception when there's no language description defined for a code in 'play.i18n.langs'" in new Setup {
      expectMessagesFilesExistsFor("default", "cy")

      whenConfigEntriesExists(
        "play.i18n.langs" -> List("en", "cy", "pl"),
        "play.i18n.descriptions" -> Map("en" -> "english", "cy" -> "cymraeg")
      ) { config =>
        a[RuntimeException] should be thrownBy config.languageMap
      }
    }
  }

  private trait Setup extends ConfigSetup[ViewConfig] {

    val configuration = Configuration.from(Map(
      "play.i18n.langs" -> List("en", "cy"),
      "play.i18n.path" -> null,
      "play.i18n.langCookieName" -> "PLAY_LANG",
      "play.i18n.langCookieSameSite" -> "strict",
      "play.i18n.langCookieSecure" -> true,
      "play.i18n.langCookieHttpOnly" -> false
    ))

    val dwpMessagesApiProvider = new DwpMessagesApiProvider(Environment.simple(), configuration,
      new DefaultLangsProvider(configuration).get, HttpConfiguration())

    val newConfigObject: Configuration => ViewConfig = new ViewConfig(_, dwpMessagesApiProvider)

    def expectMessagesFilesExistsFor(codes: String*) = {
      dwpMessagesApiProvider.get.messages
    }
  }

  private trait Setup2 extends ConfigSetup[ViewConfig] {

    val configuration = Configuration.from(Map(
      "play.i18n.langs" -> List("default"),
      "play.i18n.path" -> null,
      "play.i18n.langCookieName" -> "PLAY_LANG",
      "play.i18n.langCookieSameSite" -> "strict",
      "play.i18n.langCookieSecure" -> true,
      "play.i18n.langCookieHttpOnly" -> false
    ))

    val messagesApi = new DwpMessagesApiProvider(Environment.simple(), configuration,
      new DefaultLangsProvider(configuration).get, HttpConfiguration())
    val newConfigObject: Configuration => ViewConfig = new ViewConfig(_, messagesApi)

    def expectMessagesFilesExistsFor(codes: String*) = {
      messagesApi.get.messages
    }
  }
}
