package uk.gov.ons.br.unit.controllers


import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.mvc.Results.Ok
import play.api.mvc._
import play.api.test.{FakeRequest, StubControllerComponentsFactory}
import uk.gov.ons.br.actions.{DefaultSearchActionMaker, SearchActionMaker}
import uk.gov.ons.br.repository.{SearchRepository, SearchResult}
import uk.gov.ons.br.test.UnitSpec
import uk.gov.ons.br.unit.controllers.UnitSearchControllerSpec.SearchTerm
import uk.gov.ons.br.unit.models.UnitData
import uk.gov.ons.br.unit.test.SampleUnit.SampleUnitData

import scala.concurrent.{ExecutionContext, Future}

class UnitSearchControllerSpec extends UnitSpec with MockFactory with ScalaFutures with GuiceOneAppPerTest {

  private trait Fixture extends StubControllerComponentsFactory {
    val searchResultHandler = mockFunction[SearchResult[UnitData], Result]
  }

  private trait MockActionFixture extends Fixture {
    val searchActionMaker = new SearchActionMaker[UnitData] {
      // ScalaMock cannot stub 'ActionBuilder[SearchRequest, AnyContent] with ActionTransformer[Request, SearchRequest]'
      trait ActionBuilderWithActionTransformer extends ActionBuilder[SearchRequest, AnyContent] with ActionTransformer[Request, SearchRequest]
      val anAction = stub[ActionBuilderWithActionTransformer]
      val delegateFn = mockFunction[String, ActionBuilderWithActionTransformer]

      override def forTerm(searchTerm: String): ActionBuilder[SearchRequest, AnyContent] with ActionTransformer[Request, SearchRequest] =
        delegateFn(searchTerm)
    }
    val underTest = new UnitSearchController(stubControllerComponents(), searchActionMaker, searchResultHandler)
  }

  /*
   * While Actions are the recommended element of reuse in Play, it does not seem particularly easy to mock/stub
   * their interactions.  We resort to using a real instance here.
   */
  private trait SearchActionFixture extends Fixture {
    implicit val executionContext = ExecutionContext.Implicits.global
    implicit val materializer = app.materializer
    val searchRepository = mock[SearchRepository[UnitData]]
    val searchActionMaker = new DefaultSearchActionMaker[UnitData](stubPlayBodyParsers.default, searchRepository)
    val underTest = new UnitSearchController(stubControllerComponents(), searchActionMaker, searchResultHandler)
  }

  "A Unit Search Controller" - {
    "requests a search via the search action" in new MockActionFixture {
      searchActionMaker.delegateFn.expects(SearchTerm).returning(searchActionMaker.anAction)

      underTest.searchFor(SearchTerm)
    }

    "generates a response via a search result handler" in new SearchActionFixture {
      val searchResult = Right(Seq(SampleUnitData))
      val handlerResult = Ok("some-representation-of-sample-unit-data")
      (searchRepository.searchFor _).expects(SearchTerm).returning(Future.successful(searchResult))
      searchResultHandler.expects(searchResult).returning(handlerResult)

      whenReady(underTest.searchFor(SearchTerm)(FakeRequest())) { result =>
        result shouldBe handlerResult
      }
    }
  }
}

private object UnitSearchControllerSpec {
  val SearchTerm = SampleUnitData.unitref
}