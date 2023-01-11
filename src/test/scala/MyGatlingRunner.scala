import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder
import Simulations.{CacheableUrlsLambdas, ConcurrencySessionManager, CsMobile, CsMobileToEpgService, EpgService, EpgServicePercentageSplit, GetLegacyCatalogues, VodAuth, CsVodMedia, DsEPGTests, DsPayVod, DsPayVodPosts, UniversalPlus}
import BoxOffice.{BoxOfficeTest, CustomerRewards, Hashtool, PaymentGateway, ProfileManager,BORental}
import BoxOfficeDSTV.BOMovieRental
import SafeMode.SafeModeLambda

import Productbacklog.Continuewatching
import Connect.{AWSSigninGet, ConnectCreateJwtToken, ShowmaxSeach, registration, signin,ShowmaxRegistration,ShowmaxSignin,ShowmaxConnectProfile,ResetmailConnectProfile}

import Connect.{AWSSigninGet, ConnectCreateJwtToken, ShowmaxSeach, registration, signin}


import UserJourneys.{CsMobileNavigateCatchUpAndLiveTvJWT, CsMobileNavigateCatchUpAndLiveTvJWT2, CsMobileNavigateHomeSearchAndMyListJWT, GetCatalogueByPackageAndCountry, GetCatalogueByPackageAndCountrySplit, DsUniversalTests}

//import UserJourneys.{CsMobileNavigateCatchUpAndLiveTvJWT, CsMobileNavigateCatchUpAndLiveTvJWT2, CsMobileNavigateHomeSearchAndMyListJWT, GetCatalogueByPackageAndCountry, GetCatalogueByPackageAndCountrySplit, AWSCatalogue}


object MyGatlingRunner {

  def main(args: Array[String]): Unit = {




    val simClass = classOf[Connect.ConnectJwtTokenRefresh].getName



    val props = new GatlingPropertiesBuilder
    props.simulationClass(simClass)

    Gatling.fromMap(props.build)
  }
}
