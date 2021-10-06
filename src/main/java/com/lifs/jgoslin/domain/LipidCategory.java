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
package com.lifs.jgoslin.domain;

/**
 *
 * @author dominik
 */
public enum LipidCategory {
    NO_CATEGORY,
    UNDEFINED,
    GL, // SLM:000117142 Glycerolipids
    GP, // SLM:000001193 Glycerophospholipids
    SP, // SLM:000000525 Sphingolipids
    ST, // SLM:000500463 Steroids and derivatives
    FA, // SLM:000390054 Fatty acyls and derivatives
    PK, // polyketides
    SL // Saccharo lipids
}
