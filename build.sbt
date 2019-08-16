name := "zio-playground"

version := "1.0"

scalaVersion := "2.13.0"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

libraryDependencies ++=
  "dev.zio" %% "zio-streams" % "1.0.0-RC11-1" ::
  Nil
