/*
 * Copyright (c) 1996, 2021, Oracle and/or its affiliates. All rights reserved.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * Output stream marshaling DER-encoded data.  This is eventually provided
 * in the form of a byte array; there is no advance limit on the size of
 * that byte array.
 *
 * <P>At this time, this class supports only a subset of the types of
 * DER data encodings which are defined.  That subset is sufficient for
 * generating most X.509 certificates.
 *
 * @author David Brownell
 * @author Amit Kapoor
 * @author Hemma Prafullchandra
 */
public class DerOutputStream
        extends ByteArrayOutputStream implements DerEncoder {
    /**
     * Construct an DER output stream.
     *
     * @param size how large a buffer to preallocate.
     */
    public DerOutputStream(int size) {
        super(size);
    }

    /**
     * Construct an DER output stream.
     */
    public DerOutputStream() {
    }

    /*
     * PRIMITIVES -- these are "universal" ASN.1 simple types.
     *
     *  BOOLEAN, INTEGER, BIT STRING, OCTET STRING, NULL
     *  OBJECT IDENTIFIER, SEQUENCE(OF), SET(OF)
     *  PrintableString, T61String, IA5String, UTCTime
     */

    /**
     * Marshals a DER integer on the output stream.
     *
     * @param i the integer in the form of a BigInteger.
     */
    public void putInteger(BigInteger i) {
        write(DerValue.tag_Integer);
        byte[] buf = i.toByteArray(); // least number  of bytes
        putLength(buf.length);
        write(buf, 0, buf.length);
    }

    /**
     * Put the encoding of the length in the stream.
     *
     * @param len the length of the attribute.
     */
    public void putLength(int len) {
        if (len < 128) {
            write((byte) len);

        } else if (len < (1 << 8)) {
            write((byte) 0x081);
            write((byte) len);

        } else if (len < (1 << 16)) {
            write((byte) 0x082);
            write((byte) (len >> 8));
            write((byte) len);

        } else if (len < (1 << 24)) {
            write((byte) 0x083);
            write((byte) (len >> 16));
            write((byte) (len >> 8));
            write((byte) len);

        } else {
            write((byte) 0x084);
            write((byte) (len >> 24));
            write((byte) (len >> 16));
            write((byte) (len >> 8));
            write((byte) len);
        }
    }

    /**
     * Write the current contents of this <code>DerOutputStream</code>
     * to an <code>OutputStream</code>.
     *
     * @throws IOException on output error.
     */
    public void derEncode(OutputStream out) throws IOException {
        out.write(toByteArray());
    }

    byte[] buf() {
        return buf;
    }
}
