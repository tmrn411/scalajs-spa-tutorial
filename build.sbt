import sbt.Keys._
import sbt.Project.projectToRef

// Useful resources for debugging build configuration
// https://github.com/vmunier/play-scalajs.g8/tree/master/src/main/g8
// https://github.com/vmunier/akka-http-scalajs.g8/tree/master/src/main/g8

EclipseKeys.skipParents in ThisBuild := false

// a special crossProject for configuring a JS/JVM/shared structure
lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(
    scalaVersion := Settings.versions.scala,
    libraryDependencies ++= Settings.sharedDependencies.value
  )
  // set up settings specific to the JS project
  .jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJVM = shared.jvm.settings(name := "sharedJVM")

lazy val sharedJS = shared.js.settings(name := "sharedJS")

// use eliding to drop some debug code in the production build
lazy val elideOptions = settingKey[Seq[String]]("Set limit for elidable functions")

// instantiate the JS project for SBT with some additional settings
lazy val client: Project = (project in file("client"))
  .settings(
    name := "client",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Settings.scalajsDependencies.value,
    // by default we do development build, no eliding
    elideOptions := Seq(),
    scalacOptions ++= elideOptions.value,
    npmDependencies in Compile ++= Settings.npmDependencies,
	// config webpack to create global vars for javascript modules
	// example project with build settings
	// https://github.com/scalacenter/scalajs-bundler/tree/master/sbt-scalajs-bundler/src/sbt-test/sbt-scalajs-bundler/global-namespace-with-jsdom-unit-testing_sjs-0.6
	npmDevDependencies in Compile ++= Seq(
	  "webpack-merge" -> "4.1.2",
	  "imports-loader" -> "0.8.0",
	  "expose-loader" -> "0.7.5"
	),
	// for now, use same webpack config for all phases
	webpackConfigFile := Some(baseDirectory.value / "dev.webpack.config.js"),
	//webpackConfigFile in fastOptJS := Some(baseDirectory.value / "dev.webpack.config.js"),
	//webpackConfigFile in Test := Some(baseDirectory.value / "test.webpack.config.js"),
	
    // RuntimeDOM is needed for tests
    //jsDependencies += RuntimeDOM % "test",
    // yes, we want to package JS dependencies
    skip in packageJSDependencies := false,
    // use Scala.js provided launcher code to start the client app
    scalaJSUseMainModuleInitializer := true,
    scalaJSUseMainModuleInitializer in Test := false,
    // use uTest framework for tests
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb, ScalaJSBundlerPlugin)
  .dependsOn(sharedJS)

// Client projects (just one in this case)
lazy val clients = Seq(client)

// SbtWeb and SbtLess should be enabled by Play plugin
// instantiate the JVM project for SBT with some additional settings
lazy val server = (project in file("server"))
  .settings(
    name := "server",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Settings.jvmDependencies.value,
    libraryDependencies += ws,
    libraryDependencies += guice,
    commands += ReleaseCmd,
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    // connect to the client project
    scalaJSProjects := clients,
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // compress CSS
    LessKeys.compress in Assets := true
  )
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .disablePlugins(PlayLayoutPlugin) // use the standard directory layout instead of Play's custom
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(sharedJVM)

// Command for building a release
lazy val ReleaseCmd = Command.command("release") {
  state => "set elideOptions in client := Seq(\"-Xelide-below\", \"WARNING\")" ::
    "client/clean" ::
    "client/test" ::
    "server/clean" ::
    "server/test" ::
    "server/dist" ::
    "set elideOptions in client := Seq()" ::
    state
}

// lazy val root = (project in file(".")).aggregate(client, server)

// loads the Play server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value
