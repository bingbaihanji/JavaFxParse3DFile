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

import com.bingbaihanji.javafxparse3dfile.importers.maya.MNode;
import com.bingbaihanji.javafxparse3dfile.importers.maya.MPath;
import com.bingbaihanji.javafxparse3dfile.importers.maya.types.MPointerType;
import com.bingbaihanji.javafxparse3dfile.importers.maya.values.MData;
import com.bingbaihanji.javafxparse3dfile.importers.maya.values.MPointer;

import java.util.Iterator;

public class MPointerImpl extends MDataImpl implements MPointer {

    private MPath target;

    public MPointerImpl(MPointerType type) {
        super(type);
    }

    @Override
    public void setTarget(MPath path) {
        target = path;
    }

    @Override
    public MPath getTarget() {
        return target;
    }

    public void set(MData data) {
        //targetNode.setAttr(targetAttribute, data);
    }

    public MData get() {
        return target.apply();
    }

    @Override
    public void parse(Iterator<String> iter) {
        // Nothing
    }

    @Override
    public String toString() {
        if (target != null) {
            return target.toString();
        } else {
            return "Null Pointer";
        }
    }

    @Override
    public MNode getTargetNode() {
        return target.getTargetNode();
    }
}
