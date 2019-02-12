package uk.gov.ons.br.unit.controllers


import com.google.inject.{AbstractModule, TypeLiteral}
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestData
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import play.mvc.Http.HttpVerbs.GET
import uk.gov.ons.br.repository.SearchRepository
import uk.gov.ons.br.test.UnitSpec
import uk.gov.ons.br.unit.models.UnitData

import scala.concurrent.Future

class UnitSearchControllerRoutingSpec extends UnitSpec with GuiceOneAppPerTest with MockFactory {
  /*
   * Stub the repository layer so that a valid requests receive a success search result.
   */
   override def newAppForTest(testData: TestData): Application = {
     val searchRepository = stub[SearchRepository[UnitData]]
     (searchRepository.searchFor _).when(*).returns(Future.successful(Right(Seq.empty)))

     val fakeModule = new AbstractModule {
       override def configure(): Unit = {
         bind(new TypeLiteral[SearchRepository[UnitData]]() {}).toInstance(searchRepository)
         ()
       }
     }

     new GuiceApplicationBuilder().overrides(fakeModule).build()
   }

  private trait Fixture {
    def fakeRequestTo(uri: String): FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest(method = GET, path = uri)
  }

  "A search request" - {
    "is accepted when a searchTerm is supplied" in new Fixture {
      val result = route(app, fakeRequestTo("/v1/unit?searchTerm=foo"))

      status(result.value) shouldBe OK
    }

    "is rejected when a searchTerm is not supplied" in new Fixture {
      val result = route(app, fakeRequestTo("/v1/unit"))

      status(result.value) shouldBe BAD_REQUEST
    }
  }
}
