package uk.gov.ons.br.unit.models


import play.api.libs.json.{Json, Writes}
import uk.gov.ons.br.models.Address

case class UnitData(unitref: String,
                    unitType: String,
                    name: String,
                    tradingStyle: Option[String],
                    legalStatus: String,
                    sic: String,
                    turnover: Option[Int],
                    payeJobs: Option[Int],
                    address: Address,
                    parentUnitref: Option[String])

object UnitData {
  implicit val writes: Writes[UnitData] = Json.writes[UnitData]
}