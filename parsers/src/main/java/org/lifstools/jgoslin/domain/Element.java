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
package org.lifstools.jgoslin.domain;

/**
 * Enumeration for typical chemical elements in lipids. Also supports heavy
 * variants, e.g. C13 as the heavy C13 isotope.
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public enum Element {
    C, C13, H, H2, N, N15, O, O17, O18, P, P32, S, S34, S33, F, Cl, Br, I, As
};
