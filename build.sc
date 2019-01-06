// build.sc
import coursier.maven.MavenRepository
import mill._
import scalalib._

object deps {
  val awsVersion = "1.11.192"
  val scalatest = ivy"org.scalatest::scalatest:3.0.5"
  val dynamodb = ivy"com.amazonaws:aws-java-sdk-dynamodb:${awsVersion}"
  val dynamodbLocal = ivy"com.amazonaws:DynamoDBLocal:1.11.119".excludeName("com.almworks.sqlite4java")
}

import deps._

object `dynamodb-local` extends JavaModule {

  override def repositories = super.repositories ++ Seq(
    MavenRepository("https://s3-us-west-2.amazonaws.com/dynamodb-local/release")
  )

  override def ivyDeps = Agg(dynamodbLocal)

  def unmanagedClasspath = T {
    if (!ammonite.ops.exists(millSourcePath / "lib")) Agg()
    else Agg.from(ammonite.ops.ls(millSourcePath / "lib").map(PathRef(_)))
  }

}

object app extends SbtModule {
  def scalaVersion = "2.11.12"

  object test extends Tests {

    override def ivyDeps = Agg(dynamodb, scalatest)

    override def repositories = super.repositories ++ `dynamodb-local`.repositories

    def unmanagedClasspath = T {
      `dynamodb-local`.unmanagedClasspath
    }

    override def moduleDeps: Seq[JavaModule] = Seq(`dynamodb-local`)

    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }

}
