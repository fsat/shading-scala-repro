import scala.collection.{ Seq => DefaultSeq }
import scala.collection.immutable.Seq
import scala.xml.{ Node => XmlNode, NodeSeq => XmlNodeSeq, _ }
import scala.xml.transform.{ RewriteRule, RuleTransformer }
import sbtassembly.MergeStrategy

lazy val Versions = new {
  val akka                      = "2.4.12" // First version cross-compiled to 2.12
  val akkaDns                   = "2.4.2"
  val scala211                  = "2.11.11"
  val scala212                  = "2.12.3"  
}

lazy val scalaVersionMajor = SettingKey[String]("scala-version-major")

scalaVersionMajor in ThisBuild := (scalaVersion.value).split('.').dropRight(1).mkString(".")


lazy val root = project
  .in(file("."))
  .aggregate(
    hello
  )

lazy val shadedAkkaDns = Project(id = "shaded-akka-dns", base = file("shaded-akka-dns"))
  .settings(
    organization := "com.lightbend.rp",
    organizationName := "Lightbend, Inc.",
    scalaVersion := Versions.scala211,
    crossScalaVersions := Vector(Versions.scala211, Versions.scala212),
    test in assembly := {},
    assemblyOption in assembly ~= {
      _.copy(includeScala = false)
    },
    assemblyJarName in assembly := {
      s"${name.value}-${scalaVersionMajor.value}-${version.value}.jar"
    },
    target in assembly := {
      baseDirectory.value / "target" / scalaVersionMajor.value
    },
    packageBin in Compile := (assembly in Compile).value,
    addArtifact(Artifact("shaded-akka-dns", "assembly"), sbtassembly.AssemblyKeys.assembly),
    assemblyShadeRules in assembly := Seq(
      ShadeRule.rename("akka.io.AsyncDnsResolver**" -> "com.lightbend.rp.internal.@0").inAll,
      ShadeRule.rename("ru.smslv**" -> "com.lightbend.rp.internal.@0").inAll
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.akka"        %% "akka-actor"          % Versions.akka              % "provided",
      "ru.smslv.akka"            %% "akka-dns"            % Versions.akkaDns
    ),
    assemblyExcludedJars in assembly := {
      (fullClasspath in assembly).value.filterNot(_.data.getName.startsWith("akka-dns"))
    },
    assemblyMergeStrategy in assembly := {
      case v @ "reference.conf" =>
        // Apply custom merge strategy to `reference.conf` within Akka DNS jar to rename configured classes from
        // `ru.smslv` package to `com.lightbend.rp.internal.ru.smslv`.
        new MergeStrategy {
          override val name: String = "Akka DNS reference.conf merge"
          override def apply(tempDir: File, path: String, files: scala.Seq[File]): Either[String, Seq[(File, String)]] = {
            val (source, _, _, _) = sbtassembly.AssemblyUtils.sourceOfFileForMerge(tempDir, files.head)

            // Only apply this strategy if the reference.conf has indeed come from the Akka DNS jar, else fallback to
            // existing strategy
            if (source.getName.startsWith("akka-dns") && source.getName.endsWith(".jar")) {
              import scala.collection.JavaConverters._
              val file = MergeStrategy.createMergeTarget(tempDir, path)
              val lines = java.nio.file.Files.readAllLines(files.head.toPath).asScala
              val linesShaded = lines.map(_.replace("ru.smslv", "com.lightbend.rp.internal.ru.smslv"))
              linesShaded.foreach { v =>
                IO.append(file, v)
                IO.append(file, IO.Newline.getBytes(IO.defaultCharset))
              }
              Right(Seq(file -> path))
            } else {
              val existingStrategy = (assemblyMergeStrategy in assembly).value
              existingStrategy(v).apply(tempDir, path, files).map(_.toList)
            }
          }
        }

      case v =>
        val existingStrategy = (assemblyMergeStrategy in assembly).value
        existingStrategy(v)
    },
    pomPostProcess := { (node: XmlNode) =>
      new RuleTransformer(
        new RewriteRule {
          override def transform(node: XmlNode): XmlNodeSeq = node match {
            case e: Elem if e.label == "dependency" =>
              val organization = get(e, "groupId")
              val artifact = get(e, "artifactId")
              val version = e.child.filter(_.label == "version").flatMap(_.text).mkString

              if (organization == "ru.smslv.akka" && artifact.startsWith("akka-dns"))
                Comment(s"provided dependency $organization#$artifact;$version has been omitted")
              else
                node
            case _ => node
          }

          private def get(current: Elem, childElementName: String): String =
            current.child.filter(_.label == childElementName).flatMap(_.text).mkString
        })
        .transform(node)
        .head
    }
  )

lazy val hello = project
  .in(file("hello"))
  .settings(
    unmanagedJars in Compile ++= Seq(
      (assembly in Compile in shadedAkkaDns).value
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.akka"        %% "akka-actor"          % Versions.akka    % "provided",
      "com.typesafe.akka"        %% "akka-testkit"        % Versions.akka    % "test",
    ),
    crossScalaVersions := Vector(Versions.scala211, Versions.scala212)
  )
