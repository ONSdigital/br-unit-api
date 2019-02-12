package uk.gov.ons.br.unit.repository.solr


import org.apache.solr.common.SolrDocument
import org.scalamock.scalatest.MockFactory
import org.slf4j.Logger
import uk.gov.ons.br.test.UnitSpec
import uk.gov.ons.br.unit.repository.solr.UnitDataSolrDocumentMapperSpec.{AllFields, MandatoryFields, NumericFields}
import uk.gov.ons.br.unit.test.SampleUnit.{SampleUnitDataWithAllFields, SampleUnitDataWithOnlyMandatoryFields, Values}

import scala.collection.JavaConverters._

class UnitDataSolrDocumentMapperSpec extends UnitSpec with MockFactory {

  private trait Fixture {
    def sampleDocumentWith(fields: Map[String, AnyRef]): SolrDocument =
      new SolrDocument(fields.asJava)

    implicit val logger = stub[Logger]
    val underTest = UnitDataSolrDocumentMapper
  }

  "A UnitData SolrDocumentMapper" - {
    "can create a unit" - {
      "when all fields are defined" in new Fixture {
        val solrDocument = sampleDocumentWith(AllFields)

        underTest.fromDocument(solrDocument) shouldBe Some(SampleUnitDataWithAllFields)
      }

      "ignoring unrecognised fields" in new Fixture {
        val solrDocument = sampleDocumentWith(AllFields ++ Seq(
          "id" -> "id-value",
          "version" -> Long.box(System.currentTimeMillis())
        ))

        underTest.fromDocument(solrDocument) shouldBe Some(SampleUnitDataWithAllFields)
      }

      "when only the mandatory fields are defined" in new Fixture {
        val solrDocument = sampleDocumentWith(Map(
          FieldNames.UnitRef -> Values.UnitRef,
          FieldNames.Name -> Values.Name,
          FieldNames.LegalStatus -> Values.LegalStatus,
          FieldNames.Sic -> Values.Sic,
          FieldNames.UnitType -> Values.UnitType,
          FieldNames.Address.Line1 -> Values.AddressLine1,
          FieldNames.Address.Postcode -> Values.Postcode
        ))

        underTest.fromDocument(solrDocument) shouldBe Some(SampleUnitDataWithOnlyMandatoryFields)
      }
    }

    "cannot create a unit" - {
      "when any mandatory field is missing" in new Fixture {
        MandatoryFields.foreach { field =>
          withClue(s"with missing field [$field]") {
            val missingField = AllFields - field

            underTest.fromDocument(sampleDocumentWith(missingField)) shouldBe None
          }
        }
      }

      "when any numeric column" - {
        "contains a non-numeric value" in new Fixture {
          NumericFields.foreach { field =>
            withClue(s"with a non-numeric value for field [$field]") {
              val badField = AllFields - field + (field -> "not-a-number")

              underTest.fromDocument(sampleDocumentWith(badField)) shouldBe None
            }
          }
        }

        "contains a non-integral value" in new Fixture {
          NumericFields.foreach { field =>
            withClue(s"with a non-integral value for field [$field]") {
              val badField = AllFields - field + (field -> Double.box(3.14159))

              underTest.fromDocument(sampleDocumentWith(badField)) shouldBe None
            }
          }
        }
      }
    }
  }
}

private object UnitDataSolrDocumentMapperSpec {
  val AllFields = Map(
    FieldNames.UnitRef -> Values.UnitRef,
    FieldNames.Name -> Values.Name,
    FieldNames.TradingStyle -> Values.TradingStyle,
    FieldNames.LegalStatus -> Values.LegalStatus,
    FieldNames.Sic -> Values.Sic,
    FieldNames.Turnover -> Int.box(Values.Turnover),
    FieldNames.PayeJobs -> Int.box(Values.PayeJobs),
    FieldNames.UnitType -> Values.UnitType,
    FieldNames.ParentUnitRef -> Values.ParentUnitRef,
    FieldNames.Address.Line1 -> Values.AddressLine1,
    FieldNames.Address.Line2 -> Values.AddressLine2,
    FieldNames.Address.Line3 -> Values.AddressLine3,
    FieldNames.Address.Line4 -> Values.AddressLine4,
    FieldNames.Address.Line5 -> Values.AddressLine5,
    FieldNames.Address.Postcode -> Values.Postcode
  )

  val MandatoryFields = Seq(
    FieldNames.UnitRef,
    FieldNames.Name,
    FieldNames.LegalStatus,
    FieldNames.Sic,
    FieldNames.UnitType,
    FieldNames.Address.Line1,
    FieldNames.Address.Postcode
  )

  val NumericFields = Seq(
    FieldNames.Turnover,
    FieldNames.PayeJobs
  )
}