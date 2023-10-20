Yauaa: Yet Another UserAgent Analyzer
========================================
[![Github actions Build status](https://img.shields.io/github/actions/workflow/status/nielsbasjes/yauaa/build.yml?branch=main)](https://github.com/nielsbasjes/yauaa/actions)
[![Coverage Status](https://img.shields.io/codecov/c/github/nielsbasjes/yauaa)](https://app.codecov.io/gh/nielsbasjes/yauaa)
[![License](https://img.shields.io/:license-apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://img.shields.io/maven-central/v/nl.basjes.parse.useragent/yauaa-parent.svg)](https://central.sonatype.com/namespace/nl.basjes.parse.useragent)
[![Reproducible Builds](https://img.shields.io/badge/Reproducible_Builds-ok-success?labelColor=1e5b96)](https://github.com/jvm-repo-rebuild/reproducible-central#nl.basjes.parse.useragent:yauaa)
[![GitHub stars](https://img.shields.io/github/stars/nielsbasjes/yauaa?label=GitHub%20stars)](https://github.com/nielsbasjes/yauaa/stargazers)
[![Docker Hub](https://img.shields.io/docker/pulls/nielsbasjes/yauaa)](https://hub.docker.com/r/nielsbasjes/yauaa)
[![If this project has business value for you then don't hesitate to support me with a small donation.](https://img.shields.io/badge/Sponsor%20me-via%20Github-red.svg)](https://github.com/sponsors/nielsbasjes)
[![If this project has business value for you then don't hesitate to support me with a small donation.](https://img.shields.io/badge/Donations-via%20Paypal-red.svg)](https://www.paypal.me/nielsbasjes)
[![Website](https://img.shields.io/badge/https://-yauaa.basjes.nl-blue.svg)](https://yauaa.basjes.nl/)

This is a java library that tries to parse and analyze the useragent string (and when available the User-Agent Client Hints) and extract as many relevant attributes as possible.

Works with Java, Scala, Kotlin and provides ready for use UDFs for several processing systems.

The full documentation can be found here [https://yauaa.basjes.nl](https://yauaa.basjes.nl)

# CL Build info
EMR Compatible Build
===========================
!!!!!! IMPORTANT !!!!!!!
For building emr compatible udf please follow this steps:
   - Sync master branch with nielsbasjes/yauaa:main. in github web UI
   - checkout cl-emr-fix:
      `git checkout cl-emr-fix`
   - pull from master:
      `git pull origin master`
   - create new branch:
      `git checkout cl-emr-fix-current-data`

There is a script that builds docker container in which JARS are compiled.
To run build script in root of repo execute:
`bash build-cl-yauaa.sh`

After script completes new jars can be found in udfs/hive/target directory.

Built jar can be found in udfs/hive/target/yauaa-hive-5.11-SNAPSHOT-`udf.jar. You can see example usage [here](https://yauaa.basjes.nl/udf/apache-hive/).




Try it!
=======
You can try it online with your own browser here: [https://try.yauaa.basjes.nl/](https://try.yauaa.basjes.nl/).

**NOTES**
1. This runs on a very slow and rate limited machine.
2. If you really like this then run it on your local systems. It's much faster that way.
   A Kubernetes ready Docker image is provided. See this page about the [WebServlet](https://yauaa.basjes.nl/using/webservlet) for more information.

Donations
===
If this project has business value for you then don't hesitate to support me with a small donation.

[![If this project has business value for you then don't hesitate to support me with a small donation.](https://img.shields.io/badge/Donations-via%20Github-red.svg)](https://github.com/sponsors/nielsbasjes)
[![If this project has business value for you then don't hesitate to support me with a small donation.](https://img.shields.io/badge/Donations-via%20Paypal-red.svg)](https://www.paypal.me/nielsbasjes)

License
=======

    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2023 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
