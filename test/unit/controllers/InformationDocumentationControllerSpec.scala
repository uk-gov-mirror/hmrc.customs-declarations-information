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

package unit.controllers

import controllers.Assets
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.customs.declarations.information.controllers.InformationDocumentationController
import uk.gov.hmrc.customs.declarations.information.logging.InformationLogger

class InformationDocumentationControllerSpec extends PlaySpec with MockitoSugar with Results with BeforeAndAfterEach {

  private val mockService = mock[HttpErrorHandler]
  private val mockLogger = mock[InformationLogger]

  private val v1AndV2Disabled = Map(
    "api.access.version-1.0.enabled" -> "false",
    "api.access.version-2.0.enabled" -> "false")

  private def getApiDefinitionWith(configMap: Map[String, Any]) =
    new InformationDocumentationController(mock[Assets], Helpers.stubControllerComponents(), play.api.Configuration.from(configMap), mockLogger)
      .definition()

  override def beforeEach() {
    reset(mockService)
  }

  "API Definition" should {

    "be correct when V1 & V2 are PUBLIC by default" in {
      val result = getApiDefinitionWith(Map())(FakeRequest())

      status(result) mustBe 200
      contentAsJson(result) mustBe expectedJson()
    }

    "be correct when V1 is not enabled and V2 is not enabled" in {
      val result = getApiDefinitionWith(v1AndV2Disabled)(FakeRequest())

      status(result) mustBe 200
      contentAsJson(result) mustBe expectedJson(false, false)
    }

  }

  private def expectedJson(v1Enabled: Boolean  = true, v2Enabled: Boolean = true) =
    Json.parse(
      s"""
         |{
         |   "scopes":[
         |      {
         |         "key":"write:customs-declarations-information",
         |         "name":"Request the status of a declaration",
         |         "description":"Request the status of a declaration"
         |      }
         |   ],
         |   "api":{
         |      "name":"Customs Declarations Information",
         |      "description":"Retrieve declaration status information",
         |      "context":"customs/declarations-information",
         |      "categories": ["CUSTOMS"],
         |      "versions":[
         |         {
         |            "version":"1.0",
         |            "status":"BETA",
         |            "endpointsEnabled":$v1Enabled,
         |            "access":{
         |              "type":"PRIVATE"
         |            },
         |            "fieldDefinitions":[
         |               {
         |                  "name": "authenticatedEori",
         |                  "description": "What's your Economic Operator Registration and Identification (EORI) number?",
         |                  "type": "STRING",
         |                  "hint": "This is your EORI that will associate your application with you as a CSP",
         |                  "shortDescription" : "EORI"
         |               }
         |            ]
         |         },
         |         {
         |            "version":"2.0",
         |            "status":"BETA",
         |            "endpointsEnabled":$v2Enabled,
         |            "access":{
         |               "type":"PRIVATE"
         |            },
         |            "fieldDefinitions":[
         |               {
         |                  "name": "authenticatedEori",
         |                  "description": "What's your Economic Operator Registration and Identification (EORI) number?",
         |                  "type": "STRING",
         |                  "hint": "This is your EORI that will associate your application with you as a CSP",
         |                  "shortDescription" : "EORI"
         |               }
         |            ]
         |         }
         |      ]
         |   }
         |}
      """.stripMargin)

}
