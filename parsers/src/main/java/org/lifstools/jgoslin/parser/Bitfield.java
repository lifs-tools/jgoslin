/*
MIT License

Copyright (c) the authors (listed in global LICENSE file)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package org.lifstools.jgoslin.parser;

import org.lifstools.jgoslin.domain.ConstraintViolationException;

/**
 *
 * @author dominik
 */
// this class is dedicated to have an efficient sorted set class storing
// values within 0..n-1 and fast sequencial iterator
final class Bitfield {

    private final long[] field;
    private final int length;
    private final int field_len;
    private int num_size;
    private int iter = 0;
    private int pos = 0;

    Bitfield(int _length) {
        length = _length;
        field_len = 1 + ((length + 1) >>> 6);
        field = new long[field_len];
        num_size = 0;
        for (int i = 0; i < field_len; ++i) {
            field[i] = 0L;
        }
    }

    void add(int pos) {
        if (!find(pos)) {
            field[pos >>> 6] |= (long) (1L << (pos & 63));
            ++num_size;
        }
    }

    boolean find(int pos) {
        return ((field[pos >>> 6] >>> (pos & 63)) & 1L) == 1L;
    }

    boolean isNotSet(int pos) {
        return ((field[pos >>> 6] >>> (pos & 63)) & 1L) == 0L;
    }

    static void printBitfield(long l) {
        for (int i = 63; i >= 0; --i) {
            System.out.print(((l >>> i) & 1L));
        }
        System.out.println();
    }

    void resetIterator() {
        iter = 0;
        pos = -1;
    }

    boolean hasNext() {
        return iter < num_size;
    }

    int next() {
        pos += 1;
        if (pos >= length) {
            throw new ConstraintViolationException("Bitfield out of range at pos=" + pos + " for length=" + length);
        }

        int field_pos = pos >>> 6;
        long field_bits = field[field_pos] & (long) (~((1L << (long) (pos & 63)) - 1L));

        do {
            if (field_bits != 0) {
                pos = (field_pos << 6) + (int) java.lang.Long.numberOfTrailingZeros(field_bits & (-field_bits));
                iter += 1;
                return pos;
            }
            if (++field_pos < field_len) {
                field_bits = field[field_pos];
            }
        } while (field_pos < field_len);

        throw new ConstraintViolationException("Bitfield out of range at pos=" + pos + " for field_len=" + field_len);
    }
}
