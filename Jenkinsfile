/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

// It's assumed that Jenkins has been configured to implicitly load the vars/*.groovy libraries.
// Note that the version used is the one defined in Jenkins but it can be overridden as follows:
// @Library("XWiki@<branch, tag, sha1>") _
// See https://github.com/jenkinsci/workflow-cps-global-lib-plugin for details.

def globalMavenOpts = '-Xmx1024m -Xms256m'

stage ('Commons Builds') {
  parallel(
    'main': {
      node {
        // Build, skipping quality checks so that the result of the build can be sent as fast as possible to the devs.
        // In addition, we want the generated artifacts to be deployed to our remote Maven repository so that developers
        // can benefit from them even though some quality checks have not yet passed. In // we start a build with the
        // quality profile that executes various quality checks.
        xwikiBuild('Main') {
          xvnc = false
          mavenOpts = globalMavenOpts
          profiles = 'legacy,integration-tests'
          properties = '-Dxwiki.checkstyle.skip=true -Dxwiki.surefire.captureconsole.skip=true -Dxwiki.revapi.skip=true'
          javadoc = false
        }

        // If the "main" build has succeeded then trigger the rendering pipeline.
        // Note: we don't wait for the other builds to have finished since we want rendering to be triggered ASAP and
        // the Commons artifacts will be available for Rendering modules to build fine.
        build job: "../xwiki-rendering/${env.BRANCH_NAME}", wait: false

        // Note: we need to run this build after the "main" one since it requires that the artifacts have been built
        // and are available from the Maven Remote repository (or locally). When we upgrade the Commons version, it
        // would fail to execute the first time otherwise.

        // Build with checkstyle. Make sure "mvn checkstyle:check" passes so that we don't cause false positive on
        // Checkstyle side. This is for the Checkstyle project itself so that they can verify that when they bring
        // changes to Checkstyle, there's no regression to the XWiki build.
        xwikiBuild('Checkstyle') {
          xvnc = false
          mavenOpts = globalMavenOpts
          goals = 'checkstyle:check@default'
          javadoc = false
        }
      }
    },
    'testrelease': {
      node {
        // Simulate a release and verify all is fine, in preparation for the release day.
        xwikiBuild('TestRelease') {
          xvnc = false
          mavenOpts = globalMavenOpts
          goals = 'clean install'
          profiles = 'legacy,integration-tests'
          properties = '-DskipTests -DperformRelease=true -Dgpg.skip=true -Dxwiki.checkstyle.skip=true -Ddoclint=all'
          javadoc = false
        }
      }
    },
    'quality': {
      node {
        // Run the quality checks.
        // Sonar notes:
        // - we need sonar:sonar to perform the analysis
        // - we need sonar = true to push the analysis to Sonarcloud
        // - we need jacoco:report to execute jacoco and compute test coverage
        // - we need -Pcoverage and -Dxwiki.jacoco.itDestFile to tell Jacoco to compute a single global Jacoco
        //   coverage for the full reactor (so that the coverage percentage computed takes into account module tests
        //   which cover code in other modules)
        xwikiBuild('Quality') {
          xvnc = false
          mavenOpts = globalMavenOpts
          goals = 'clean install jacoco:report sonar:sonar'
          profiles = 'quality,legacy,coverage'
          // Note: We specify the "jvm" system property to to execute the tests with Java 8 in order to limit problems
          // with more recent versions of Java. In the future, we'll need to be able to also execute the tests with
          // Java 14+. Remove that when we support it. See for example https://jira.xwiki.org/browse/XCOMMONS-2136
          properties = '--settings /root/.m2/xwiki-commons-settings.xml -Dxwiki.jacoco.itDestFile=`pwd`/target/jacoco-it.exec -Djvm=/home/hudsonagent/java8/bin/java'
          sonar = true
          javadoc = false
          // Build with Java 14 since Sonar requires Java 11+ and we want at the same time to verify that XWiki builds
          // with the latest Java version.
          javaTool = 'java14'
        }
      }
    }
  )
}




