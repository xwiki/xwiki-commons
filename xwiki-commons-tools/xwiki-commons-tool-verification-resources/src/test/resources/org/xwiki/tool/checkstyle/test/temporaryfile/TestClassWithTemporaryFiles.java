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
package org.xwiki.tool.checkstyle.test.temporaryfile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestClassWithTemporaryFiles
{
    public void violations(Path directory) throws Exception
    {
        // Violation: hardcoded system temporary directory.
        File a = new File("/tmp/xwiki");
        Path b = Path.of("/var/tmp/test");
        // Violation: createTempFile without an explicit parent directory.
        File c = File.createTempFile("prefix", "suffix");
        // Violation: Files temporary methods with a String literal as first argument.
        Path d = Files.createTempFile("prefix", "suffix");
        Path e = Files.createTempDirectory("prefix");
    }

    public void noViolations(Path directory) throws Exception
    {
        // OK: not the system temporary directory.
        File a = new File("/permanent/xwiki");
        File b = new File("target/foo");
        // OK: starts with the characters "/tmp" but is NOT the system temporary directory (path-segment boundary).
        File f = new File("/tmpdir");
        String pattern = "/tmpdir/temp/chart/page.png";
        File g = new File("/var/tmpfoo");
        // OK: "/tmp" is not at the start of the literal.
        String script = "do new File(\"/tmp/exploit\")";
        // OK: explicit parent directory is passed.
        File c = File.createTempFile("prefix", "suffix", directory.toFile());
        Path d = Files.createTempFile(directory, "prefix", "suffix");
        Path e = Files.createTempDirectory(directory, "prefix");
    }
}
