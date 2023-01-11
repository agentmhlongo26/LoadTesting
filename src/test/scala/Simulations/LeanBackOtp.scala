package Simulations

import io.gatling.core.Predef.Simulation
import io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

import scala.concurrent.duration.DurationInt


class LeanBackOtp extends Simulation {


  before {
    println(s"Running test with ${startingUsers} users")
    println(s"Ramping users over ${levelDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }
  /** Configure Protocol * */
  val httpConf = http
    .baseUrl(" https://leanback.legacy.connectedvideo.tv")
    .header("Accept", "application/json")
  //.proxy(Proxy("localhost", 8888).httpsPort(8888))

  /** Runtime Parameters * */

  def testDuration: Int = getProperty("DURATION", "10").toInt

  /** Stair Case Runtime Parameters */
  def incrementUserCount: Int = getProperty("INCREMENT_USERS", "2").toInt

  def noOfSteps: Int = getProperty("NO_OF_STEPS", "5").toInt

  def startingUsers: Int = getProperty("STARTING_USERS", "2").toInt

  def levelDuration: Int = getProperty("LEVEL_DURATION", "5").toInt

  def stepRampDuration: Int = getProperty("STEP_RAMP_DURATION", "2").toInt


  /** Helper Methods * */
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  /** Scenarios * */

  def GenerateLeanBackOTP(): ChainBuilder = {
    val path: String = "/lean-back-otp/device/registration"
    exec(http("POST OTP registration")
      .post(path)
      .header("Content-Type", "application/json")
      .body(StringBody(
        """
                { "deviceId": "on-prem-load-test",
                  "userAgent": "Mozilla/5.0 (Linux; BRAVIA 4K 2015 Build/LMY48E.S265) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36 OPR/28.0.1754.0",
                  "appVersion": "v 1.0.1"
                }
               """
      )).check(status.is(201))
    )
  }


  val scn = scenario("LeanBack OTP")
    .forever() {
      exec(GenerateLeanBackOTP())
        .pause(1)
    }

  /** Load Test Simulation * */
  setUp(
    scn.inject(
      incrementConcurrentUsers(incrementUserCount)
        .times(noOfSteps)
        .eachLevelLasting(levelDuration seconds)
        .separatedByRampsLasting(stepRampDuration seconds)
        .startingFrom(startingUsers)
    )
  )
    .protocols(httpConf)
    .maxDuration(testDuration seconds)

  after {
    println("Load Test completed")
  }
}
