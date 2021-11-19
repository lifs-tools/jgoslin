/*
 * Copyright 2021 Dominik Kopczynski, Nils Hoffmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lifstools.jgoslin.parser;

import org.lifstools.jgoslin.domain.ConstraintViolationException;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
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
