import org.scalatest.flatspec.AnyFlatSpec
import com.typesafe.config.ConfigFactory

class LogFileGeneratorDataTest extends AnyFlatSpec {

  // Checking for encoded aws access key in config
  "config" should "encoded aws access key" in {

    // Getting key
    val key = ConfigFactory.load().getString("awsConfig.AWS_ACCESS_KEY_ENCODED")

    assert(key == "QUtJQTM3WU5EREw2R0YzTENPRlg=")

  }

  // Checking for encoded aws secret key in config
  "config" should "encoded aws secret key" in {

    // Getting key
    val key = ConfigFactory.load().getString("awsConfig.AWS_SECRET_KEY_ENCODED")

    assert(key == "aXEyYzBBY0pqSW9EamdHZmFreWh5dnA2T2NRS2h4Y3pzUmQydWl1dg==")

  }

  // Checking for redis host in config
  "config" should "contain redis host" in {

    // Getting host
    val host = ConfigFactory.load().getString("awsConfig.REDIS_HOST")

    assert(host == "log-file-generator-data.bagybp.ng.0001.use2.cache.amazonaws.com")

  }

  // Checking for redis port in config
  "config" should "contain redis port" in {

    // Getting port
    val port = ConfigFactory.load().getInt("awsConfig.REDIS_PORT")

    assert(port == 6379)

  }


  // Checking for s3 bucket name in config
  "config" should "contain s3 bucket name" in {

    // Getting name
    val name = ConfigFactory.load().getString("awsS3updater.bucket")

    assert(name == "log-file-generator-data")

  }

}
