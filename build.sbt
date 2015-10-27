lazy val root = (project in file(".")).
  settings(
    name := "hello",
    version := "1.0",
    scalaVersion := "2.11.4",
    libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.3.0"
  )
