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

import android.os.Build;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a single DER-encoded value.  DER encoding rules are a subset
 * of the "Basic" Encoding Rules (BER), but they only support a single way
 * ("Definite" encoding) to encode any given value.
 *
 * <P>All DER-encoded data are triples <em>{type, length, data}</em>.  This
 * class represents such tagged values as they have been read (or constructed),
 * and provides structured access to the encoded data.
 *
 * <P>At this time, this class supports only a subset of the types of DER
 * data encodings which are defined.  That subset is sufficient for parsing
 * most X.509 certificates, and working with selected additional formats
 * (such as PKCS #10 certificate requests, and some kinds of PKCS #7 data).
 * <p>
 * A note with respect to T61/Teletex strings: From RFC 1617, section 4.1.3
 * and RFC 5280, section 8, we assume that this kind of string will contain
 * ISO-8859-1 characters only.
 *
 * @author David Brownell
 * @author Amit Kapoor
 * @author Hemma Prafullchandra
 */
public class DerValue {

    /*
     * The type starts at the first byte of the encoding, and
     * is one of these tag_* values.  That may be all the type
     * data that is needed.
     */

    /*
     * These tags are the "universal" tags ... they mean the same
     * in all contexts.  (Mask with 0x1f -- five bits.)
     */

    /**
     * Tag value indicating an ASN.1 "BOOLEAN" value.
     */
    public static final byte tag_Boolean = 0x01;

    /**
     * Tag value indicating an ASN.1 "INTEGER" value.
     */
    public static final byte tag_Integer = 0x02;

    // CONSTRUCTED seq/set

    /**
     * Tag value indicating an ASN.1
     * "SEQUENCE" (zero to N elements, order is significant).
     */
    public static final byte tag_Sequence = 0x30;

    // This class is mostly immutable except that:
    //
    // 1. resetTag() modifies the tag
    // 2. the data field is mutable
    //
    // For compatibility, data, getData() and resetTag() are preserved.
    // A modern caller should call withTag() or data() instead.
    //
    // Also, some constructors have not cloned buffer, so the data could
    // be modified externally.
    // Unsafe. Legacy. Never null.
    public final DerInputStream data;
    final byte[] buffer;
    final int end;
    private final int start;
    private final boolean allowBER;
    public /*final*/ byte tag;

    /*
     * These values are the high order bits for the other kinds of tags.
     */

    /**
     * Creates a new DerValue by specifying all its fields.
     */
    DerValue(byte tag, byte[] buffer, int start, int end, boolean allowBER) {
        if ((tag & 0x1f) == 0x1f) {
            throw new IllegalArgumentException("Tag number over 30 is not supported");
        }
        this.tag = tag;
        this.buffer = buffer;
        this.start = start;
        this.end = end;
        this.allowBER = allowBER;
        this.data = data();
    }

    DerValue(byte tag, byte[] buffer, boolean allowBER) {
        this(tag, buffer, 0, buffer.length, allowBER);
    }

    /**
     * Creates a DerValue from a tag and some DER-encoded data.
     * <p>
     * This is a public constructor.
     *
     * @param tag    the DER type tag
     * @param buffer the DER-encoded data
     */
    public DerValue(byte tag, byte[] buffer) {
        this(tag, buffer.clone(), true);
    }

    /**
     * Parse an ASN.1 encoded datum from a byte array.
     *
     * @param buf       the byte array containing the DER-encoded datum
     * @param offset    where the encoded datum starts inside {@code buf}
     * @param len       length of bytes to parse inside {@code buf}
     * @param allowBER  whether BER is allowed
     * @param allowMore whether extra bytes are allowed after the encoded datum.
     *                  If true, {@code len} can be bigger than the length of
     *                  the encoded datum.
     * @throws IOException if it's an invalid encoding or there are extra bytes
     *                     after the encoded datum and {@code allowMore} is false.
     */
    DerValue(byte[] buf, int offset, int len, boolean allowBER, boolean allowMore)
            throws IOException {

        if (len < 2) {
            throw new IOException("Too short");
        }
        int pos = offset;
        tag = buf[pos++];
        if ((tag & 0x1f) == 0x1f) {
            throw new IOException("Tag number over 30 at " + offset + " is not supported");
        }
        int lenByte = buf[pos++];

        int length;
        if (lenByte == (byte) 0x80) { // indefinite length
            if (!allowBER) {
                throw new IOException("Indefinite length encoding " +
                        "not supported with DER");
            }
            if (!isConstructed()) {
                throw new IOException("Indefinite length encoding " +
                        "not supported with non-constructed data");
            }

            // Reconstruct data source
            buf = DerIndefLenConverter.convertStream(
                    new ByteArrayInputStream(buf, pos, len - (pos - offset)), tag);
            offset = 0;
            len = buf.length;
            pos = 2;

            if (tag != buf[0]) {
                throw new IOException("Indefinite length encoding not supported");
            }
            lenByte = buf[1];
            if (lenByte == (byte) 0x80) {
                throw new IOException("Indefinite len conversion failed");
            }
        }

        if ((lenByte & 0x080) == 0x00) { // short form, 1 byte datum
            length = lenByte;
        } else {                     // long form
            lenByte &= 0x07f;
            if (lenByte > 4) {
                throw new IOException("Invalid lenByte");
            }
            if (len < 2 + lenByte) {
                throw new IOException("Not enough length bytes");
            }
            length = 0x0ff & buf[pos++];
            lenByte--;
            if (length == 0 && !allowBER) {
                // DER requires length value be encoded in minimum number of bytes
                throw new IOException("Redundant length bytes found");
            }
            while (lenByte-- > 0) {
                length <<= 8;
                length += 0x0ff & buf[pos++];
            }
            if (length < 0) {
                throw new IOException("Invalid length bytes");
            } else if (length <= 127 && !allowBER) {
                throw new IOException("Should use short form for length");
            }
        }
        // pos is now at the beginning of the content
        if (len - length < pos - offset) {
            throw new EOFException("not enough content");
        }
        if (len - length > pos - offset && !allowMore) {
            throw new IOException("extra data at the end");
        }
        this.buffer = buf;
        this.start = pos;
        this.end = pos + length;
        this.allowBER = allowBER;
        this.data = data();
    }

    /**
     * Returns true iff the CONSTRUCTED bit is set in the type tag.
     */
    public boolean isConstructed() {
        return ((tag & 0x020) == 0x020);
    }

    /**
     * Returns a new DerInputStream pointing at the start of this
     * DerValue's content.
     *
     * @return the new DerInputStream value
     */
    public final DerInputStream data() {
        return new DerInputStream(buffer, start, end - start, allowBER);
    }

    /**
     * Returns an ASN.1 BOOLEAN
     *
     * @return the boolean held in this DER value
     */
    public boolean getBoolean() throws IOException {
        if (tag != tag_Boolean) {
            throw new IOException("DerValue.getBoolean, not a BOOLEAN " + tag);
        }
        if (end - start != 1) {
            throw new IOException("DerValue.getBoolean, invalid length "
                    + (end - start));
        }
        data.pos = data.end; // Compatibility. Reach end.
        return buffer[start] != 0;
    }

    /**
     * Returns an ASN.1 INTEGER value as a positive BigInteger.
     * This is just to deal with implementations that incorrectly encode
     * some values as negative.
     *
     * @return the integer held in this DER value as a BigInteger.
     */
    public BigInteger getPositiveBigInteger() throws IOException {
        return getBigIntegerInternal(tag_Integer, true);
    }

    public fr.upec.e2ee.protocol.math.BigInteger getPositiveBigInteger2() throws IOException {
        return getBigIntegerInternal2(tag_Integer, true);
    }

    /**
     * Returns a BigInteger value
     *
     * @param makePositive whether to always return a positive value,
     *                     irrespective of actual encoding
     * @return the integer as a BigInteger.
     */
    private BigInteger getBigIntegerInternal(byte expectedTag, boolean makePositive) throws IOException {
        if (tag != expectedTag) {
            throw new IOException("DerValue.getBigIntegerInternal, not expected " + tag);
        }
        if (end == start) {
            throw new IOException("Invalid encoding: zero length Int value");
        }
        data.pos = data.end; // Compatibility. Reach end.
        if (!allowBER && (end - start >= 2 && (buffer[start] == 0) && (buffer[start + 1] >= 0))) {
            throw new IOException("Invalid encoding: redundant leading 0s");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return makePositive
                    ? new BigInteger(1, buffer, start, end - start)
                    : new BigInteger(buffer, start, end - start);
        }
        throw new IllegalStateException("Unexpected state!");
    }

    private fr.upec.e2ee.protocol.math.BigInteger getBigIntegerInternal2(byte expectedTag, boolean makePositive) throws IOException {
        if (tag != expectedTag) {
            throw new IOException("DerValue.getBigIntegerInternal, not expected " + tag);
        }
        if (end == start) {
            throw new IOException("Invalid encoding: zero length Int value");
        }
        data.pos = data.end; // Compatibility. Reach end.
        if (!allowBER && (end - start >= 2 && (buffer[start] == 0) && (buffer[start + 1] >= 0))) {
            throw new IOException("Invalid encoding: redundant leading 0s");
        }
        return makePositive
                ? new fr.upec.e2ee.protocol.math.BigInteger(1, buffer, start, end - start)
                : new fr.upec.e2ee.protocol.math.BigInteger(buffer, start, end - start);
    }

    /**
     * Bitwise equality comparison.  DER encoded values have a single
     * encoding, so that bitwise equality of the encoded values is an
     * efficient way to establish equivalence of the unencoded values.
     *
     * @param o the object being compared with this one
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DerValue)) {
            return false;
        }
        DerValue other = (DerValue) o;
        if (tag != other.tag) {
            return false;
        }
        if (buffer == other.buffer && start == other.start && end == other.end) {
            return true;
        }
        return Arrays.equals(buffer, start, end, other.buffer, other.start, other.end);
    }

    /**
     * Returns a printable representation of the value.
     *
     * @return printable representation of the value
     */
    @Override
    public String toString() {
        return String.format("DerValue(%02x, %s, %d, %d)",
                0xff & tag, buffer, start, end);
    }

    /**
     * Returns a DER-encoded value, such that if it's passed to the
     * DerValue constructor, a value equivalent to "this" is returned.
     *
     * @return DER-encoded value, including tag and length.
     */
    public byte[] toByteArray() {
        data.pos = data.start; // Compatibility. At head.
        // Minimize content duplication by writing out tag and length only
        DerOutputStream out = new DerOutputStream();
        out.write(tag);
        out.putLength(end - start);
        int headLen = out.size();
        byte[] result = Arrays.copyOf(out.buf(), end - start + headLen);
        System.arraycopy(buffer, start, result, headLen, end - start);
        return result;
    }

    /**
     * Get the length of the encoded value.
     */
    public int length() {
        return end - start;
    }

    /**
     * Returns a hashcode for this DerValue.
     *
     * @return a hashcode for this DerValue.
     */
    @Override
    public int hashCode() {
        int result = tag;
        for (int i = start; i < end; i++) {
            result = 31 * result + buffer[i];
        }
        return result;
    }

    /**
     * Reads the sub-values in a constructed DerValue.
     *
     * @param expectedTag the expected tag, or zero if we don't check.
     *                    This is useful when this DerValue is IMPLICIT.
     * @param startLen    estimated number of sub-values
     * @return the sub-values in an array
     */
    DerValue[] subs(byte expectedTag, int startLen) throws IOException {
        if (expectedTag != 0 && expectedTag != tag) {
            throw new IOException("Not the correct tag");
        }
        List<DerValue> result = new ArrayList<>(startLen);
        DerInputStream dis = data();
        while (dis.available() > 0) {
            result.add(dis.getDerValue());
        }
        return result.toArray(new DerValue[0]);
    }
}
