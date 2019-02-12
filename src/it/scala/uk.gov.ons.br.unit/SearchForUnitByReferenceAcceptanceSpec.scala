package uk.gov.ons.br.unit


import com.github.tomakehurst.wiremock.client.MappingBuilder
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{Json, Reads}
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON
import uk.gov.ons.br.models.Address
import uk.gov.ons.br.test.matchers.HttpServerErrorStatusCodeMatcher.aServerError
import uk.gov.ons.br.test.solr.SolrResponseBuilder.{aDocument, ofSolrJavaBinResponseFor}
import uk.gov.ons.br.test.solr.{AbstractServerAcceptanceSpec, SolrJsonRequestBuilder}
import uk.gov.ons.br.unit.SearchForUnitByReferenceAcceptanceSpec.{Postcode, UnitRef, aSearchRequest, readsResult}
import uk.gov.ons.br.unit.models.UnitData

class SearchForUnitByReferenceAcceptanceSpec extends AbstractServerAcceptanceSpec {

  // must match that configured in src/it/resources/it_application.conf
  override val SolrPort: Int = 8984

  info("As a data explorer")
  info("I want to search across units by a search term")
  info("So that I can build a picture of a business")

  feature("search across units for a search term") {
    scenario("when a single match is found") { wsClient =>
      Given(s"a unit exists with reference $UnitRef")
      stubSolrFor(aSearchRequest(forTerm = UnitRef).willReturn(
        anOkResponse().withBody(
          ofSolrJavaBinResponseFor(query = UnitRef)(
            aDocument(
              "unit_id" -> UnitRef,
              "name" -> "BIG BOX CEREAL 52 LTD",
              "legal_status" -> "1",
              "sic" -> "10612",
              "turnover" -> Int.box(10),
              "paye_jobs" -> Int.box(1),
              "address1" -> "(ROOM 9/10)",
              "address2" -> "LEEGOMERY COMMUNITY CENTRE",
              "address3" -> "LEEGATE AVENUE",
              "address4" -> "TELFORD",
              "postcode" -> "TF1 6NA",
              "unit_type" -> "LEGAL_UNIT",
              "parent_unit_id" -> "1100000063"
            )
          )
        )
      ))

      When(s"a search is requested for the term $UnitRef")
      val response = await(wsClient.url(s"/v1/unit?searchTerm=$UnitRef").get())

      Then(s"the details of the unit with reference $UnitRef are returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE).value shouldBe JSON
      response.json.as[Seq[UnitData]](readsResult) shouldBe Seq(
        UnitData(
          unitref = UnitRef,
          unitType = "LEGAL_UNIT",
          name = "BIG BOX CEREAL 52 LTD",
          tradingStyle = None,
          legalStatus = "1",
          sic = "10612",
          turnover = Some(10),
          payeJobs = Some(1),
          address = Address(
            line1 = "(ROOM 9/10)",
            line2 = Some("LEEGOMERY COMMUNITY CENTRE"),
            line3 = Some("LEEGATE AVENUE"),
            line4 = Some("TELFORD"),
            line5 = None,
            postcode = "TF1 6NA"
          ),
          parentUnitref = Some("1100000063")
        )
      )
    }

    scenario("when no match is found") { wsClient =>
      Given("a search will find no matches")
      stubSolrFor(aSearchRequest(forTerm = UnitRef).willReturn(
        anOkResponse().withBody(
          ofSolrJavaBinResponseFor(query = UnitRef)()
        )
      ))

      When(s"a search is requested for the term $UnitRef")
      val response = await(wsClient.url(s"/v1/unit?searchTerm=$UnitRef").get())

      Then("an empty response is returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE).value shouldBe JSON
      response.json.as[Seq[UnitData]](readsResult) shouldBe Seq.empty
    }

    scenario("when multiple matches are found") { wsClient =>
      Given(s"multiple units exist with postcode $Postcode")
      stubSolrFor(aSearchRequest(forTerm = "TF1+6NA").willReturn(
        anOkResponse().withBody(
          ofSolrJavaBinResponseFor(query = "TF1+6NA")(
            aDocument(
              "unit_id" -> UnitRef,
              "name" -> "BIG BOX CEREAL 52 LTD",
              "legal_status" -> "1",
              "sic" -> "10612",
              "turnover" -> Int.box(10),
              "paye_jobs" -> Int.box(1),
              "address1" -> "(ROOM 9/10)",
              "address2" -> "LEEGOMERY COMMUNITY CENTRE",
              "address3" -> "LEEGATE AVENUE",
              "address4" -> "TELFORD",
              "postcode" -> Postcode,
              "unit_type" -> "LEGAL_UNIT",
              "parent_unit_id" -> "1100000063"
            ),
            aDocument(
              "unit_id" -> "553769640200",
              "name" -> "BIG BOX CEREAL LIMITED",
              "legal_status" -> "A",
              "sic" -> "10612",
              "turnover" -> Int.box(10),
              "address1" -> "(ROOM 9/10)",
              "address2" -> "LEEGOMERY COMMUNITY CENTRE",
              "address3" -> "LEEGATE AVENUE",
              "address4" -> "TELFORD",
              "postcode" -> Postcode,
              "unit_type" -> "VAT",
              "parent_unit_id" -> UnitRef
            ),
          )
        )
      ))

      When(s"a search is requested for the term $Postcode")
      val response = await(wsClient.url(s"/v1/unit?searchTerm=TF1+6NA").get())

      Then(s"the details of the units with postcode $Postcode are returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE).value shouldBe JSON
      response.json.as[Seq[UnitData]](readsResult) shouldBe Seq(
        UnitData(
          unitref = UnitRef,
          unitType = "LEGAL_UNIT",
          name = "BIG BOX CEREAL 52 LTD",
          tradingStyle = None,
          legalStatus = "1",
          sic = "10612",
          turnover = Some(10),
          payeJobs = Some(1),
          address = Address(
            line1 = "(ROOM 9/10)",
            line2 = Some("LEEGOMERY COMMUNITY CENTRE"),
            line3 = Some("LEEGATE AVENUE"),
            line4 = Some("TELFORD"),
            line5 = None,
            postcode = Postcode
          ),
          parentUnitref = Some("1100000063")
        ),
        UnitData(
          unitref = "553769640200",
          unitType = "VAT",
          name = "BIG BOX CEREAL LIMITED",
          tradingStyle = None,
          legalStatus = "A",
          sic = "10612",
          turnover = Some(10),
          payeJobs = None,
          address = Address(
            line1 = "(ROOM 9/10)",
            line2 = Some("LEEGOMERY COMMUNITY CENTRE"),
            line3 = Some("LEEGATE AVENUE"),
            line4 = Some("TELFORD"),
            line5 = None,
            postcode = Postcode
          ),
          parentUnitref = Some(UnitRef)
        )
      )
    }
  }

  feature("validate the request") {
    scenario("when a search term is not specified") { wsClient =>
      When(s"a search is requested without a search term")
      val response = await(wsClient.url(s"/v1/unit").get())

      Then("a Bad Request response is returned")
      response.status shouldBe BAD_REQUEST
    }
  }

  feature("handle failure gracefully") {
    scenario("when the search engine is unavailable") { wsClient =>
      Given("the search engine is unavailable")
      stopMockSolr()

      When(s"a search is requested for the term $UnitRef")
      val response = await(wsClient.url(s"/v1/unit?searchTerm=$UnitRef").get())

      Then("a server error is returned")
      response.status shouldBe aServerError
    }

    scenario("when the search engine returns an error response") { wsClient =>
      Given("the search engine will return an error response")
      stubSolrFor(aSearchRequest(forTerm = UnitRef).willReturn(
        aServiceUnavailableResponse()
      ))

      When(s"a search is requested for the term $UnitRef")
      val response = await(wsClient.url(s"/v1/unit?searchTerm=$UnitRef").get())

      Then("an Internal Server Error is returned")
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    scenario("when some matches do not comply with the expected document schema") { wsClient =>
      Given("name is a mandatory field")
      And(s"a search for the term $Postcode will find one matching document with a name field and one without")
      stubSolrFor(aSearchRequest(forTerm = "TF1+6NA").willReturn(
        anOkResponse().withBody(
          ofSolrJavaBinResponseFor(query = "TF1+6NA")(
            aDocument(
              "unit_id" -> UnitRef,
              "legal_status" -> "1",
              "sic" -> "10612",
              "turnover" -> Int.box(10),
              "paye_jobs" -> Int.box(1),
              "address1" -> "(ROOM 9/10)",
              "address2" -> "LEEGOMERY COMMUNITY CENTRE",
              "address3" -> "LEEGATE AVENUE",
              "address4" -> "TELFORD",
              "postcode" -> Postcode,
              "unit_type" -> "LEGAL_UNIT",
              "parent_unit_id" -> "1100000063"
            ),
            aDocument(
              "unit_id" -> "553769640200",
              "name" -> "BIG BOX CEREAL LIMITED",
              "legal_status" -> "A",
              "sic" -> "10612",
              "turnover" -> Int.box(10),
              "address1" -> "(ROOM 9/10)",
              "address2" -> "LEEGOMERY COMMUNITY CENTRE",
              "address3" -> "LEEGATE AVENUE",
              "address4" -> "TELFORD",
              "postcode" -> Postcode,
              "unit_type" -> "VAT",
              "parent_unit_id" -> UnitRef
            ),
          )
        )
      ))

      When(s"a search is requested for the term $Postcode")
      val response = await(wsClient.url(s"/v1/unit?searchTerm=TF1+6NA").get())

      Then(s"the only the details of the matching unit that has a name field is returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE).value shouldBe JSON
      response.json.as[Seq[UnitData]](readsResult) shouldBe Seq(
        UnitData(
          unitref = "553769640200",
          unitType = "VAT",
          name = "BIG BOX CEREAL LIMITED",
          tradingStyle = None,
          legalStatus = "A",
          sic = "10612",
          turnover = Some(10),
          payeJobs = None,
          address = Address(
            line1 = "(ROOM 9/10)",
            line2 = Some("LEEGOMERY COMMUNITY CENTRE"),
            line3 = Some("LEEGATE AVENUE"),
            line4 = Some("TELFORD"),
            line5 = None,
            postcode = Postcode
          ),
          parentUnitref = Some(UnitRef)
        )
      )
    }

    scenario("when all matches do not comply with the expected document schema") { wsClient =>
      Given("name is a mandatory field")
      And(s"a search for the term $UnitRef will find a single matching document without a name field")
      stubSolrFor(aSearchRequest(forTerm = UnitRef).willReturn(
        anOkResponse().withBody(
          ofSolrJavaBinResponseFor(query = UnitRef)(
            aDocument(
              "unit_id" -> UnitRef,
              "legal_status" -> "1",
              "sic" -> "10612",
              "turnover" -> Int.box(10),
              "paye_jobs" -> Int.box(1),
              "address1" -> "(ROOM 9/10)",
              "address2" -> "LEEGOMERY COMMUNITY CENTRE",
              "address3" -> "LEEGATE AVENUE",
              "address4" -> "TELFORD",
              "postcode" -> "TF1 6NA",
              "unit_type" -> "LEGAL_UNIT",
              "parent_unit_id" -> "1100000063"
            )
          )
        )
      ))

      When(s"a search is requested for the term $UnitRef")
      val response = await(wsClient.url(s"/v1/unit?searchTerm=$UnitRef").get())

      Then("an Internal Server Error is returned")
      response.status shouldBe INTERNAL_SERVER_ERROR
    }
  }
}

private object SearchForUnitByReferenceAcceptanceSpec extends SolrJsonRequestBuilder {
  val UnitRef = "1000012345000039"
  val Postcode = "TF1 6NA"

  // must match the configuration at src/it/resources/it_application.conf
  def aSearchRequest(forTerm: String): MappingBuilder =
    aSearchRequest(ofCollection = "unit")(forTerm)

  private implicit val readsAddress: Reads[Address] = Json.reads[Address]
  private implicit val readsUnitData: Reads[UnitData] = Json.reads[UnitData]
  val readsResult: Reads[Seq[UnitData]] = Reads.seq[UnitData]
}
