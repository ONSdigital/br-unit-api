package uk.gov.ons.br.unit.models

import play.api.libs.json.Json
import uk.gov.ons.br.test.UnitSpec
import uk.gov.ons.br.test.json.JsonString
import uk.gov.ons.br.test.json.JsonString.{withObject, withOptionalInt, withOptionalString, withString}
import uk.gov.ons.br.unit.test.SampleUnit.{SampleUnitDataWithAllFields, SampleUnitDataWithOnlyMandatoryFields}

class UnitDataSpec extends UnitSpec {

  private trait Fixture {
    def expectedJsonStrOf(unitData: UnitData): String = {
      JsonString.ofObject(
        withString(named = "unitref", withValue = unitData.unitref),
        withString(named = "unitType", withValue = unitData.unitType),
        withString(named = "name", withValue = unitData.name),
        withOptionalString(named = "tradingStyle", withValue = unitData.tradingStyle),
        withString(named = "legalStatus", withValue = unitData.legalStatus),
        withString(named = "sic", withValue = unitData.sic),
        withOptionalInt(named = "turnover", withValue = unitData.turnover),
        withOptionalInt(named = "payeJobs", withValue = unitData.payeJobs),
        withObject(named = "address",
          withString(named = "line1", withValue = unitData.address.line1),
          withOptionalString(named = "line2", withValue = unitData.address.line2),
          withOptionalString(named = "line3", withValue = unitData.address.line3),
          withOptionalString(named = "line4", withValue = unitData.address.line4),
          withOptionalString(named = "line5", withValue = unitData.address.line5),
          withString(named = "postcode", withValue = unitData.address.postcode)
        ),
        withOptionalString(named = "parentUnitref", withValue = unitData.parentUnitref)
      )
    }
  }

  "Unit Data" - {
    "can be represented in Json" - {
      "when all fields are defined" in new Fixture {
        Json.toJson(SampleUnitDataWithAllFields) shouldBe Json.parse(expectedJsonStrOf(SampleUnitDataWithAllFields))
      }

      "when only mandatory fields are defined" in new Fixture {
        Json.toJson(SampleUnitDataWithOnlyMandatoryFields) shouldBe Json.parse(expectedJsonStrOf(SampleUnitDataWithOnlyMandatoryFields))
      }
    }
  }
}
