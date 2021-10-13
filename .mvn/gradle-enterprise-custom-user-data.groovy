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
import com.gradle.maven.extension.api.scan.BuildScanApi

/**
 * Captures the Maven active profiles and add them as tags to the Build Scan. The goal is to make it simpler to
 * filter builds by filtering on Maven profiles.
 */

BuildScanApi buildScan = session.lookup('com.gradle.maven.extension.api.scan.BuildScanApi')
if (!buildScan) {
    return
}

buildScan.executeOnce('tag-profiles') { BuildScanApi buildScanApi ->
    project.activeProfiles.each { profile -> buildScanApi.tag profile.id }
}
