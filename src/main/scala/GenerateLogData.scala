/*
 *
 *  Copyright (c) 2021. Mark Grechanik and Lone Star Consulting, Inc. All rights reserved.
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under
 *   the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *   either express or implied.  See the License for the specific language governing permissions and limitations under the License.
 *
 */
import Generation.{LogMsgSimulator, RandomStringGenerator}
import HelperUtils.{CreateLogger, ObtainConfigReference, Parameters}

import collection.JavaConverters.*
import scala.concurrent.{Await, Future, duration}
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client

import java.nio.file.Paths
import java.io.File
import scala.collection.JavaConverters.*
import java.util.Base64
import com.redis._


object GenerateLogData:

  val logger = CreateLogger(classOf[GenerateLogData.type])

//this is the main starting point for the log generator
@main def runLogGenerator =

  import Generation.RSGStateMachine.*
  import Generation.*
  import HelperUtils.Parameters.*
  import GenerateLogData.*

  logger.info("Log data generator started...")
  val INITSTRING = "Starting the string generation"
  val init = unit(INITSTRING)

  val logFuture = Future {
    LogMsgSimulator(init(RandomStringGenerator((Parameters.minStringLength, Parameters.maxStringLength), Parameters.randomSeed)), Parameters.maxCount)
  }
  Try(Await.result(logFuture, Parameters.runDurationInMinutes)) match {
    case Success(value) => logger.info(s"Log data generation has completed after generating ${Parameters.maxCount} records.")
    case Failure(exception) => logger.info(s"Log data generation has completed within the allocated time, ${Parameters.runDurationInMinutes}")
  }

  // Getting encoded AWS credentials
  val AWS_ACCESS_KEY_ENCODED = config.getString("awsConfig.AWS_ACCESS_KEY_ENCODED")
  val AWS_SECRET_KEY_ENCODED = config.getString("awsConfig.AWS_SECRET_KEY_ENCODED")

  // Decoding AWS credentials and converting them to strings
  val AWS_ACCESS_KEY_DECODED = Base64.getDecoder().decode(AWS_ACCESS_KEY_ENCODED)
  val AWS_SECRET_KEY_DECODED = Base64.getDecoder().decode(AWS_SECRET_KEY_ENCODED)
  val AWS_ACCESS_KEY = new String(AWS_ACCESS_KEY_DECODED)
  val AWS_SECRET_KEY = new String(AWS_SECRET_KEY_DECODED)

  // Getting AWS bucket name
  val bucket = config.getString("awsS3updater.bucket")

  // Connecting ot AWS S3 using credentials
  val AWSCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)
  val amazonS3Client = new AmazonS3Client(AWSCredentials)

  // Getting Redis host and port
  val REDIS_HOST = config.getString("awsConfig.REDIS_HOST")
  val REDIS_PORT = config.getInt("awsConfig.REDIS_PORT")

  // Connecting to Redis
  val r = new RedisClient(REDIS_HOST, REDIS_PORT)

  // Getting all generated log files
  val logfiles = java.nio.file.Files.walk(Paths.get("log")).iterator().asScala.filter(file => file.toString.endsWith(".log")).toList

  // Reading log files and putting logs into Redis
  logfiles.foreach(
    logfile => {
      val logs = scala.io.Source.fromFile(logfile.normalize.toString).getLines.toList
      logs.foreach(
        log => {
          r.set("p-" + java.util.UUID.randomUUID.toString, log)
        }
      )

      // Optional push log file to S3 bucket
      // amazonS3Client.putObject(bucket, logfile.toString.substring(4), logs.mkString("\r\n"))
      new File(logfile.toString).delete()

    }
  )
