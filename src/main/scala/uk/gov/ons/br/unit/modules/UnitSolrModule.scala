package uk.gov.ons.br.unit.modules


import com.github.ghik.silencer.silent
import com.google.inject.{AbstractModule, Provides}
import io.ino.solrs.AsyncSolrClient
import javax.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Environment}
import uk.gov.ons.br.config.{BaseUrlConfigLoader, SolrClientConfig, SolrClientConfigLoader}
import uk.gov.ons.br.repository.SearchRepository
import uk.gov.ons.br.repository.solr.solrs.{AsyncSolrClientMaker, SolrsClientWrapper}
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
    bind(classOf[SolrClientConfig]).toInstance(loadSolrClientConfig())
    bind(classOf[SolrClient]).to(classOf[SolrsClientWrapper])
  }

  private def loadSolrClientConfig(): SolrClientConfig = {
    val solrClientConfigLoader = new SolrClientConfigLoader(BaseUrlConfigLoader)
    solrClientConfigLoader.load(configuration.underlying, "search.db.solr")
  }

  @Singleton
  @Provides
  def providesAsyncSolrClient(@Inject() applicationLifecycle: ApplicationLifecycle,
                              solrClientConfig: SolrClientConfig)
                             (implicit ec: ExecutionContext): AsyncSolrClient[Future] = {
    val asyncSolrClient = AsyncSolrClientMaker.asyncSolrClientFor(solrClientConfig)
    applicationLifecycle.addStopHook(() => Future(asyncSolrClient.shutdown()))
    asyncSolrClient
  }

  @Provides
  def providesSearchRepository(@Inject() solrClient: SolrClient)(implicit ec: ExecutionContext): SearchRepository[UnitData] =
    new SolrSearchRepository[UnitData](solrClient, UnitDataSolrDocumentMapper)(ec, solrLogger)
}
