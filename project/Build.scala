import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "com_beligum_core"
  val appVersion      = "0.1.0"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "commons-io" % "commons-io" % "1.3.2",
    "be.objectify" %% "deadbolt-java" % "2.1-RC2"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
  	organization := "com.beligum",
  
    // Add your own project settings here    
    resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("Objectify Play Snapshot Repository", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns),   

    publishArtifact in(Compile, packageDoc) := false,
    sources in doc in Compile := List(),
    
    com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys.skipParents in ThisBuild := false,
    com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys.withSource := true,
    
    //hmm, doesn't work, see https://groups.google.com/forum/#!topic/play-framework/k5MT68xFzDA
    //routesImport ++= Seq("_root_.com.beligum._"),
	templatesImport ++= Seq("com.beligum._")
  )

}
