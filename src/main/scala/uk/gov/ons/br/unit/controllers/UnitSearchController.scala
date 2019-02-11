package uk.gov.ons.br.unit.controllers


import javax.inject._
import play.api.mvc._
import uk.gov.ons.br.actions.SearchActionMaker
import uk.gov.ons.br.http.SearchResultHandler
import uk.gov.ons.br.unit.models.UnitData

@Singleton
class UnitSearchController @Inject()(protected val controllerComponents: ControllerComponents,
                                     searchAction: SearchActionMaker[UnitData],
                                     responseFor: SearchResultHandler[UnitData]) extends BaseController {
  def searchFor(searchTerm: String): Action[AnyContent] = searchAction.forTerm(searchTerm) { request =>
    responseFor(request.searchResult)
  }
}
