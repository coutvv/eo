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
package org.eolang;

import java.nio.charset.StandardCharsets;

/**
 * Class to print {@link Phi} objects with UTF8 String data correctly.
 * @since 0.35
 * @checkstyle AbbreviationAsWordInNameCheck (5 lines)
 */
public final class PhContainingUTF8 implements Phi {

    /**
     * Origin.
     */
    private final Phi origin;

    /**
     * Ctor.
     * @param origin Origin.
     */
    public PhContainingUTF8(final Phi origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        return String.format(
            "{%s: Δ as String = \"%s\"}",
            this.origin.toString(),
            new String(
                new Dataized(this.origin).take(),
                StandardCharsets.UTF_8
            )
        );
    }

    @Override
    public Phi copy() {
        return this.origin.copy();
    }

    @Override
    public Attr attr(final int pos) {
        return this.origin.attr(pos);
    }

    @Override
    public Attr attr(final String name) {
        return this.origin.attr(name);
    }

    @Override
    public String locator() {
        return this.origin.locator();
    }

    @Override
    public String forma() {
        return this.origin.forma();
    }

    @Override
    public String φTerm() {
        return this.origin.φTerm();
    }
}