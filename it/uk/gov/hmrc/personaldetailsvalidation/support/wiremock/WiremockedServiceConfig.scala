package uk.gov.hmrc.personaldetailsvalidation.support.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import uk.gov.hmrc.personaldetailsvalidation.support.PersonalDetailsFrontendService

trait WiremockedServiceConfig {

  self: PersonalDetailsFrontendService =>

  val wiremockedServiceName: String = "personal-details-validation"
  val wiremockedServiceHost: String = "localhost"
  val wiremockedServicePort: Int = 11111

  WireMock.configureFor(wiremockedServiceHost, wiremockedServicePort)

  lazy val wiremockAdditionalConfiguration: Map[String, Any] = {
    println("serviceName: " + wiremockedServiceName)
    println("wiremockedServiceHost: " + wiremockedServiceHost)
    println("wiremockedServicePort: " + wiremockedServicePort)
    Map(
      s"microservice.services.$wiremockedServiceName.host" -> wiremockedServiceHost,
      s"microservice.services.$wiremockedServiceName.port" -> wiremockedServicePort
    )
    }

  override protected lazy val additionalConfiguration = wiremockAdditionalConfiguration

  val wireMockServer = new WireMockServer(wireMockConfig().port(wiremockedServicePort))

  protected def startWiremock(): Unit = {

    wireMockServer.start()
  }

  protected def stopWiremock(): Unit = wireMockServer.stop()

  protected def resetWiremock(): Unit = WireMock.reset()
}

