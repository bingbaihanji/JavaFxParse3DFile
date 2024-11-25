/*
 * Copyright (c) 2019 F(X)yz
 * Copyright (c) 2014, Oracle and/or its affiliates.
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
 * 版权所有 (c) 2019 F(X)yz
 * 版权所有 (c) 2014，Oracle及/或其附属公司。
 * 保留所有权利。使用须遵守许可协议条款。
 *
 * 本文件可以根据以下许可协议使用和分发：
 *
 * 允许以源代码和二进制形式进行再分发和使用，无论是否修改，只要满足以下条件：
 *
 *  - 源代码的再分发必须保留上述版权声明、本条件列表和以下免责声明。
 *  - 二进制形式的再分发必须在分发的文档和/或其他材料中复制上述版权声明、本条件列表和以下免责声明。
 *  - 未经Oracle公司或其贡献者的特定事先书面许可，不得将Oracle公司或其贡献者的名称用于推广或支持基于此软件的产品。
 *
 * 本软件由版权持有者和贡献者按“原样”提供，且明确否认任何明示或暗示的保证，包括但不限于对适销性和特定用途适用性的暗示保证。在任何情况下，版权持有者或贡献者均不对任何直接、间接、附带、特殊、示范性或后果性损害（包括但不限于替代商品或服务的采购；使用、数据或利润的丧失；或业务中断）承担责任，无论是合同、严格责任还是侵权（包括过失或其他原因）理论下引起的，甚至在已被告知可能发生此类损害的情况下。
 */
package com.bingbaihanji.javafxparse3dfile.importers;

import java.io.IOException;
import java.net.URL;

public interface Importer {

    /**
     * 加载3D文件
     *
     * @param url 需要加载的3D文件的URL
     * @throws IOException 如果加载文件时出现问题
     * @return 加载的3D模型
     */
    Model3D load(URL url) throws IOException;

    /**
     * 将3D文件加载为多边形网格。
     *
     * @param url 需要加载的3D文件的URL
     * @throws IOException 如果加载文件时出现问题
     * @return 加载的3D多边形模型
     */
    Model3D loadAsPoly(URL url) throws IOException;

    /**
     * 测试给定的3D文件扩展名是否受支持（例如“ma”，“ase”，“obj”，“fxml”，“dae”）。
     *
     * @param supportType 文件扩展名（例如“ma”，“ase”，“obj”，“fxml”，“dae”）
     * @return 如果扩展名属于受支持类型，则返回true；否则返回false。
     */
    boolean isSupported(String supportType);
}
