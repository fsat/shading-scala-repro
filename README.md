# Reproducer for SBT Assembly Shading for Scala Libraries

This project is created to show the problem of shading Scala library, particularly [Akka DNS](https://github.com/ilya-epifanov/akka-dns).

Run the following to reproduce the problem.

```
sbt hello/compile
```

## The setup

The project `hello` is a simple hello-world like project, which depends on the shaded Akka DNS.

The shaded Akka DNS is built using `shaded-akka-dns` project which will product a `jar` file containing _only_ shaded classes from the Akka DNS library (i.e. transitive dependencies are not included). [SBT Assembly](https://github.com/sbt/sbt-assembly) is used to package and shade the Akka DNS library. SBT Assembly also provides the means to post-process the `reference.conf` contained within Akka DNS jar to ensure the shaded classes is used in the config file.

## The problem

When compiling against shaded Akka DNS jar, we've found the following compile failure.

```
$ sbt hello/compile
[info] Loading settings from plugins.sbt ...
[info] Loading global plugins from /Users/felixsatyaputra/.sbt/1.0/plugins
[info] Loading settings from plugins.sbt ...
[info] Loading project definition from /Users/felixsatyaputra/workspace/typesafe-fsat/shading-scala-repro/project
[info] Loading settings from build.sbt ...
[info] Set current project to root (in build file:/Users/felixsatyaputra/workspace/typesafe-fsat/shading-scala-repro/)
[info] Including from cache: akka-dns_2.11-2.4.2.jar
[info] Checking every *.class/*.jar file's SHA-1.
[info] Merging files...
[info] Packaging /Users/felixsatyaputra/workspace/typesafe-fsat/shading-scala-repro/shaded-akka-dns/target/2.12/shaded-akka-dns-2.12-0.1-SNAPSHOT.jar ...
[info] Done packaging.
[info] Compiling 1 Scala source to /Users/felixsatyaputra/workspace/typesafe-fsat/shading-scala-repro/hello/target/scala-2.12/classes ...
[error] /Users/felixsatyaputra/workspace/typesafe-fsat/shading-scala-repro/hello/src/main/scala/Hello.scala:1:42: object AsyncDnsResolver is not a member of package com.lightbend.rp.internal.akka.io
[error] Note: class AsyncDnsResolver exists, but it has no companion object.
[error] import com.lightbend.rp.internal.akka.io.AsyncDnsResolver.SrvResolved
[error]                                          ^
[error] /Users/felixsatyaputra/workspace/typesafe-fsat/shading-scala-repro/hello/src/main/scala/Hello.scala:2:8: Symbol 'term <none>.dns.raw' is missing from the classpath.
[error] This symbol is required by ' <none>'.
[error] Make sure that term raw is in your classpath and check for conflicting dependencies with `-Ylog-classpath`.
[error] A full rebuild may help if 'package.class' was compiled against an incompatible version of <none>.dns.
[error] import com.lightbend.rp.internal.ru.smslv.akka.dns.raw.SRVRecord
[error]        ^
[error] two errors found
[error] (hello/compile:compileIncremental) Compilation failed
[error] Total time: 1 s, completed 18/12/2017 1:53:13 PM
```

The generated `shaded-akka-dns-2.12-0.1-SNAPSHOT.jar` does contain all classes from the Akka DNS library.

```
$ jar tvf /Users/felixsatyaputra/workspace/typesafe-fsat/shading-scala-repro/shaded-akka-dns/target/2.12/shaded-akka-dns-2.12-0.1-SNAPSHOT.jar
   300 Mon Dec 18 13:53:12 AEDT 2017 META-INF/MANIFEST.MF
     0 Mon Dec 18 13:53:12 AEDT 2017 com/
     0 Mon Dec 18 13:53:12 AEDT 2017 com/lightbend/
     0 Mon Dec 18 13:53:12 AEDT 2017 com/lightbend/rp/
     0 Mon Dec 18 13:53:12 AEDT 2017 com/lightbend/rp/internal/
     0 Mon Dec 18 13:53:12 AEDT 2017 com/lightbend/rp/internal/akka/
     0 Mon Dec 18 13:53:12 AEDT 2017 com/lightbend/rp/internal/akka/io/
     0 Mon Dec 18 13:53:12 AEDT 2017 com/lightbend/rp/internal/ru/
     0 Mon Dec 18 13:53:12 AEDT 2017 com/lightbend/rp/internal/ru/smslv/
     0 Mon Dec 18 13:53:12 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/
     0 Mon Dec 18 13:53:12 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/
     0 Mon Dec 18 13:53:12 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/
  1479 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$11$$anonfun$apply$1.class
  2455 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$11.class
  1895 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$12.class
  1439 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$16$$anonfun$apply$2.class
  1480 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$16$$anonfun$apply$3.class
  2013 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$16.class
  1119 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$parseNameserverAddress$1.class
  1510 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$parseNameserverAddress$2.class
  1271 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$1.class
  2661 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$10.class
  1587 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$13.class
  1822 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$14.class
  1823 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$15.class
  1507 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$2.class
  1271 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$3.class
  1507 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$4.class
  1271 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$5.class
  1508 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$6.class
  2831 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$7.class
  2841 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$8.class
  2844 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$9.class
  1996 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$applyOrElse$1.class
  1996 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$applyOrElse$2.class
  1844 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$applyOrElse$3.class
  1844 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$applyOrElse$4.class
  1845 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$applyOrElse$5.class
  1845 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$applyOrElse$6.class
  1845 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1$$anonfun$applyOrElse$7.class
 16537 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$$anonfun$receive$1.class
  5761 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$.class
  4305 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$CurrentRequest$.class
  6483 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$CurrentRequest.class
  2735 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$SrvResolved$.class
  3639 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver$SrvResolved.class
 15980 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/akka/io/AsyncDnsResolver.class
  1456 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/AsyncDnsProvider.class
  2620 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/AAAARecord$.class
  6829 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/AAAARecord.class
  2595 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/ARecord$.class
  6790 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/ARecord.class
  2560 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Answer$.class
  5987 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Answer.class
  2390 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/CNAMERecord$.class
  6922 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/CNAMERecord.class
  8460 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/DnsClient$$anonfun$ready$1.class
  3197 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/DnsClient$$anonfun$receive$1.class
  9383 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/DnsClient.class
  1251 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/DomainName$$anonfun$parse$1.class
  1590 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/DomainName$$anonfun$write$1$$anonfun$apply$1.class
  1918 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/DomainName$$anonfun$write$1.class
  3197 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/DomainName$.class
  1182 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/DomainName.class
  1740 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Message$$anonfun$1.class
  1770 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Message$$anonfun$2.class
  1770 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Message$$anonfun$3.class
  1770 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Message$$anonfun$4.class
  1622 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Message$$anonfun$write$1.class
  1646 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Message$$anonfun$write$2.class
  1646 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Message$$anonfun$write$3.class
  1646 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Message$$anonfun$write$4.class
  6135 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Message$.class
 11217 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Message.class
  7010 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/MessageFlags$.class
  7957 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/MessageFlags.class
   956 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/OpCode$.class
  1435 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/OpCode.class
  2821 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Question$.class
  6487 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Question.class
  2207 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Question4$.class
  5159 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Question4.class
  2207 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Question6$.class
  5159 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/Question6.class
  1968 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/RecordClass$.class
  1994 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/RecordClass.class
  3743 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/RecordType$.class
  3413 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/RecordType.class
  3321 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/ResourceRecord$.class
  2867 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/ResourceRecord.class
  1316 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/ResponseCode$.class
  1838 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/ResponseCode.class
  2944 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/SRVRecord$.class
  8029 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/SRVRecord.class
  2225 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/SrvQuestion$.class
  5183 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/SrvQuestion.class
  2452 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/UnknownRecord$.class
  7225 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/UnknownRecord.class
   682 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/package$.class
   762 Mon Dec 18 13:52:28 AEDT 2017 com/lightbend/rp/internal/ru/smslv/akka/dns/raw/package.class
  1178 Mon Dec 18 13:53:12 AEDT 2017 reference.conf
```
