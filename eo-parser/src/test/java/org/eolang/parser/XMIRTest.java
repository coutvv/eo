/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2023 Objectionary.com
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
package org.eolang.parser;

import com.jcabi.log.Logger;
import com.jcabi.xml.ClasspathSources;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import com.yegor256.Jaxec;
import com.yegor256.xsline.TrClasspath;
import com.yegor256.xsline.TrLogged;
import com.yegor256.xsline.Xsline;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cactoos.io.InputOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;
import org.eolang.jucs.ClasspathSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Test case for {@link XMIR}.
 *
 * @since 0.5
 * @checkstyle AbbreviationAsWordInNameCheck (500 lines)
 */
final class XMIRTest {

    /**
     * This is not really a test case, but a procedure that has to be
     * executed together with all tests.
     *
     * <p>The purpose of this procedure is to convert
     * {@code src/main/antlr4/org/eolang/parser/Program.g4}
     * (the ANTLR4 grammar file) to the {@code target/ebnf.txt} text file,
     * which later can be used by the {@see https://ctan.org/pkg/naive-ebnf}.</p>
     *
     * @param temp Temp directory, where LaTeX will be compiled, just for test
     * @since 0.30.0
     *
     * @todo #2399:30min The test fails on GitHub Actions CI. Need to figure out what's the problem
     *  Stack trace:
     *  java.lang.IllegalArgumentException: Non-zero exit code 1: This is pdfTeX,
     *  Version 3.141592653-2.6-1.40.25 (T..4496..duced!\nTranscript written on article.log.\n
     * 	  at com.jcabi.log.VerboseProcess.stdout(VerboseProcess.java:298)
     * 	  at com.jcabi.log.VerboseProcess.stdout(VerboseProcess.java:185)
     * 	  at com.yegor256.Jaxec.execUnsafe(Jaxec.java:289)
     * 	  at com.yegor256.Jaxec.exec(Jaxec.java:258)
     * 	  at org.eolang.parser.XMIRTest.convertsAntlrToEbnf(XMIRTest.java:148)
     */
    @Test
    @Disabled
    void convertsAntlrToEbnf(@TempDir final Path temp) throws Exception {
        String home = System.getenv("CONVERT_PATH");
        if (home == null) {
            home = "/code/convert-master/build/lib";
        }
        final Path lib = Paths.get(home);
        Assumptions.assumeTrue(
            lib.toFile().exists(),
            String.format("The JAR of the 'convert' tool is not available at '%s'", lib)
        );
        final List<String> jars = Stream.of(lib.toFile().listFiles())
            .filter(file -> !file.isDirectory())
            .map(File::getAbsolutePath)
            .collect(Collectors.toList());
        final String output = new Jaxec(
            "java",
            "-cp",
            String.join(":", jars),
            "de.bottlecaps.convert.Convert",
            "-xml",
            "src/main/antlr4/org/eolang/parser/Program.g4"
        ).exec();
        if (!output.startsWith("<?xml")) {
            Logger.warn(this, "Stdout: %n%s", output);
        }
        final XML xml = new XMLDocument(output);
        final XML after = new Xsline(
            new TrLogged(
                new TrClasspath<>()
                    .with("/org/eolang/parser/ebnf/to-non-terminals.xsl")
                    .with("/org/eolang/parser/ebnf/catch-duplicates.xsl")
                    .with("/org/eolang/parser/ebnf/to-ebnf.xsl")
                    .back(),
                XMIRTest.class,
                Level.FINE
            )
        ).pass(xml);
        final String ebnf = after.xpath("/ebnf/text()").get(0).replaceAll(" +", " ");
        final Path target = Paths.get("target/ebnf.txt");
        Files.write(
            target,
            ebnf.getBytes(StandardCharsets.UTF_8)
        );
        Logger.debug(this, "EBNF generated into %s:%n%s", target, ebnf);
        Logger.debug(this, "XML generated by the convert:%n%s", output);
        MatcherAssert.assertThat(
            new String(Files.readAllBytes(target), StandardCharsets.UTF_8),
            Matchers.allOf(
                Matchers.containsString("<byte> := /[0-9A-F]/ /[0-9A-F]/ \\\\"),
                Matchers.containsString("<string> := 'DQ' {"),
                Matchers.containsString("<text> := 'DQ' 'DQ' 'DQ' ["),
                Matchers.containsString("\"\\textvisiblespace{}\""),
                Matchers.containsString("\"\\textquotesingle{}\"")
            )
        );
        Files.write(
            temp.resolve("article.tex"),
            new TextOf(new ResourceOf("tex/ebnf.tex"))
                .asString()
                .replace("EBNF", ebnf)
                .getBytes(StandardCharsets.UTF_8)
        );
        new Jaxec(
            "pdflatex",
            "-interaction=errorstopmode",
            "-halt-on-error",
            "-shell-escape",
            "article"
        ).withHome(temp).exec();
    }

    /**
     * Convert EO to xmir and back and compare.
     * @param src EO source.
     * @throws Exception If fails.
     *
     * @todo #2399:30min "idiomatic.eo" is not converted successfully. After introducing new grammar
     *  ({@see Program.g4}) example with name "idiomatic.eo" is not converted successfully in the
     *  test, so it was moved from {@link org.eolang.parser.xmir-samples} to
     *  {@link org.eolang.parser.xmir-samples-wrong}. Need to figure what's the problem and move it
     *  back to the origin folder.
     */
    @ParameterizedTest
    @ClasspathSource(value = "org/eolang/parser/xmir-samples/", glob = "**.eo")
    void printsToEO(final String src) throws Exception {
        Logger.debug(this, "Original EOLANG:%n%s", src);
        final XML first = XMIRTest.clean(XMIRTest.parse(src));
        Logger.debug(this, "First:%n%s", first);
        final String eolang = new XMIR(first).toEO();
        Logger.debug(this, "EOLANG:%n%s", eolang);
        final XML second = XMIRTest.clean(XMIRTest.parse(eolang));
        Logger.debug(this, "Second:%n%s", second);
        final String ignore = "data=\"\\S+\"";
        MatcherAssert.assertThat(
            first
                .toString()
                .replaceAll(ignore, ""),
            Matchers.equalTo(
                second
                    .toString()
                    .replaceAll(ignore, "")
            )
        );
    }

    /**
     * Parse EO code to XMIR.
     * @param source The source
     * @return XMIR
     * @throws IOException If fails
     */
    private static XML parse(final String source) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Syntax syntax = new Syntax(
            "test", new InputOf(source), new OutputTo(baos)
        );
        syntax.parse();
        final XSL wrap = new XSLDocument(
            XMIRTest.class.getResourceAsStream("wrap-method-calls.xsl")
        ).with(new ClasspathSources());
        return wrap.transform(new XMLDocument(baos.toByteArray()));
    }

    /**
     * Take the clean version of XML, without the noise.
     * @param xmir The original
     * @return Clean one
     */
    private static XML clean(final XML xmir) {
        return new XSLDocument(
            XMIRTest.class.getResourceAsStream("strip-xmir.xsl")
        ).with(new ClasspathSources()).transform(xmir);
    }

}
