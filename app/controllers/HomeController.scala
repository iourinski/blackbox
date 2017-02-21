package controllers

import javax.inject._

import play.api._
import play.api.mvc._
import services.PudgeClient
import play.api.Logger


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (pc: PudgeClient) extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  val logger: Logger = Logger(getClass)

  def index = Action {
    Ok("Your new application is ready.")
  }

  def hit(domainId: String, url: String, screen: String, referrer: String) = Action { request =>
    val ip = request.remoteAddress
    val host = request.host
    val domain = request.domain
    logger.info("request from " + ip + " for " + url)
    val res =   pc.recordEvent(domainId, "HIT", ip, host, url, screen: String)
    Ok(res)
  }
  def demHit(domainId: String, url: String, screen: String, referrer: String) = Action { request =>
    val ip = request.remoteAddress
    val host = request.host
    val domain = request.domain

    val key =   pc.recordEvent(domainId, "HIT", ip, host, url, screen: String)
    val res = pc.readFromPudge(key)
    Ok("wrote key " + key + " with value " + res)
  }

}
