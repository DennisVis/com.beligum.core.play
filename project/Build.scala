import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "com_beligum_core"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "commons-io" % "commons-io" % "1.3.2",
    "be.objectify" %% "deadbolt-java" % "2.1-RC2"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here    
    resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
  	resolvers += Resolver.url("Objectify Play Snapshot Repository", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns),   

    publishArtifact in(Compile, packageDoc) := false,
    ebeanEnabled := true  
  )

}
