package uk.gov.hmrc.personaldetailsvalidation.specs

import java.net.URLEncoder

import uk.gov.hmrc.personaldetailsvalidation.pages.ErrorPage
import uk.gov.hmrc.personaldetailsvalidation.pages.PersonalDetailsPage.personalDetailsPage
import uk.gov.hmrc.personaldetailsvalidation.support.BaseIntegrationSpec

class PersonalDetailsValidationStartISpec extends BaseIntegrationSpec {

  feature("Start Personal Details Validation journey") {

    scenario("Personal Details Validation journey started with a valid completionUrl") {

      When("I navigate to /personal-details-validation/start with a valid completionUrl")
      val completionUrl = URLEncoder.encode("/foobar?param1=value1&param2=value2", "UTF-8")
      goTo(s"/start?completionUrl=$completionUrl")

      Then("I should get redirected to the Personal Details page")
      on(personalDetailsPage(completionUrl))
    }

    scenario("Personal Details Validation journey started without a completionUrl parameter") {

      When("I navigate to /personal-details-validation/start without a completionUrl parameter")
      goTo("/start")

      Then("I should see the error page")
      on(ErrorPage())
    }

    scenario("Personal Details Validation journey started with an invalid completionUrl") {

      When("I navigate to /personal-details-validation/start with an invalid completionUrl")
      goTo("/start?completionUrl=http://foobar")

      Then("I should see the error page")
      on(ErrorPage())
    }
  }
}
