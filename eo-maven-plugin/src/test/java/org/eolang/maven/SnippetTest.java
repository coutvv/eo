/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2021 Yegor Bugayenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.eolang.maven;

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseProcess;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.cactoos.Func;
import org.cactoos.Input;
import org.cactoos.Output;
import org.cactoos.io.InputOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.ResourceOf;
import org.cactoos.io.TeeInput;
import org.cactoos.list.Joined;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.LengthOf;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.yaml.snakeyaml.Yaml;

/**
 * Integration test for simple snippets.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class SnippetTest {

    /**
     * Temp dir.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @TempDir
    public Path temp;

    @ParameterizedTest
    @MethodSource("yamlSnippets")
    @SuppressWarnings("unchecked")
    public void testFullRun(final String yml) throws Exception {
        final Yaml yaml = new Yaml();
        final Map<String, Object> map = yaml.load(
            new TextOf(
                new ResourceOf(
                    String.format("org/eolang/maven/snippets/%s", yml)
                )
            ).asString()
        );
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final int result = SnippetTest.run(
            this.temp,
            new InputOf(String.format("%s\n", map.get("eo"))),
            (List<String>) map.get("args"),
            new InputOf(map.get("in").toString()),
            new OutputTo(stdout)
        );
        MatcherAssert.assertThat(result, Matchers.equalTo(map.get("exit")));
        Logger.debug(this, "Stdout: \"%s\"", stdout.toString());
        for (final String ptn : (Iterable<String>) map.get("out")) {
            MatcherAssert.assertThat(
                new String(stdout.toByteArray(), StandardCharsets.UTF_8),
                Matchers.matchesPattern(
                    Pattern.compile(ptn, Pattern.DOTALL | Pattern.MULTILINE)
                )
            );
        }
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static String[] yamlSnippets() throws IOException {
        return new TextOf(
            new ResourceOf("org/eolang/maven/snippets/")
        ).asString().split("\n");
    }

    /**
     * Compile EO to Java and run.
     * @param tmp Temp dir
     * @param code EO sources
     * @param args Command line arguments
     * @param stdin The input
     * @param stdout Where to put stdout
     * @return All Java code
     * @throws Exception If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    @SuppressWarnings("unchecked")
    private static int run(final Path tmp, final Input code, final List<String> args,
        final Input stdin, final Output stdout) throws Exception {
        final Path src = tmp.resolve("src");
        new Save(code, src.resolve("code.eo")).save();
        final Path target = tmp.resolve("target");
        final Path foreign = target.resolve("eo-foreign.json");
        new Moja<>(RegisterMojo.class)
            .with("foreign", target.resolve("eo-foreign.json").toFile())
            .with("sourcesDir", src.toFile())
            .execute();
        new Moja<>(DemandMojo.class)
            .with("foreign", foreign.toFile())
            .with("objects", new ListOf<>("org.eolang.bool"))
            .execute();
        final Path home = Paths.get(
            System.getProperty(
                "runtime.path",
                Paths.get("").toAbsolutePath().resolve("eo-runtime").toString()
            )
        );
        new Moja<>(AssembleMojo.class)
            .with("outputDir", target.resolve("out").toFile())
            .with("targetDir", target.toFile())
            .with("foreign", foreign.toFile())
            .with("placed", target.resolve("list").toFile())
            .with(
                "objectionary",
                (Func<String, Input>) name -> new InputOf(
                    home.resolve(
                        String.format(
                            "src/main/eo/%s.eo",
                            name.replace(".", "/")
                        )
                    )
                )
            )
            .with("central", Central.EMPTY)
            .execute();
        final Path generated = target.resolve("generated");
        new Moja<>(TranspileMojo.class)
            .with("compiler", "canonical")
            .with("project", new MavenProjectStub())
            .with("targetDir", target.toFile())
            .with("generatedDir", generated.toFile())
            .with("foreign", foreign.toFile())
            .execute();
        final Path classes = target.resolve("classes");
        classes.toFile().mkdir();
        final String cpath = String.format(
            ".%s%s",
            File.pathSeparatorChar,
            System.getProperty(
                "runtime.jar",
                Paths.get(System.getProperty("user.home")).resolve(
                    String.format(
                        ".m2/repository/org/eolang/eo-runtime/%s/eo-runtime-%1$s.jar",
                        "1.0-SNAPSHOT"
                    )
                ).toString()
            )
        );
        SnippetTest.exec(
            String.format(
                "javac -encoding UTF-8 %s -d %s -cp %s",
                new Walk(generated).stream()
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .collect(Collectors.joining(" ")),
                classes,
                cpath
            ),
            generated
        );
        SnippetTest.exec(
            String.join(
                " ",
                new Joined<>(
                    new ListOf<>(
                        "java",
                        "-Dfile.encoding=UTF-8",
                        "-cp",
                        cpath,
                        "org.eolang.Main"
                    ),
                    args
                )
            ),
            classes,
            stdin,
            stdout
        );
        return 0;
    }

    /**
     * Run some command and print out the output.
     *
     * @param cmd The command
     * @param dir The home dir
     */
    private static void exec(final String cmd, final Path dir) {
        SnippetTest.exec(
            cmd, dir, new InputOf(""),
            new OutputTo(new ByteArrayOutputStream())
        );
    }

    /**
     * Run some command and print out the output.
     *
     * @param cmd The command
     * @param dir The home dir
     * @param stdin Stdin
     * @param stdout Stdout
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private static void exec(final String cmd, final Path dir,
        final Input stdin, final Output stdout) {
        Logger.debug(SnippetTest.class, "+%s", cmd);
        final String sysenc = System.getProperty("file.encoding");
        try {
            // @checkstyle MethodBodyCommentsCheck (5 lines)
            // We set here explicitly the default charset to UTF-8 because process
            // needs it to read properly the command that we pass to it.
            // We need it because process only works with the default charset,
            // which is by default the encoding of the system, not the one we were
            // running the tests with.
            changeDefaultCharset("UTF-8");
            final Process proc = new ProcessBuilder()
                .command(cmd.split(" "))
                .directory(dir.toFile())
                .redirectErrorStream(true)
                .start();
            new LengthOf(
                new TeeInput(
                    stdin,
                    new OutputTo(proc.getOutputStream())
                )
            ).value();
            try (VerboseProcess vproc = new VerboseProcess(proc)) {
                new LengthOf(
                    new TeeInput(
                        new InputOf(vproc.stdout()),
                        stdout
                    )
                ).value();
            }
        // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Exception ex) {
            throw new IllegalStateException(ex);
        } finally {
            // @checkstyle MethodBodyCommentsCheck (1 line)
            // We restore here the default charset.
            changeDefaultCharset(sysenc);
        }
    }

    /**
     * Change default charset.
     * @param charset Charset
     */
    @SuppressWarnings({"unchecked", "PMD.AvoidCatchingGenericException"})
    private static void changeDefaultCharset(final String charset) {
        try {
            System.setProperty("file.encoding", charset);
            final Field field = Charset.class.getDeclaredField("defaultCharset");
            field.setAccessible(true);
            field.set(null, null);
        // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Exception exc) {
            throw new IllegalArgumentException(exc);
        }
    }
}
