package services

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import net.spy.memcached.{CachedData, MemcachedClient}
import net.spy.memcached.transcoders.Transcoder
import scala.util.hashing.MurmurHash3
//import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
//import scala.collection.convert.WrapAsJava._
//import scala.collection.convert.WrapAsScala._

/**
  * Базовые запросы к memcached (либо другой ДБ, реализующей протокол memcached)
  */
class PudgeClient {

  private val timeout = Duration(2, TimeUnit.MINUTES)

  //import scala.concurrent.ExecutionContext.Implicits.global // TODO: получать exec context извне
  val memcachedClient: MemcachedClient =  new MemcachedClient(new InetSocketAddress("127.0.0.1", 11213))
  memcachedClient.getConnection
  private def writeKeyToPudge(key: String, value: String): Boolean = {
    memcachedClient.set(key, 0, value.getBytes).get(10, TimeUnit.SECONDS)
  }

  def recordEvent(event: String, ip: String, host: String, domain: String, url: String,screen: String): String = {
    val key = formKey(event, url, screen, host, ip)
    try {
      writeKeyToPudge(key, "OK")
    } catch {
      case e: Throwable => println(e.getMessage)
    }

    key
  }

  def formKey(event: String, url: String,screen: String, host: String, ip: String): String = {
    val age: Long  = (2524608000L - System.currentTimeMillis()/1000)
    val uid = scala.util.hashing.MurmurHash3.stringHash(ip + screen)
    return event + ":" + host + ":" +  age + ":" + uid + ":" + url
  }
}

class ByteArrayTranscoder extends Transcoder[Array[Byte]] {
  override def encode(o: Array[Byte]): CachedData = new CachedData(0, o, o.size)

  override def asyncDecode(d: CachedData): Boolean = false

  override def getMaxSize: Int = Int.MaxValue

  override def decode(d: CachedData): Array[Byte] = d.getData
}