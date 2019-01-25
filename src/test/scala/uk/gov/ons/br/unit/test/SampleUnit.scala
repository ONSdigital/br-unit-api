package uk.gov.ons.br.unit.test


import uk.gov.ons.br.models.Address
import uk.gov.ons.br.unit.models.UnitData

object SampleUnit {
  import Values._

  val SampleUnitDataWithAllFields = UnitData(
      unitref = UnitRef,
      unitType = UnitType,
      name = Name,
      tradingStyle = Some(TradingStyle),
      legalStatus = LegalStatus,
      sic = Sic,
      turnover = Some(Turnover),
      payeJobs = Some(PayeJobs),
      address = Address(
        line1 = AddressLine1,
        line2 = Some(AddressLine2),
        line3 = Some(AddressLine3),
        line4 = Some(AddressLine4),
        line5 = Some(AddressLine5),
        postcode = Postcode
      ),
      parentUnitref = Some(ParentUnitRef)
    )

  val SampleUnitDataWithOnlyMandatoryFields =
    SampleUnitDataWithAllFields.copy(
      tradingStyle = None,
      turnover = None,
      payeJobs = None,
      address = SampleUnitDataWithAllFields.address.copy(
        line2 = None,
        line3 = None,
        line4 = None,
        line5 = None
      ),
      parentUnitref = None
    )

  val SampleUnitData = SampleUnitDataWithAllFields

  object Values {
    val UnitRef = "1000012345000039"
    val UnitType = "LEGAL_UNIT"
    val Name = "BIG BOX CEREAL 52 LTD"
    val TradingStyle = "BIG BOX CEREALS"
    val LegalStatus = "1"
    val Sic = "10612"
    val Turnover = 10
    val PayeJobs = 1
    val AddressLine1 = "(ROOM 9/10)"
    val AddressLine2 = "LEEGOMERY COMMUNITY CENTRE"
    val AddressLine3 = "LEEGATE AVENUE"
    val AddressLine4 = "TELFORD"
    val AddressLine5 = "SHROPSHIRE"
    val Postcode = "TF1 6NA"
    val ParentUnitRef = "1100000063"
  }
}
