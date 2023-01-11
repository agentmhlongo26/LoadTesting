package Simulations

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import java.util.Calendar
import java.text.SimpleDateFormat

import com.fasterxml.jackson.databind.JsonNode
import io.gatling.core.check.MultipleFindCheckBuilder
import io.gatling.core.check.jsonpath.{JsonPathCheckType, JsonPathOfType}

import scala.concurrent.duration.DurationInt

class CsMobileV6 extends Simulation {

  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  object MyClass {
    def main(args: Array[String]) {
      val form = new SimpleDateFormat("yyyy / MM / dd");
      val c = Calendar.getInstance();

      println("Present Date : " + c.getTime());

      val formattedDate = form.format(c.getTime());
      println("Date formatted : "+formattedDate);
    }
  }

  /** Configure Protocol **/
  val httpConf = http
    .baseUrl("https://ssl.dstv.com/api")
    .header("Accept", "application/json")

  /** Runtime Parameters **/
  def userCount: Int = getProperty("USERS", "15").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt

  def testDuration: Int = getProperty("DURATION", "120").toInt

  /** Helper Methods **/
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  /** Test Data CSV **/
  val csvFeeder = csv("sessionId.csv").circular

  /** Scenarios **/

  def getCatalogue():ChainBuilder = {
    val path: String = "/cs-mobile/now-content/v6/catalogue;productId=c53b19ce-62c0-441e-ad29-ecba2dcdb199;platformId=0aed2408-a480-493a-9305-09480614b206;tags=Movies"

    feed(csvFeeder)
      .exec(http("GET cs-mobile/catalogue")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200))
      )}

  def getCataloguesTvShow(): ChainBuilder = {
    val path: String = "/cs-mobile/now-content/v6/catalogue;productId=c53b19ce-62c0-441e-ad29-ecba2dcdb199;platformId=f8113a08-286b-4250-b7c5-31fbfcaec8b0;tags=TV_Shows;sort=az"
    feed(csvFeeder)
      .exec(http("GET cs-mobile/catalogueTV_Shows")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200))

      )
  }

  def getCataloguesKids(): ChainBuilder = {
    val path: String = "/cs-mobile/now-content/v6/catalogue;productId=c53b19ce-62c0-441e-ad29-ecba2dcdb199;platformId=f8113a08-286b-4250-b7c5-31fbfcaec8b0;tags=Kids;sort=az"
    feed(csvFeeder)
      .exec(http("GET cs-mobile/catalogueKids")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200))

      )
  }

  def getEditorialsForHome(): ChainBuilder = {
    val path: String = "/cs-mobile/editorial/v6/getEditorialsForHome;productId=1b09957b-27aa-493b-a7c9-53b3cec92d63;platformId=0aed2408-a480-493a-9305-09480614b206;packageId=3e6e5480-8b8a-4fd5-9721-470c895f91e2;platformType=mobile"
    feed(csvFeeder)
      .exec(http("GET cs-mobile/Editorials For home")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200))

      )
  }

  def getCataloguesSport(): ChainBuilder = {
    val path: String = "/cs-mobile/now-content/v6/catalogue;productId=c53b19ce-62c0-441e-ad29-ecba2dcdb199;platformId=f8113a08-286b-4250-b7c5-31fbfcaec8b0;tags=Sport;sort=az"
    feed(csvFeeder)
      .exec(http("GET cs-mobile/catalogueSport")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200))

      )
  }

  def getRecentlyWatched(): ChainBuilder = {
    val path: String = "/cs-mobile/editorial/v6/editorialList/recently-watched-channels;productId=1b09957b-27aa-493b-a7c9-53b3cec92d63;packageId=3e6e5480-8b8a-4fd5-9721-470c895f91e2;platformId=51a0d73e-2304-4bd9-b9a5-9c7d99a301e9"
    feed(csvFeeder)
      .exec(http("GET cs-mobile/recently watched")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200))

      )
  }

  /** New EPG Service Tests */

  def getEpgEvents():ChainBuilder = {
    val path:  String = "/cs-mobile/epg/v6/events;channelTags=E10,HDT,VAM,M1D,MCH,MAP,SDN,MDH,S30,SUH,MFH,V30,UHD,TEL,B23,B26,DHD,C33,ITH,E26,FHD,FOC,BHD,M33,LTE,RTK,T32,F32,TND,E03,MZE,E05,EV1,KHD,K30,KNM,VHD,IA9,AMM,AFM,RTA,MOH,MZH,W30,MZB,M11,H26,19K,I26,I32,SHP,BBL,F26,HCH,FTV,TRV,WEA,NA8,NGW,ETH,H23,ITV,SPI,B1C,B2C,B3C,HDE,E01,SSZ,SSH,SH2,TS2,SH4,HD5,HD6,HD7,MSH,9HD,SSV,A11,12H,XHD,SPY,IGN,M30,STV,TVB,1KZ,TSH,CTV,GAU,LTV,CHD,33B,SO8,DXD,NIC,CBB,NJR,NTO,DPH,JJM,E06,PB7,E11,OHD,MZM,B33,VH8,T26,8GT,MM2,GOS,DUM,TBK,DW5,TB1,IST,EMM,BB8,CNN,SKY,HD2,SAN,NZD,ALJ,EVE,PAR,CC9,CN8,BLO,ABC,DEE,RAI,BVN,TVK,DW4,SHO,RTP,MN1"
    feed(csvFeeder)
      .exec(http("GET EPG Events ")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200))
      )
  }

  def getEpgEventByTag():ChainBuilder = {
    val path: String = "/cs-mobile/epg/v6/getCurrentEventByTag;startDate=2020-01-23T12:04:44+0200;endDate=2020-01-23T14:04:44+0200;channelTag=HDT"
    feed(csvFeeder)
      .exec(http("Get Epg EVents By Tag")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }

  def getChannels():ChainBuilder = {
    val path: String = "/cs-mobile/epg/v6/getEpgChannels"
    feed(csvFeeder)
      .exec(http("Get Epg Channels")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }

  def getChannelsEventsByCount():ChainBuilder = {
    val path: String = "/cs-mobile/epg/v6/channels;platformId=51a0d73e-2304-4bd9-b9a5-9c7d99a301e9;eventsCount=2"
    feed(csvFeeder)
      .exec(http("Get Channels Events By Count")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }

  def getSections():ChainBuilder = {
    val path: String = "/cs-mobile/epg/v6/sections;platformId=51a0d73e-2304-4bd9-b9a5-9c7d99a301e9"
    feed(csvFeeder)
      .exec(http("Get Epg Sections")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }

  def getStreamableChannels():ChainBuilder = {
    val path: String = "/cs-mobile/epg/v6/getStreamableChannels;platformId=51a0d73e-2304-4bd9-b9a5-9c7d99a301e9"
    feed(csvFeeder)
      .exec(http("Get Streamable Channels")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }

  def getStreamableChannelByGenre():ChainBuilder = {
    val path: String = "/cs-mobile/epg/v6/getStreamableChannels;platformId=51a0d73e-2304-4bd9-b9a5-9c7d99a301e9;subCategoryId=ba8140b9-e4d0-4122-802e-fd427b9310ad"
    feed(csvFeeder)
      .exec(http("Get Streamable Channels by Genre")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }

  def versionManagerCheck():ChainBuilder = {
    val path: String = "/cs-mobile/version-manager/v6/checkVersion;productId=1b09957b-27aa-493b-a7c9-53b3cec92d63;platformId=51a0d73e-2304-4bd9-b9a5-9c7d99a301e9;versionNumber=10.8.1"
    feed(csvFeeder)
      .exec(http("Version Manager Check")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }
  def navigationMenuWeb():ChainBuilder = {
    val path: String = "/cs-mobile/v6/application/navigationmenu;platformId=32faad53-5e7b-4cc0-9f33-000092e85950"
    feed(csvFeeder)
      .exec(http("Navigation Menu")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }
  def navigationMenuMobile():ChainBuilder = {
    val path: String = "/cs-mobile/v6/application/navigationmenu;platformId=b058d380-69ac-4275-abfb-a2da0edfee43"
    feed(csvFeeder)
      .exec(http("Navigation Menu")
        .get(path)
        .header("Authorization", "${sessionId}")
        .check(status.is(200)))
  }

  val scn = scenario("Check catalogue Movies ")

    .forever() {
      exec(getCatalogue())
        .pause(1)
        .exec(getCataloguesTvShow())
        .pause(1)
        .exec(getCataloguesKids())
        .pause(1)
        .exec(getCataloguesSport())
        .pause(1)
        .exec(getEpgEvents())
        .pause(1)
        .exec(getEpgEventByTag())
        .pause(1)
        .exec(getChannels())
        .pause(1)
        .exec(getSections())
        .pause(0)
        .exec(getStreamableChannels())
        .pause(1)
        .exec(getStreamableChannelByGenre())
        .pause(1)
        .exec(versionManagerCheck())
        .pause(1)
        .exec(navigationMenuWeb())
        .pause(1)
        .exec(navigationMenuMobile())
        .pause(1)
        .exec(getChannelsEventsByCount())
        .pause(1)
        .exec(getEditorialsForHome())
        .pause(1)
        .exec(getRecentlyWatched())
        .pause(1)
    }



  /** Load Test Simulation **/
  setUp(
    scn.inject(
      nothingFor(2 seconds),
      rampUsers(userCount) during (rampDuration seconds))
  )
    .protocols(httpConf)
    .maxDuration(testDuration seconds)

  after {
    println("Load Test completed")
  }



}
