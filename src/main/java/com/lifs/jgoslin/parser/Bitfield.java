/*
 * Copyright 2021 dominik.
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
package com.lifs.jgoslin.parser;

import java.util.Iterator;
import com.lifs.jgoslin.domain.LipidParsingException;

/**
 *
 * @author dominik
 */
// this class is dedicated to have an efficient sorted set class storing
// values within 0..n-1 and fast sequencial iterator
public class Bitfield {
    public long[] field;
    public int length;
    public int field_len;
    public int num_size;
    public int iter = 0;
    public int pos = 0;

    public Bitfield(int _length){
        length = _length;
        field_len = 1 + ((length + 1) >>> 6);
        field = new long[field_len];
        num_size = 0;
        for (int i = 0; i < field_len; ++i) field[i] = 0L;
    }

    public void add(int pos)
    {
        if (!find(pos)){
            field[pos >>> 6] |= (long)(1L << (pos & 63));
            ++num_size;
        }
    }


    public boolean find(int pos)
    {
        return ((field[pos >>> 6] >>> (pos & 63)) & 1L) == 1L;
    }


    public boolean isNotSet(int pos)
    {
        return ((field[pos >>> 6] >>> (pos & 63)) & 1L) == 0L;
    }
    
    public static void print_bitfield(long l){
        for (int i = 63; i >= 0; --i){
            System.out.print(((l >>> i) & 1L));
        } System.out.println();
    }
    
    
    public void reset_iterator(){
        iter = 0;
        pos = -1;
    }
    
    
    public boolean has_next(){
        return iter < num_size;
    }


    public int next(){
        pos += 1;
        if (pos >= length) throw new RuntimeException("Bitfield out of range");

        int field_pos = pos >>> 6;
        long field_bits = field[field_pos] & (long)(~((1L << (long)(pos & 63)) - 1L));

        do {
            if (field_bits != 0){
                pos = (field_pos << 6) + (int)java.lang.Long.numberOfTrailingZeros(field_bits & (-field_bits));
                iter += 1;
                return pos;
            }
            if (++field_pos < field_len) field_bits = field[field_pos];
        } while (field_pos < field_len);

        throw new RuntimeException("Bitfield out of range");
    }
}