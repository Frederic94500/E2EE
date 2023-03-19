/*
 * Copyright (c) 1996, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package fr.upec.e2ee.protocol.Der;

import java.io.IOException;

/**
 * A DER input stream, used for parsing ASN.1 DER-encoded data such as
 * that found in X.509 certificates.  DER is a subset of BER/1, which has
 * the advantage that it allows only a single encoding of primitive data.
 * (High level data such as dates still support many encodings.)  That is,
 * it uses the "Definite" Encoding Rules (DER) not the "Basic" ones (BER).
 *
 * <P>Note that, like BER/1, DER streams are streams of explicitly
 * tagged data values.  Accordingly, this programming interface does
 * not expose any variant of the java.io.InputStream interface, since
 * that kind of input stream holds untagged data values and using that
 * I/O model could prevent correct parsing of the DER data.
 *
 * <P>At this time, this class supports only a subset of the types of DER
 * data encodings which are defined.  That subset is sufficient for parsing
 * most X.509 certificates.
 *
 * @author David Brownell
 * @author Amit Kapoor
 * @author Hemma Prafullchandra
 */

public class DerInputStream {

    // The static part
    final byte[] data;
    final int start;    // inclusive
    final int end;      // exclusive
    final boolean allowBER;

    // The moving part
    int pos;
    int mark;

    /**
     * Constructs a DerInputStream by assigning all its fields.
     * <p>
     * No checking on arguments since all callers are internal.
     * {@code data} should never be null even if length is 0.
     */
    public DerInputStream(byte[] data, int start, int length, boolean allowBER) {
        this.data = data;
        this.start = start;
        this.end = start + length;
        this.allowBER = allowBER;
        this.pos = start;
        this.mark = start;
    }

    /**
     * Reads a DerValue from this stream. After the call, the data pointer
     * is right after this DerValue so that the next call will read the
     * next DerValue.
     *
     * @return the read DerValue.
     * @throws IOException if a DerValue cannot be constructed starting from
     *                     this position because of byte shortage or encoding error.
     */
    public DerValue getDerValue() throws IOException {
        DerValue result = new DerValue(
                this.data, this.pos, this.end - this.pos, this.allowBER, true);
        if (result.buffer != this.data) {
            // Indefinite length observed. Unused bytes in data are appended
            // to the end of return value by DerIndefLenConverter::convertBytes
            // and stay inside result.buffer.
            int unused = result.buffer.length - result.end;
            this.pos = this.data.length - unused;
        } else {
            this.pos = result.end;
        }
        return result;
    }

    public DerValue[] getSequence(int startLen) throws IOException {
        return getDerValue().subs(DerValue.tag_Sequence, startLen);
    }

    /**
     * Returns the number of bytes available for reading.
     * This is most useful for testing whether the stream is
     * empty.
     */
    public int available() {
        return end - pos;
    }
}
