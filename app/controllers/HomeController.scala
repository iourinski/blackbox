package controllers

import javax.inject._

import play.api._
import play.api.mvc._
import services.PudgeClient

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
  def index = Action {
    Ok("<html><body><h1>Your new application is ready.</h1></body></html>")
  }
  def hit(url: String, screen: String, referrer: String) = Action { request =>
    val ip = request.remoteAddress
    val host = request.host
    val domain = request.domain

    val res =   pc.recordEvent("HIT",ip,host,domain,url,screen: String)

    Ok(res)
  }

}
