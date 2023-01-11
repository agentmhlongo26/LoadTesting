package Simulations

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import scala.concurrent.duration._

class DsPayVodPosts extends Simulation {

  before {
    println(s"Running test with ${userCount} users")
    println(s"Running test with ${maxUserCount} max users")
    println(s"Ramping to max users over ${maxUserRampDuration} seconds")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  /** Configure Protocol * */
  val httpConf = http

    /** Prod configuration * */
    .baseUrl("http://ds-pay-vod-dev.dstvo.internal:8080")
    //.baseUrl("https://public-ingress-prd-230570154.eu-west-1.elb.amazonaws.com")
    .proxy(Proxy("localhost", 8888).httpsPort(8888))
    .header("Accept", "application/json")

  /** Runtime Parameters * */
  def userCount: Int = getProperty("USERS", "5").toInt

  def maxUserCount: Int = getProperty("MAX_USERS", "15").toInt

  def maxUserRampDuration: Int = getProperty("MAX_USER_RAMP_DURATION", "2").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt

  def testDuration: Int = getProperty("DURATION", "300").toInt

  /** Helper Methods * */
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  /** Test Data CSV * */
  //val paramsFeeder = csv("").random
  var userIdFeeder = (1 to 999999).toStream.map(i => Map("slugId" -> i)).toIterator

  /** Object definition * */
  def createProgram(): ChainBuilder = {
    exec(http("POST |  ProgramCreate")
      .post("/ds-pay-vod/program")
      .header("Content-Type", "application/json")
      .body(StringBody(
        """
          |{
          |    "name": "Manhattan",
          |    "groupName": "Manhattan Catch Up",
          |    "mmsId": 0,
          |    "synopsis": "The writer of ‘The West Wing’ penned this historical drama based on the nuclear project of the same name. In the season finale, Charlie finds himself in the US Army's hot seat.",
          |    "shortSynopsis": "The writer of ‘The West Wing’ penned this historical drama based on the nuclear project of the same name. In the season finale, Charlie finds himself in the US Army's hot seat.",
          |    "seasons": [
          |        {
          |            "seasonNumber": 1,
          |            "synopsis": "The writer of ‘The West Wing’ penned this historical drama based on the nuclear project of the same name. In the season finale, Charlie finds himself in the US Army's hot seat.",
          |            "totalNumberOfEpisodes": 13,
          |            "seasonId": ""
          |        }
          |    ],
          |    "channels": [
          |        {
          |            "id": "ce8fb703-6ba0-4143-872b-d3366dd7569e",
          |            "href": "http://ds-epg.dstvo.internal/ds-epg/channel/ce8fb703-6ba0-4143-872b-d3366dd7569e/"
          |        }
          |    ],
          |    "imageInstancesMap": {
          |        "poster": {
          |            "tags": [],
          |            "products": [],
          |            "people": [],
          |            "organisations": [],
          |            "originalImage": {
          |                "path": "/2017/02/16/IS20_1025379_PP.jpg",
          |                "width": 440,
          |                "height": 610,
          |                "fileSize": 94763
          |            },
          |            "optimisedOriginalImage": {
          |                "path": "/2017/02/16/IS20_1025379_PP_pre.jpg",
          |                "width": 440,
          |                "height": 610,
          |                "fileSize": 87497
          |            },
          |            "images": {
          |                "MEDIUM": {
          |                    "path": "/2017/02/16/IS20_1025379_PP_med.jpg",
          |                    "width": 220,
          |                    "height": 305,
          |                    "fileSize": 24426
          |                },
          |                "LARGE": {
          |                    "path": "/2017/02/16/IS20_1025379_PP_lrg.jpg",
          |                    "width": 440,
          |                    "height": 610,
          |                    "fileSize": 88294
          |                }
          |            },
          |            "imagePreviews": {
          |                "SMALL": {
          |                    "path": "/2017/02/16/IS20_1025379_PP_pre_sml.jpg",
          |                    "width": 433,
          |                    "height": 600,
          |                    "fileSize": 87190
          |                },
          |                "THUMB": {
          |                    "path": "/2017/02/16/IS20_1025379_PP_pre_thumb.jpg",
          |                    "width": 202,
          |                    "height": 280,
          |                    "fileSize": 20645
          |                }
          |            },
          |            "entityImageAsset": {
          |                "id": "",
          |                "href": ""
          |            }
          |        }
          |    },
          |    "pageViews": 0,
          |    "categorisations": [
          |        {
          |            "product": {
          |                "id": "1b09957b-27aa-493b-a7c9-53b3cec92d63",
          |                "href": "http://ds-universal.dstvo.internal:8080/ds-universal/product/1b09957b-27aa-493b-a7c9-53b3cec92d63/"
          |            },
          |            "category": {
          |                "id": "18d672aa-64ee-428d-b616-b14ab1df7f9c",
          |                "href": "http://ds-universal.dstvo.internal:8080/ds-universal/category/18d672aa-64ee-428d-b616-b14ab1df7f9c/"
          |            },
          |            "subCategory": {
          |                "id": "a926d393-cf14-4fab-8e6c-567df588c51b",
          |                "href": "http://ds-universal.dstvo.internal:8080/ds-universal/category/a926d393-cf14-4fab-8e6c-567df588c51b/"
          |            }
          |        }
          |    ],
          |    "seriesId": "",
          |    "createdDate": "2016-12-05T10:13:31.730",
          |    "lastModifiedDate": "2022-05-23T11:56:45.944",
          |    "self": {
          |        "id": "0005682a-07a1-4998-b582-02ab8daed168",
          |        "href": "http://ds-pay-vod.dstvo.internal/ds-pay-vod/program/0005682a-07a1-4998-b582-02ab8daed168/"
          |    }
          |}
          |""".stripMargin))
      .check(status.is(201)))
  }

  def createVideoMeta(): ChainBuilder = {
    feed(userIdFeeder)
    .exec(http("POST | VideoMetaCreate")
      .post("/ds-pay-vod/videoMeta")
      .header("Content-Type", "application/json")
      .body(StringBody("""
          {
          |    "title": "Republiek van Zoid Afrika: 11 Oktober 2016",
          |    "slug": "slugID${slugId}",
          |    "mmsId": 1134214,
          |    "ratingAdvisory": "S,",
          |    "shortSynopsis": "President Zoid het gesels met Armand Aucamp, Christo Davids en Ard Matthews oor hul emosies en wense vir die toekoms.",
          |    "synopsis": "President Zoid het gesels met Armand Aucamp, Christo Davids en Ard Matthews oor hul emosies en wense vir die toekoms.",
          |    "yearOfRelease": 0,
          |    "seasonNumber": 4,
          |    "episode": 1,
          |    "durationInSeconds": 267,
          |    "tags": [
          |        "kyknet",
          |        " zoid afrika",
          |        " seisoen 4",
          |        " Karen Zoid",
          |        " musiek",
          |        " kunstenaars",
          |        " gesels",
          |        " emosies",
          |        " familie"
          |    ],
          |    "airDate": "2016-10-12T00:00:00.000",
          |    "seoKeywords": [
          |        "kyknet",
          |        " zoid afrika",
          |        " seisoen 4",
          |        " Karen Zoid",
          |        " musiek"
          |    ],
          |    "categorisations": [
          |        {
          |            "product": {
          |                "id": "024d4e34-742a-4068-a2ef-5a76368337e7",
          |                "href": "http://ds-universal.dstvo.internal:8080/ds-universal/product/024d4e34-742a-4068-a2ef-5a76368337e7/"
          |            },
          |            "category": {
          |                "id": "66c62e5a-3c3d-4b51-a1cd-52ac7d16941c",
          |                "href": "http://ds-universal.dstvo.internal:8080/ds-universal/category/66c62e5a-3c3d-4b51-a1cd-52ac7d16941c/"
          |            },
          |            "subCategory": {
          |                "id": "db63a095-438a-4abc-8539-7a23d2e3a4fe",
          |                "href": "http://ds-universal.dstvo.internal:8080/ds-universal/category/db63a095-438a-4abc-8539-7a23d2e3a4fe/"
          |            }
          |        }
          |    ],
          |    "ageRestriction": {
          |        "id": "fcc124ec-d849-4646-9aa0-45343c44f90d",
          |        "href": "http://ds-universal.dstvo.internal:8080/ds-universal/restrictionRating/fcc124ec-d849-4646-9aa0-45343c44f90d/"
          |    },
          |    "imageInstancesMap": {
          |        "play-image": {
          |            "tags": [],
          |            "products": [],
          |            "people": [],
          |            "organisations": [],
          |            "originalImage": {
          |                "path": "/2017/03/23/kn_rza_s4_hoogtepunt_ep1.jpg",
          |                "width": 640,
          |                "height": 360,
          |                "fileSize": 130508
          |            },
          |            "optimisedOriginalImage": {
          |                "path": "/2017/03/23/kn_rza_s4_hoogtepunt_ep1_pre.jpg",
          |                "width": 640,
          |                "height": 360,
          |                "fileSize": 42676
          |            },
          |            "images": {
          |                "MEDIUM": {
          |                    "path": "/2017/03/23/kn_rza_s4_hoogtepunt_ep1_med.jpg",
          |                    "width": 600,
          |                    "height": 338,
          |                    "fileSize": 39633
          |                },
          |                "SMALL": {
          |                    "path": "/2017/03/23/kn_rza_s4_hoogtepunt_ep1_sml.jpg",
          |                    "width": 300,
          |                    "height": 169,
          |                    "fileSize": 15453
          |                }
          |            },
          |            "imagePreviews": {
          |                "SMALL": {
          |                    "path": "/2017/03/23/kn_rza_s4_hoogtepunt_ep1_pre_sml.jpg",
          |                    "width": 600,
          |                    "height": 338,
          |                    "fileSize": 39633
          |                },
          |                "THUMB": {
          |                    "path": "/2017/03/23/kn_rza_s4_hoogtepunt_ep1_pre_thumb.jpg",
          |                    "width": 280,
          |                    "height": 158,
          |                    "fileSize": 14009
          |                }
          |            },
          |            "entityImageAsset": {
          |                "id": "",
          |                "href": ""
          |            }
          |        }
          |    },
          |    "ProgramCreate": [
          |        {
          |            "id": "2540e1b1-a9fe-429c-93e3-acc679e9a182",
          |            "href": "http://ds-pay-vod.dstvo.internal/ds-pay-vod/videoAsset/2540e1b1-a9fe-429c-93e3-acc679e9a182/"
          |        }
          |    ],
          |    "program": {
          |        "id": "945f7ffe-b7f0-464d-a2da-febdafd4dc6b",
          |        "href": "http://ds-pay-vod.dstvo.internal/ds-pay-vod/program/945f7ffe-b7f0-464d-a2da-febdafd4dc6b/"
          |    },
          |    "channels": [
          |        {
          |            "id": "abe5994d-189a-40be-b51d-b15369e4bde7",
          |            "href": "http://ds-epg.dstvo.internal/ds-epg/channel/abe5994d-189a-40be-b51d-b15369e4bde7/"
          |        }
          |    ],
          |    "pageViews": 0,
          |    "yearOfProduction": 0,
          |    "createdDate": "2017-03-23T16:22:25.797",
          |    "lastModifiedDate": "2019-08-22T05:25:07.045",
          |    "self": {
          |        "id": "00001110-40a0-4c74-874f-653e2dbbc3e4",
          |        "href": "http://ds-pay-vod.dstvo.internal/ds-pay-vod/videoMeta/00001110-40a0-4c74-874f-653e2dbbc3e4/"
          |    }
          |}
          |""".stripMargin))
      .check(status.is(201)))
  }

  def getVideoPackage(): ChainBuilder = {
    exec(http("GET | All videoPackages")
      .get("/ds-pay-vod/videoPackage")
      .check(status.is(200)))
  }

  def getEditorialList(): ChainBuilder = {
    exec(http("GET | All editorialLists")
      .get("/ds-pay-vod/editorialList")
      .check(status.is(200)))
  }

  def getPrograms(): ChainBuilder = {
    exec(http("GET | All programs")
      .get("/ds-pay-vod/program")
      .check(status.is(200)))
  }

  def getSchedules(): ChainBuilder = {
    exec(http("GET | All schedules")
      .get("/ds-pay-vod/schedule")
      .check(status.is(200)))
  }

  def getPlaylist(): ChainBuilder = {
    exec(http("GET | All playlists")
      .get("/ds-pay-vod/playlist")
      .check(status.is(200)))
  }

  /** Scenario definition * */
  val ProgramCreate = scenario("Post ProgramCreate").forever() {
    exec(createProgram()).pause(1000 milliseconds)
  }
  val VideoMetaCreate = scenario("Post videoMeta").forever() {
    exec(createVideoMeta()).pause(1000 milliseconds)
  }
  /**val videoPackages = scenario("GET videoPackage").forever() {
    exec(getVideoPackage()).pause(1000 milliseconds)
  }
  val editorialLists = scenario("GET editorialList").forever() {
    exec(getEditorialList()).pause(1000 milliseconds)
  }
  val programs = scenario("GET program").forever() {
    exec(getPrograms()).pause(1000 milliseconds)
  }
  val schedules = scenario("GET schedule").forever() {
    exec(getSchedules()).pause(1000 milliseconds)
  }
  val playlists = scenario("GET playList").forever() {
    exec(getPlaylist()).pause(1000 milliseconds)
  }* */

  /** Load Test Simulation * */
  setUp(
    ProgramCreate.inject(rampUsers(maxUserCount) during (maxUserRampDuration seconds)),
    VideoMetaCreate.inject(rampUsers(maxUserCount) during (maxUserRampDuration seconds))
/**videoPackages.inject(rampUsers(maxUserCount) during (maxUserRampDuration seconds)),
    editorialLists.inject(rampUsers(maxUserCount) during (maxUserRampDuration)),
    programs.inject(rampUsers(maxUserCount) during (maxUserRampDuration)),
    schedules.inject(rampUsers(maxUserCount) during (maxUserRampDuration)),
    playlists.inject(rampUsers(maxUserCount) during (maxUserRampDuration))* */
  )
    .protocols(httpConf)
    .maxDuration(testDuration seconds)

  after {
    println("Load Test completed")
  }
}
