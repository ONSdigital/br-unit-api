package uk.gov.ons.br.unit.modules


import com.github.ghik.silencer.silent
import com.google.inject.{AbstractModule, Provides}
import io.ino.solrs.AsyncSolrClient
import javax.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Environment}
import uk.gov.ons.br.config.AsyncSolrClientLoader
import uk.gov.ons.br.repository.SearchRepository
import uk.gov.ons.br.repository.solr.solrs.SolrsClientWrapper
import uk.gov.ons.br.repository.solr.{SolrClient, SolrSearchRepository}
import uk.gov.ons.br.unit.models.UnitData
import uk.gov.ons.br.unit.repository.solr.UnitDataSolrDocumentMapper

import scala.concurrent.{ExecutionContext, Future}

/*
 * This class must be listed in application.conf under 'play.modules.enabled' for this to be used.
 *
 * @silent unused - in order to have Configuration injected into this constructor, we must also accept
 *                  Environment.  Attempts to inject Configuration alone are met with: play.api.PlayException: No
 *                  valid constructors[Module [uk.gov.ons.br.unit.modules.UnitSolrModule] cannot be instantiated.]
 */
@SuppressWarnings(Array("UnusedMethodParameter"))
class UnitSolrModule(@silent environment: Environment, configuration: Configuration) extends AbstractModule {

  // We use a dedicated logger for Solr tracing (which should be configured in logback.xml)
  private lazy val solrLogger = LoggerFactory.getLogger("solr")

  override def configure(): Unit = {
    bind(classOf[SolrClient]).to(classOf[SolrsClientWrapper])
  }

  @Singleton
  @Provides
  def providesAsyncSolrClientLoader: AsyncSolrClientLoader =
    new AsyncSolrClientLoader(solrLogger)

  @Singleton
  @Provides
  def providesAsyncSolrClient(@Inject() applicationLifecycle: ApplicationLifecycle,
                              asyncSolrClientLoader: AsyncSolrClientLoader)
                             (implicit ec: ExecutionContext): AsyncSolrClient[Future] = {
    val asyncSolrClient = asyncSolrClientLoader.load(configuration.underlying, "search.db.solr")
    applicationLifecycle.addStopHook(() => Future(asyncSolrClient.shutdown()))
    asyncSolrClient
  }

  @Provides
  def providesSearchRepository(@Inject() solrClient: SolrClient)(implicit ec: ExecutionContext): SearchRepository[UnitData] =
    new SolrSearchRepository[UnitData](solrClient, UnitDataSolrDocumentMapper)(ec, solrLogger)
}
