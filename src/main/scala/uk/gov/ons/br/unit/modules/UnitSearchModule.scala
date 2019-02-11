package uk.gov.ons.br.unit.modules


import com.google.inject.{AbstractModule, Provides}
import javax.inject.Inject
import play.api.mvc.PlayBodyParsers
import uk.gov.ons.br.actions.{DefaultSearchActionMaker, SearchActionMaker}
import uk.gov.ons.br.http.{JsonSearchResultHandler, SearchResultHandler}
import uk.gov.ons.br.repository.SearchRepository
import uk.gov.ons.br.unit.models.UnitData

import scala.concurrent.ExecutionContext

/*
 * This class must be listed in application.conf under 'play.modules.enabled' for this to be used.
 */
class UnitSearchModule extends AbstractModule {

  override def configure(): Unit = ()

  @Provides
  def providesSearchActionMaker(@Inject() bodyParsers: PlayBodyParsers,
                                searchRepository: SearchRepository[UnitData])
                               (implicit ec: ExecutionContext): SearchActionMaker[UnitData] =
    new DefaultSearchActionMaker[UnitData](bodyParsers.default, searchRepository)

  @Provides
  def providesSearchResultHandler: SearchResultHandler[UnitData] =
    JsonSearchResultHandler.apply[UnitData]
}
