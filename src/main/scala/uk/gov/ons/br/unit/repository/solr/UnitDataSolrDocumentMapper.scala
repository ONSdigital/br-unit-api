package uk.gov.ons.br.unit.repository.solr


import org.apache.solr.common.SolrDocument
import org.slf4j.Logger
import uk.gov.ons.br.models.Address
import uk.gov.ons.br.repository.Field.{mandatoryStringNamed, optionalIntNamed, optionalStringNamed}
import uk.gov.ons.br.repository.solr.SolrDocumentMapper
import uk.gov.ons.br.repository.solr.SolrDocumentSupport.asFields
import uk.gov.ons.br.unit.models.UnitData

private[solr] object FieldNames {
  val UnitRef = "unit_id"
  val Name = "name"
  val TradingStyle = "trading_style"
  val LegalStatus = "legal_status"
  val Sic = "sic"
  val Turnover = "turnover"
  val PayeJobs = "paye_jobs"
  val UnitType = "unit_type"
  val ParentUnitRef = "parent_unit_id"

  object Address {
    val Line1 = "address1"
    val Line2 = "address2"
    val Line3 = "address3"
    val Line4 = "address4"
    val Line5 = "address5"
    val Postcode = "postcode"
  }
}

object UnitDataSolrDocumentMapper extends SolrDocumentMapper[UnitData] {

  override def fromDocument(document: SolrDocument)(implicit logger: Logger): Option[UnitData] = {
    import FieldNames._
    val fields = asFields(document)
    for {
      unitRef <- mandatoryStringNamed(UnitRef).apply(fields)
      name <- mandatoryStringNamed(Name).apply(fields)
      optTradingStyle = optionalStringNamed(TradingStyle).apply(fields)
      legalStatus <- mandatoryStringNamed(LegalStatus).apply(fields)
      sic <- mandatoryStringNamed(Sic).apply(fields)
      optTurnover <- optionalIntNamed(Turnover).apply(fields).toOption
      optPayeJobs <- optionalIntNamed(PayeJobs).apply(fields).toOption
      unitType <- mandatoryStringNamed(UnitType).apply(fields)
      optParentUnitRef = optionalStringNamed(ParentUnitRef).apply(fields)
      address <- toAddress(fields)
    } yield UnitData(
      unitRef,
      unitType,
      name,
      optTradingStyle,
      legalStatus,
      sic,
      optTurnover,
      optPayeJobs,
      address,
      optParentUnitRef
    )
  }

  private def toAddress(fields: Map[String, String])(implicit logger: Logger): Option[Address] = {
    import FieldNames.Address._
    for {
      line1 <- mandatoryStringNamed(Line1).apply(fields)
      optLine2 = optionalStringNamed(Line2).apply(fields)
      optLine3 = optionalStringNamed(Line3).apply(fields)
      optLine4 = optionalStringNamed(Line4).apply(fields)
      optLine5 = optionalStringNamed(Line5).apply(fields)
      postcode <- mandatoryStringNamed(Postcode).apply(fields)
    } yield Address(line1, optLine2, optLine3, optLine4, optLine5, postcode)
  }
}
