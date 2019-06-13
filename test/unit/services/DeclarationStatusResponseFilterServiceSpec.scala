/*
 * Copyright 2019 HM Revenue & Customs
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

package unit.services

import org.joda.time.{DateTime, DateTimeZone}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.customs.declarations.information.logging.InformationLogger
import uk.gov.hmrc.customs.declarations.information.services.{InformationConfigService, StatusResponseFilterService}
import uk.gov.hmrc.play.test.UnitSpec
import util.StatusTestXMLData.{DeclarationType, ImportTradeMovementType, generateDeclarationStatusResponse, generateValidStatusResponseWithMultiplePartiesOnly}

import scala.xml.NodeSeq

class DeclarationStatusResponseFilterServiceSpec extends UnitSpec with MockitoSugar {

  private val acceptanceDateVal = DateTime.now(DateTimeZone.UTC)

  trait SetUp {

    val mockInformationLogger: InformationLogger = mock[InformationLogger]
    val mockInformationConfigService: InformationConfigService = mock[InformationConfigService]

    val service = new StatusResponseFilterService(mockInformationLogger, mockInformationConfigService)

    def createStatusResponseWithAllValues(): NodeSeq = service.transform(generateDeclarationStatusResponse(acceptanceOrCreationDate = acceptanceDateVal))
  }

  "Status Response Filter Service" should {

    "create the version number" in new SetUp {
      private val response = createStatusResponseWithAllValues()
      private val node = response \\ "VersionID"

      node.text shouldBe "0"
    }

    "create the mrn" in new SetUp {
      private val response = createStatusResponseWithAllValues()
      private val node = response \ "Declaration" \ "ID"

      node.text shouldBe "mrn"
    }

    "create the creation date" in new SetUp {
      private val response = createStatusResponseWithAllValues()
      private val node = response \\ "CreationDateTime" \\ "DateTimeString"

      node.text shouldBe "2001-12-17T09:30:47Z"
      node.head.attribute("formatCode").get.text shouldBe "string"
    }

    "create the goods item count" in new SetUp {
      private val response = createStatusResponseWithAllValues()
      private val node = response \\ "GoodsItemQuantity"

      node.text shouldBe "2"
      node.head.attribute("unitType").get.text shouldBe "101"
    }

    "create the type code" in new SetUp {
      private val response = createStatusResponseWithAllValues()
      private val node = response \\ "TypeCode"

      node.text shouldBe ImportTradeMovementType + DeclarationType
    }

    "create the TotalPackageQuantity" in new SetUp {
      private val response = createStatusResponseWithAllValues()
      private val node = response \\ "TotalPackageQuantity"

      node.text shouldBe "3"
    }

    "create the acceptance date" in new SetUp {
      private val response = createStatusResponseWithAllValues()
      private val node = response \\ "AcceptanceDateTime" \\ "DateTimeString"
      node.head.text shouldBe acceptanceDateVal.toString("yyyy-MM-dd'T'HH:mm:ss'Z'")
    }

    "create the TB party identification number" in new SetUp {
      private val response = createStatusResponseWithAllValues()
      private val node = response \\ "Submitter" \ "ID"

      node.head.text shouldBe "123456"
    }

    "create the party identification number when there is one with TB type and the rest are not" in new SetUp{
      private val response = service.transform(generateValidStatusResponseWithMultiplePartiesOnly)
      private val node = response \\ "Submitter" \ "ID"

      node.head.text shouldBe "1"
    }

    "not create acceptance date when not provided" in new SetUp {
      private val response = service.transform(generateValidStatusResponseWithMultiplePartiesOnly)
      private val node = response \\ "AcceptanceDateTime" \\ "DateTimeString"

      node shouldBe empty
    }

    "not create submitter id when not provided" in new SetUp {
      private val response = service.transform(generateDeclarationStatusResponse(acceptanceOrCreationDate = acceptanceDateVal, partyType = "TT"))
      private val node = response \\ "Submitter" \ "ID"

      node shouldBe empty
    }

  }
}
