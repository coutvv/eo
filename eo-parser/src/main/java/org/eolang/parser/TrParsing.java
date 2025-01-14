/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2024 Objectionary.com
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

import com.yegor256.xsline.TrClasspath;
import com.yegor256.xsline.TrEnvelope;
import com.yegor256.xsline.TrFast;
import com.yegor256.xsline.TrLambda;
import com.yegor256.xsline.TrLogged;
import java.util.logging.Level;

/**
 * Train of XSL shifts.
 *
 * @since 0.1
 */
public final class TrParsing extends TrEnvelope {
    /**
     * Ctor.
     */
    public TrParsing() {
        super(
            new TrStepped(
                new TrFast(
                    new TrLambda(
                        new TrLogged(
                            new TrClasspath<>(
                                "/org/eolang/parser/cti-adds-errors.xsl",
                                "/org/eolang/parser/add-refs.xsl",
                                "/org/eolang/parser/expand-qqs.xsl",
                                "/org/eolang/parser/add-probes.xsl",
                                "/org/eolang/parser/vars-float-up.xsl",
                                "/org/eolang/parser/add-refs.xsl",
                                "/org/eolang/parser/expand-aliases.xsl",
                                "/org/eolang/parser/resolve-aliases.xsl",
                                "/org/eolang/parser/add-refs.xsl",
                                "/org/eolang/parser/add-default-package.xsl",
                                "/org/eolang/parser/explicit-data.xsl",
                                "/org/eolang/parser/set-locators.xsl",
                                "/org/eolang/parser/clean-up.xsl"
                            ).back(),
                            TrParsing.class,
                            Level.FINEST
                        ),
                        StEoLogged::new
                    ),
                    TrFast.class,
                    500L
                )
            )
        );
    }
}
