ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "TopFive3Grams",
        libraryDependencies += "org.scala-lang" %% "toolkit" % "0.2.0",
        libraryDependencies += "com.lihaoyi" %% "upickle" % "3.1.0",
//    libraryDependencies += "com.github.losizm" %% "little-json" % "9.0.0",
  )
//val json4sNative = "org.json4s" %% "json4s-native" % "{latestVersion}"