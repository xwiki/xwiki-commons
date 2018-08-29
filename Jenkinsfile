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
        }
      }

      // If the "main" build has succeeded then trigger the rendering pipeline
      build job: "../xwiki-rendering/${env.BRANCH_NAME}", wait: false
    },
    'testrelease': {
      node {
        // Simulate a release and verify all is fine, in preparation for the release day.
        xwikiBuild('TestRelease') {
          xvnc = false
          mavenOpts = globalMavenOpts
          goals = 'clean install'
          profiles = 'legacy,integration-tests'
          properties = '-DskipTests -DperformRelease=true -Dgpg.skip=true -Dxwiki.checkstyle.skip=true'
        }
      }
    },
    'quality': {
      node {
        // Run the quality checks.
        xwikiBuild('Quality') {
          xvnc = false
          mavenOpts = globalMavenOpts
          goals = 'clean install jacoco:report'
          profiles = 'quality,legacy'
        }
      }
    },
    'checkstyle': {
      node {
        // Build with checkstyle. Make sure "mvn checkstyle:check" passes so that we don't cause false positive on
        // Checkstyle side. This is for the Checkstyle project itself so that they can verify that when they bring
        // changes to Checkstyle, there's no regression to the XWiki build.
        xwikiBuild('Checkstyle') {
          xvnc = false
          mavenOpts = globalMavenOpts
          goals = 'clean test-compile checkstyle:check'
          profiles = 'legacy'
        }
      }
    }
  )

  // If the job is successful, trigger the rendering job
  if (currentBuild.result == 'SUCCESS') {
    build job: "../xwiki-rendering/${env.BRANCH_NAME}", wait: false
  }
}




