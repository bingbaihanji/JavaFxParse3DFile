/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.bingbaihanji.javafxparse3dfile.importers.maya.values.impl;

import com.bingbaihanji.javafxparse3dfile.importers.maya.types.MFloat3Type;
import com.bingbaihanji.javafxparse3dfile.importers.maya.values.MData;
import com.bingbaihanji.javafxparse3dfile.importers.maya.values.MFloat;
import com.bingbaihanji.javafxparse3dfile.importers.maya.values.MFloat3;

import java.util.Iterator;

public class MFloat3Impl extends MDataImpl implements MFloat3 {

    private final float[] data = new float[3];

    class MFloat3Component extends MDataImpl implements MFloat {
        private final int index;

        MFloat3Component(int index) {
            super(MFloat3Impl.this.getEnv().findDataType("float"));
            this.index = index;
        }

        @Override
        public void set(float value) {
            data[index] = value;
        }

        @Override
        public float get() {
            return data[index];
        }

        @Override
        public void parse(Iterator<String> elements) {
            data[index] = Float.parseFloat(elements.next());
        }
    }

    public MFloat3Impl(MFloat3Type type) {
        super(type);
    }

    @Override
    public void set(float x, float y, float z) {
        data[0] = x; data[1] = y; data[2] = z;
    }

    @Override
    public float[] get() {
        return data;
    }

    @Override
    public float getX() {
        return data[0];
    }

    @Override
    public float getY() {
        return data[1];
    }

    @Override
    public float getZ() {
        return data[2];
    }

    @Override
    public float get(int index) {
        return data[index];
    }

    @Override
    public void parse(Iterator<String> elements) {
        for (int i = 0; i < 3; i++) {
            data[i] = Float.parseFloat(elements.next());
        }
    }

    @Override
    public MData getData(int index) {
        return new MFloat3Component(index);
    }

    @Override
    public MData getData(String name) {
        if (name.equals("x")) {
            return getData(0);
        } else if (name.equals("y")) {
            return getData(1);
        } else if (name.equals("z")) {
            return getData(2);
        }
        return super.getData(name);
    }
}
