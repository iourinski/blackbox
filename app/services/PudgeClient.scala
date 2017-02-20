package services

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.TimeZone

import net.spy.memcached.{CachedData, MemcachedClient}
import net.spy.memcached.transcoders.Transcoder

import scala.util.hashing.MurmurHash3
import scala.concurrent.duration.Duration


/**
  * Базовые запросы к memcached (либо другой ДБ, реализующей протокол memcached)
  */
class PudgeClient {

  private val timeout = Duration(2, TimeUnit.MINUTES)
  val tz = TimeZone.getTimeZone("Europe/Moscow")
  val tsFormat = new SimpleDateFormat("yyMMddHHmmss")
  tsFormat.setTimeZone(tz)
  //import scala.concurrent.ExecutionContext.Implicits.global // TODO: получать exec context извне
  val memcachedClient: MemcachedClient =  new MemcachedClient(new InetSocketAddress("127.0.0.1", 11213))
  try {
    memcachedClient.getConnection
  } catch {
    case e: Throwable => println("Can't connect")
  }


  private def writeKeyToPudge(key: String, value: String): Boolean = {
    memcachedClient.set(key, 0, value.getBytes).get(10, TimeUnit.SECONDS)
  }

  def readFromPudge(key: String): String = {
    try {
      memcachedClient.get(key).toString
    } catch {
      case e: Throwable =>
        println(e.getMessage)
        return ""
    }
  }

  def recordEvent(
                   domainId: String,
                   event: String,
                   ip: String,
                   host: String,
                   url: String,
                   screen: String
                 ): String = {
    val (key, value) = formKeyValue(event, url, screen, host, ip, domainId)
    try {
      writeKeyToPudge(key, value)
    } catch {
      case e: Throwable => println(e.getMessage)
    }
    key
  }

  def formKeyValue(
                    event: String,
                    url: String,
                    screen: String,
                    host: String,
                    ip: String,
                    domainId: String
                  ): (String, String) = {

    val ts: String  = tsFormat.format(System.currentTimeMillis())

    val uid = scala.util.hashing.MurmurHash3.stringHash(ip + screen)
    return (domainId + ":" + ts + ":"  + uid, event + ":" + url)
  }
}

class ByteArrayTranscoder extends Transcoder[Array[Byte]] {
  override def encode(o: Array[Byte]): CachedData = new CachedData(0, o, o.size)

  override def asyncDecode(d: CachedData): Boolean = false

  override def getMaxSize: Int = Int.MaxValue

  override def decode(d: CachedData): Array[Byte] = d.getData
}