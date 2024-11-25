/*
 * Copyright (c) 2019, 2020, F(X)yz
 * Copyright (c) 2014, 2016, Oracle and/or its affiliates.
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
package com.bingbaihanji.javafxparse3dfile.importers.obj;

import com.bingbaihanji.javafxparse3dfile.importers.Importer;
import com.bingbaihanji.javafxparse3dfile.importers.MaterialData;
import com.bingbaihanji.javafxparse3dfile.importers.Model3D;
import com.bingbaihanji.javafxparse3dfile.importers.SmoothingGroups;
import com.bingbaihanji.javafxparse3dfile.importers.shapes.polygon.PolygonMesh;
import com.bingbaihanji.javafxparse3dfile.importers.shapes.polygon.PolygonMeshView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.collections.ObservableIntegerArray;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

/**
 * object loader
 */
public class ObjImporter implements Importer {

    private static final String SUPPORTED_EXT = "obj";

    private static boolean debug = false;
    private static float scale = 1;
    private static boolean flatXZ = false;

    static void log(String string) {
        if (debug) {
            System.out.println(string);
        }
    }

    public static void setFlatXZ(boolean flatXZ) {
        ObjImporter.flatXZ = flatXZ;
    }

    public static void setDebug(boolean debug) {
        ObjImporter.debug = debug;
    }

    public static void setScale(float scale) {
        ObjImporter.scale = scale;
    }

    @Override
    public Model3D load(URL url) throws IOException {
        return read(url, false);
    }

    @Override
    public Model3D loadAsPoly(URL url) throws IOException {
        return read(url, true);
    }

    @Override
    public boolean isSupported(String extension) {
        return SUPPORTED_EXT.equalsIgnoreCase(extension);
    }

    private ObjModel read(URL url, boolean asPolygon) throws IOException {
        log("Reading from URL: " + url + " as polygon: " + asPolygon);

        ObjModel model = asPolygon ? new PolyObjModel(url) : new ObjModel(url);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            reader.lines()
                    .map(String::trim)
                    .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                    .forEach(model::parseLine);
        }

        model.addMesh(model.key);

        log("Totally loaded " + (model.vertices.size() / 3.) + " vertices, "
                + (model.uvs.size() / 2.) + " uvs, "
                + (model.numFaces() / 6.) + " faces, "
                + model.smoothingGroups.size() + " smoothing groups.");

        model.loadComplete();

        return model;
    }

    private static class ObjModel extends Model3D {

        // obj format spec: http://paulbourke.net/dataformats/obj/
        /**
         * 定义了一个静态的解析器映射，用于将 OBJ 文件中不同的关键字映射到对应的解析逻辑。
         * 该映射的键是 OBJ 文件中行的关键字，值是一个 BiConsumer 对象，
         * 它接收当前行的内容和模型对象（ObjModel），并调用相应的解析方法处理数据。
         *
         * <p>主要解析的关键字及其功能说明：
         * <ul>
         *   <li>"g" - 组名称，用于定义当前几何体的逻辑组。</li>
         *   <li>"v " - 顶点坐标，定义几何体的顶点。</li>
         *   <li>"vt " - 纹理坐标，用于定义顶点在纹理上的映射位置。</li>
         *   <li>"f " - 面数据，描述几何体的多边形面。</li>
         *   <li>"s " - 平滑组，用于定义面之间是否共享平滑法线。</li>
         *   <li>"mtllib " - 材质库，指定一个材质库文件。</li>
         *   <li>"usemtl " - 使用材质，指定当前几何体使用的材质。</li>
         *   <li>"vn " - 法线向量，定义顶点或面的法线。</li>
         * </ul>
         *
         * <p>每个关键字对应的 BiConsumer 调用方法：
         * <ul>
         *   <li>参数 1：当前行的内容（去掉关键字部分的具体数据）。</li>
         *   <li>参数 2：OBJ 模型对象，用于存储解析结果。</li>
         * </ul>
         * <p>
         * 示例：
         * <pre>
         * PARSERS.get("v ").accept("1.0 2.0 3.0", objModel);
         * </pre>
         * 上述代码会调用 objModel 的 parseVertex 方法，解析顶点数据。
         */
        private static final Map<String, BiConsumer<String, ObjModel>> PARSERS = Map.of(
                "g", (l, m) -> m.parseGroupName(l),       // 解析组名称
                "v ", (l, m) -> m.parseVertex(l),             // 解析顶点坐标
                "vt ", (l, m) -> m.parseVertexTexture(l),     // 解析纹理坐标
                "f ", (l, m) -> m.parseFace(l),               // 解析面数据
                "s ", (l, m) -> m.parseSmoothGroup(l),        // 解析平滑组
                "mtllib ", (l, m) -> m.parseMaterialLib(l),   // 解析材质库
                "usemtl ", (l, m) -> m.parseUseMaterial(l),   // 解析材质使用
                "vn ", (l, m) -> m.parseVertexNormal(l)       // 解析法线向量
        );


        private void parseLine(String line) {
            for (Entry<String, BiConsumer<String, ObjModel>> parser : PARSERS.entrySet()) {
                String identifier = parser.getKey();
                if (line.startsWith(identifier)) {
                    parser.getValue().accept(line.substring(identifier.length()).trim(), this);
                    return;
                }
            }
            log("line skipped: " + line);
        }

        List<Map<String, Material>> materialLibrary = new ArrayList<>();

        ObservableFloatArray vertices = FXCollections.observableFloatArray();
        ObservableFloatArray uvs = FXCollections.observableFloatArray();
        ObservableFloatArray normals = FXCollections.observableFloatArray();
        ObservableIntegerArray smoothingGroups = FXCollections.observableIntegerArray();
        MaterialData materialData = new MaterialData("default", new PhongMaterial(Color.WHITE));
        Map<String, String> meshNamesToMaterialNames = new HashMap<>();

        int facesStart = 0;
        int facesNormalStart = 0;
        int smoothingGroupsStart = 0;
        int currentSmoothGroup = 0;
        String key = "default";

        List<String> meshNames = new ArrayList<>();

        // specific to single obj model
        private Map<String, TriangleMesh> meshes = new HashMap<>();
        private ObservableIntegerArray faces = FXCollections.observableIntegerArray();
        private ObservableIntegerArray faceNormals = FXCollections.observableIntegerArray();

        private final URL url;

        ObjModel(URL url) {
            this.url = url;
        }

        int numFaces() {
            return faces.size();
        }

        protected int vertexIndex(int vertexIndex) {
            return vertexIndex + (vertexIndex < 0 ? vertices.size() / 3 : -1);
        }

        protected int uvIndex(int uvIndex) {
            return uvIndex + (uvIndex < 0 ? uvs.size() / 2 : -1);
        }

        protected int normalIndex(int normalIndex) {
            return normalIndex + (normalIndex < 0 ? normals.size() / 3 : -1);
        }

        Node buildMeshView(String key) {
            MeshView meshView = new MeshView();
            meshView.setId(key);
            meshView.setMaterial(getMaterial(meshNamesToMaterialNames.get(key)));
            meshView.setMesh(meshes.get(key));
            meshView.setCullFace(CullFace.NONE);
            return meshView;
        }

        private void loadComplete() {
            meshNames.forEach(meshName -> addMeshView(meshName, buildMeshView(meshName)));
        }

        void addMesh(String key) {
            if (facesStart >= faces.size()) {
                // we're only interested in faces
                smoothingGroupsStart = smoothingGroups.size();
                return;
            }

            TriangleMesh mesh = new TriangleMesh();
            mesh.getPoints().ensureCapacity(vertices.size() / 2);
            mesh.getTexCoords().ensureCapacity(uvs.size() / 2);
            ObservableFloatArray newNormals = FXCollections.observableFloatArray();
            newNormals.ensureCapacity(normals.size() / 2);
            boolean useNormals = true;

            Map<Integer, Integer> vertexMap = new HashMap<>(vertices.size() / 2);
            Map<Integer, Integer> uvMap = new HashMap<>(uvs.size() / 2);
            Map<Integer, Integer> normalMap = new HashMap<>(normals.size() / 2);

            for (int i = facesStart; i < faces.size(); i += 2) {
                int vi = faces.get(i);
                Integer nvi = vertexMap.putIfAbsent(vi, mesh.getPoints().size() / 3);
                if (nvi == null) {
                    nvi = mesh.getPoints().size() / 3;
                    mesh.getPoints().addAll(vertices, vi * 3, 3);
                }
                faces.set(i, nvi);

                int uvi = faces.get(i + 1);
                Integer nuvi = uvMap.putIfAbsent(uvi, mesh.getTexCoords().size() / 2);
                if (nuvi == null) {
                    nuvi = mesh.getTexCoords().size() / 2;
                    if (uvi >= 0) {
                        mesh.getTexCoords().addAll(uvs, uvi * 2, 2);
                    } else {
                        mesh.getTexCoords().addAll(0, 0);
                    }
                }
                faces.set(i + 1, nuvi);

                if (useNormals) {
                    int ni = faceNormals.get(i / 2);
                    Integer nni = normalMap.putIfAbsent(ni, newNormals.size() / 3);
                    if (nni == null) {
                        nni = newNormals.size() / 3;
                        if (ni >= 0 && normals.size() >= (ni + 1) * 3) {
                            newNormals.addAll(normals, ni * 3, 3);
                        } else {
                            useNormals = false;
                            newNormals.addAll(0, 0, 0);
                        }
                    }
                    faceNormals.set(i / 2, nni);
                }
            }
            mesh.getFaces().setAll(faces, facesStart, faces.size() - facesStart);

            // Use normals if they are provided
            if (useNormals) {
                int[] facesArray = mesh.getFaces().toArray(new int[mesh.getFaces().size()]);

                int fnLength = faceNormals.size() - facesNormalStart;
                int[] faceNormalsArray = faceNormals.toArray(facesNormalStart, new int[fnLength], fnLength);

                float[] normalsArray = newNormals.toArray(new float[newNormals.size()]);

                int[] smGroups = SmoothingGroups.calcSmoothGroups(mesh, facesArray, faceNormalsArray, normalsArray);
                mesh.getFaceSmoothingGroups().setAll(smGroups);
            } else {
                int sgLength = smoothingGroups.size() - smoothingGroupsStart;
                mesh.getFaceSmoothingGroups().setAll(smoothingGroups, smoothingGroupsStart, sgLength);
            }

            int keyIndex = 2;
            String keyBase = key;
            while (meshes.get(key) != null) {
                key = keyBase + " (" + keyIndex++ + ")";
            }
            meshes.put(key, mesh);
            meshNames.add(key);

            meshNamesToMaterialNames.put(key, materialData.getName());
            addMaterial(materialData.getName(), materialData.getMaterial());

            log("Added mesh '" + key + "' of " + mesh.getPoints().size() / mesh.getPointElementSize() + " vertices, "
                    + mesh.getTexCoords().size() / mesh.getTexCoordElementSize() + " uvs, "
                    + mesh.getFaces().size() / mesh.getFaceElementSize() + " faces, "
                    + mesh.getFaceSmoothingGroups().size() + " smoothing groups.");
            log("material diffuse color = " + ((PhongMaterial) materialData.getMaterial()).getDiffuseColor());
            log("material diffuse map = " + ((PhongMaterial) materialData.getMaterial()).getDiffuseMap());

            facesStart = faces.size();
            facesNormalStart = faceNormals.size();
            smoothingGroupsStart = smoothingGroups.size();
        }

        private void parseGroupName(String value) {
            addMesh(key);
            key = value.isEmpty() ? "default" : value;
            log("key = " + key);
        }

//        private void parseVertex(String value) {
//            String[] split = value.split(" +");
//            float x = Float.parseFloat(split[0]) * scale;
//            float y = Float.parseFloat(split[1]) * scale;
//            float z = Float.parseFloat(split[2]) * scale;
//            vertices.addAll(x, y, z);
//
//            if (flatXZ) {
//                uvs.addAll(x, z);
//            }
//        }

        /**
         * 解析 OBJ 文件中的顶点 (vertex) 数据，并将其转换为程序所需的格式。
         * 顶点数据通常以 "v x y z" 的形式定义，其中 x、y、z 为顶点的坐标。
         * <p>
         * 此方法会对顶点坐标进行以下处理：
         * 1. 根据提供的比例 (scale) 缩放顶点坐标。
         * 2. 反转 Y 轴和 Z 轴的方向以适配当前渲染坐标系。
         * 3. 如果启用了 flatXZ 模式，将顶点的 X 和 Z 轴值存储为纹理坐标。
         *
         * @param value 包含顶点数据的字符串，例如 "v 1.0 2.0 3.0"。
         */
        private void parseVertex(String value) {
            // 按空格拆分顶点数据
            String[] split = value.split(" +");
            // 解析并缩放顶点坐标
            float x = Float.parseFloat(split[0]) * scale; // 顶点的 X 坐标
            float y = -Float.parseFloat(split[1]) * scale; // 顶点的 Y 坐标，反转方向
            float z = -Float.parseFloat(split[2]) * scale; // 顶点的 Z 坐标，反转方向

            // 将转换后的顶点坐标添加到顶点集合中
            vertices.addAll(x, y, z);

            // 如果启用了 flatXZ 模式，将顶点的 X 和 Z 值存储为纹理坐标
            if (flatXZ) {
                uvs.addAll(x, z);
            }
        }


//        private void parseVertexTexture(String value) {
//            String[] split = value.split(" +");
//            float u = split[0].trim().equalsIgnoreCase("nan") ? Float.NaN : Float.parseFloat(split[0]);
//            float v = split[1].trim().equalsIgnoreCase("nan") ? Float.NaN : Float.parseFloat(split[1]);
//            uvs.addAll(u, 1 - v);
//        }

        /**
         * 解析 OBJ 文件中的纹理坐标 (vertex texture) 数据，并将其转换为程序所需的格式。
         * 纹理坐标通常以 "vt u v" 的形式定义，其中 u、v 为纹理映射的坐标。
         * <p>
         * 此方法会对纹理坐标进行以下处理：
         * 1. 如果纹理坐标值为 "NaN"，则将其设置为 Float.NaN。
         * 2. 反转 V 坐标的方向，以适配当前的纹理坐标系。
         * 3. 将解析后的纹理坐标存储到 uvs 集合中。
         *
         * @param value 包含纹理坐标数据的字符串，例如 "vt 0.5 0.5"。
         */
        private void parseVertexTexture(String value) {
            // 按空格拆分纹理坐标数据
            String[] split = value.split(" +");

            // 解析 U 坐标，若为 "NaN" 则设为 Float.NaN
            float u = split[0].trim().equalsIgnoreCase("nan") ? Float.NaN : Float.parseFloat(split[0]);

            // 解析 V 坐标并反转其方向，若为 "NaN" 则设为 Float.NaN
            float v = split[1].trim().equalsIgnoreCase("nan") ? Float.NaN : 1 - Float.parseFloat(split[1]);

            // 将解析后的纹理坐标添加到 UV 集合中
            uvs.addAll(u, v);
        }


//        protected void parseFace(String value) {
//            String[] split = value.split(" +");
//            int[][] data = new int[split.length][];
//            boolean uvProvided = true;
//            boolean normalProvided = true;
//            for (int i = 0; i < split.length; i++) {
//                String[] split2 = split[i].split("/");
//                if (split2.length < 2) {
//                    uvProvided = false;
//                    normalProvided = false;
//                } else if (split2.length < 3) {
//                    normalProvided = false;
//                }
//                data[i] = new int[split2.length];
//                for (int j = 0; j < split2.length; j++) {
//                    if (split2[j].length() == 0) {
//                        data[i][j] = 0;
//                        if (j == 1) {
//                            uvProvided = false;
//                        } else if (j == 2) {
//                            normalProvided = false;
//                        }
//                    } else {
//                        data[i][j] = Integer.parseInt(split2[j]);
//                    }
//                }
//            }
//            int v1 = vertexIndex(data[0][0]);
//            int uv1 = -1;
//            int n1 = -1;
//            if (uvProvided && !flatXZ) {
//                uv1 = uvIndex(data[0][1]);
//                if (uv1 < 0) {
//                    uvProvided = false;
//                }
//            }
//            if (normalProvided) {
//                n1 = normalIndex(data[0][2]);
//                if (n1 < 0) {
//                    normalProvided = false;
//                }
//            }
//            for (int i = 1; i < data.length - 1; i++) {
//                int v2 = vertexIndex(data[i][0]);
//                int v3 = vertexIndex(data[i + 1][0]);
//                int uv2 = -1;
//                int uv3 = -1;
//                int n2 = -1;
//                int n3 = -1;
//                if (uvProvided && !flatXZ) {
//                    uv2 = uvIndex(data[i][1]);
//                    uv3 = uvIndex(data[i + 1][1]);
//                }
//                if (normalProvided) {
//                    n2 = normalIndex(data[i][2]);
//                    n3 = normalIndex(data[i + 1][2]);
//                }
//
//                // log("v1 = " + v1 + ", v2 = " + v2 + ", v3 = " + v3);
//                // log("uv1 = " + uv1 + ", uv2 = " + uv2 + ", uv3 = " + uv3);
//
//                faces.addAll(v1, uv1, v2, uv2, v3, uv3);
//                faceNormals.addAll(n1, n2, n3);
//
//                smoothingGroups.addAll(currentSmoothGroup);
//            }
//        }

        /**
         * 解析 OBJ 文件中的面（face）数据，并存储顶点、纹理坐标和法线索引。
         * 面数据通常以 "f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 ..." 的形式定义，其中：
         * - v 表示顶点索引。
         * - vt 表示纹理坐标索引。
         * - vn 表示法线索引。
         * <p>
         * 此方法支持以下情况：
         * 1. 仅有顶点索引，例如 "f v1 v2 v3"。
         * 2. 顶点索引和纹理坐标索引，例如 "f v1/vt1 v2/vt2 v3/vt3"。
         * 3. 顶点索引、纹理坐标索引和法线索引，例如 "f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3"。
         * 4. 不完整的面数据，例如 "f v1//vn1 v2//vn2 v3//vn3"。
         *
         * @param value 包含面数据的字符串，例如 "f 1/1/1 2/2/2 3/3/3"。
         */
        protected void parseFace(String value) {
            // 按空格拆分面数据
            String[] split = value.split(" +");
            int[][] data = new int[split.length][]; // 存储每个顶点的索引数组
            boolean uvProvided = true; // 是否提供了纹理坐标
            boolean normalProvided = true; // 是否提供了法线

            // 解析每个顶点的索引
            for (int i = 0; i < split.length; i++) {
                String[] split2 = split[i].split("/"); // 按 '/' 拆分顶点数据
                if (split2.length < 2) {
                    uvProvided = false; // 未提供纹理坐标
                    normalProvided = false; // 未提供法线
                } else if (split2.length < 3) {
                    normalProvided = false; // 未提供法线
                }
                data[i] = new int[split2.length];
                for (int j = 0; j < split2.length; j++) {
                    if (split2[j].length() == 0) {
                        data[i][j] = 0; // 缺失值设置为 0
                        if (j == 1) {
                            uvProvided = false; // 缺少纹理坐标
                        } else if (j == 2) {
                            normalProvided = false; // 缺少法线
                        }
                    } else {
                        data[i][j] = Integer.parseInt(split2[j]); // 转换为整数索引
                    }
                }
            }

            // 获取第一个顶点的索引
            int v1 = vertexIndex(data[0][0]);
            int uv1 = -1;
            int n1 = -1;
            if (uvProvided && !flatXZ) {
                uv1 = uvIndex(data[0][1]); // 获取纹理坐标索引
                if (uv1 < 0) {
                    uvProvided = false; // 纹理坐标无效
                }
            }
            if (normalProvided) {
                n1 = normalIndex(data[0][2]); // 获取法线索引
                if (n1 < 0) {
                    normalProvided = false; // 法线无效
                }
            }

            // 生成三角形面数据
            for (int i = 1; i < data.length - 1; i++) {
                int v2 = vertexIndex(data[i][0]);
                int v3 = vertexIndex(data[i + 1][0]);
                int uv2 = -1;
                int uv3 = -1;
                int n2 = -1;
                int n3 = -1;

                if (uvProvided && !flatXZ) {
                    uv2 = uvIndex(data[i][1]); // 获取纹理坐标索引
                    uv3 = uvIndex(data[i + 1][1]);
                }
                if (normalProvided) {
                    n2 = normalIndex(data[i][2]); // 获取法线索引
                    n3 = normalIndex(data[i + 1][2]);
                }

                // 添加面数据到对应的集合
                faces.addAll(v1, uv1, v2, uv2, v3, uv3); // 顶点索引和纹理坐标索引
                faceNormals.addAll(n1, n2, n3); // 法线索引
                smoothingGroups.addAll(currentSmoothGroup); // 平滑组
            }
        }


        private void parseSmoothGroup(String value) {
            currentSmoothGroup = value.equals("off") ? 0 : Integer.parseInt(value);
        }

        /**
         * 解析 OBJ 文件中的材质库声明。
         *
         * <p>OBJ 文件中的 "mtllib" 关键字用于指定一个或多个材质库文件。
         * 此方法解析关键字后面的材质库文件名，并通过 MtlReader 加载这些文件。
         * 加载的材质会存储到 `materialLibrary` 列表中。
         *
         * @param value 包含材质库文件名的字符串，文件名之间以空格分隔。
         *              例如："mtllib materials1.mtl materials2.mtl" 中的 "materials1.mtl materials2.mtl"。
         *
         *              <p>主要流程：
         *              <ol>
         *                <li>使用空格分隔 `value` 参数，将所有文件名提取到数组中。</li>
         *                <li>对每个文件名创建一个 {@code MtlReader} 实例，并提供文件名和资源的 URL。</li>
         *                <li>通过 {@code MtlReader#getMaterials()} 方法获取材质集合，并将其添加到 `materialLibrary`。</li>
         *              </ol>
         *
         *              <p>注意：
         *              <ul>
         *                <li>如果材质文件未找到或读取失败，可能会抛出异常，需考虑异常处理。</li>
         *                <li>确保文件路径和 URL 的有效性，否则可能会导致加载错误。</li>
         *              </ul>
         */
        private void parseMaterialLib(String value) {
            // setting materials lib 设置材质库
            String[] split = value.split(" +"); // 按空格分割材质库文件名
            for (String filename : split) {
                // 使用 MtlReader 加载材质库
                MtlReader mtlReader = new MtlReader(filename, url.toExternalForm());
                // 将材质集合添加到 materialLibrary
                materialLibrary.add(mtlReader.getMaterials());
            }
        }

        /**
         * 解析 usemtl 语句，设置当前网格的材质。
         *
         * @param value 材质名称。
         */
        private void parseUseMaterial(String value) {
            // 添加当前网格的 key
            addMesh(key);

            // 设置新的材质用于下一个网格
            String materialName = value;
            boolean materialFound = false;

            // 遍历材质库，查找指定的材质
            for (Map<String, Material> mm : materialLibrary) {
                Material m = mm.get(materialName);
                if (m != null) {
                    // 找到材质，设置材质数据
                    materialData = new MaterialData(materialName, m);
                    materialFound = true;
                    break;
                }
            }

            // 如果没有找到指定的材质，输出警告信息
            if (!materialFound) {
                System.err.println("警告: 找不到材质 '" + materialName + "'，使用默认材质。");
            }
        }

        private void parseVertexNormal(String value) {
            String[] split = value.split(" +");
            float x = Float.parseFloat(split[0]);
            float y = Float.parseFloat(split[1]);
            float z = Float.parseFloat(split[2]);
            normals.addAll(x, y, z);
        }
    }

    /**
     * @MethodName: a
     * @Author 冰白寒祭
     * @Description:PolyObjModel 主要用于解析 .obj 文件并将其转换为 JavaFX 中可显示的多边形网格数据。
     * 它处理的关键数据包括顶点、纹理坐标、法线、面的连接关系等。通过这些数据，
     * 类可以构建 PolygonMeshView 来展示 3D 模型。
     * @Date: 2024-11-18 16:20:56
     */

    private static class PolyObjModel extends ObjModel {

        // specific to poly obj model
        private Map<String, PolygonMesh> polygonMeshes = new HashMap<>();
        private List<int[]> facesPolygon = new ArrayList<>();
        private List<int[]> faceNormalsPolygon = new ArrayList<>();

        PolyObjModel(URL url) {
            super(url);
        }

        @Override
        int numFaces() {
            return facesPolygon.size();
        }

        @Override
        Node buildMeshView(String key) {
            PolygonMeshView polygonMeshView = new PolygonMeshView();
            polygonMeshView.setId(key);
            polygonMeshView.setMaterial(getMaterial(meshNamesToMaterialNames.get(key)));
            polygonMeshView.setMesh(polygonMeshes.get(key));
            // TODO:
            // polygonMeshView.setCullFace(CullFace.NONE);
            return polygonMeshView;
        }

        @Override
        void addMesh(String key) {
            if (facesStart >= facesPolygon.size()) {
                // we're only interested in faces
                smoothingGroupsStart = smoothingGroups.size();
                return;
            }

            PolygonMesh mesh = new PolygonMesh();
            mesh.getPoints().ensureCapacity(vertices.size() / 2);
            mesh.getTexCoords().ensureCapacity(uvs.size() / 2);
            ObservableFloatArray newNormals = FXCollections.observableFloatArray();
            newNormals.ensureCapacity(normals.size() / 2);
            boolean useNormals = true;

            Map<Integer, Integer> vertexMap = new HashMap<>(vertices.size() / 2);
            Map<Integer, Integer> uvMap = new HashMap<>(uvs.size() / 2);
            Map<Integer, Integer> normalMap = new HashMap<>(normals.size() / 2);

            mesh.setFaces(new int[facesPolygon.size() - facesStart][]);
            int[][] faceNormalArrays = new int[faceNormalsPolygon.size() - facesNormalStart][];

            for (int i = facesStart; i < facesPolygon.size(); i++) {
                int[] faceIndexes = facesPolygon.get(i);
                int[] faceNormalIndexes = faceNormalsPolygon.get(i);
                for (int j = 0; j < faceIndexes.length; j += 2) {
                    int vi = faceIndexes[j];
                    Integer nvi = vertexMap.putIfAbsent(vi, mesh.getPoints().size() / 3);
                    if (nvi == null) {
                        nvi = mesh.getPoints().size() / 3;
                        mesh.getPoints().addAll(vertices, vi * 3, 3);
                    }
                    faceIndexes[j] = nvi;
//                  faces.set(i, nvi);

                    int uvi = faceIndexes[j + 1];
                    Integer nuvi = uvMap.putIfAbsent(uvi, mesh.getTexCoords().size() / 2);
                    if (nuvi == null) {
                        nuvi = mesh.getTexCoords().size() / 2;
                        if (uvi >= 0) {
                            mesh.getTexCoords().addAll(uvs, uvi * 2, 2);
                        } else {
                            mesh.getTexCoords().addAll(0, 0);
                        }
                    }
                    faceIndexes[j + 1] = nuvi;
//                  faces.set(i + 1, nuvi);

                    int ni = faceNormalIndexes[j / 2];
                    Integer nni = normalMap.putIfAbsent(ni, newNormals.size() / 3);
                    if (nni == null) {
                        nni = newNormals.size() / 3;
                        if (ni >= 0 && normals.size() >= (ni + 1) * 3) {
                            newNormals.addAll(normals, ni * 3, 3);
                        } else {
                            useNormals = false;
                            newNormals.addAll(0, 0, 0);
                        }
                    }
                    faceNormalIndexes[j / 2] = nni;
                }
                mesh.getFaces()[i - facesStart] = faceIndexes;
                faceNormalArrays[i - facesNormalStart] = faceNormalIndexes;
            }

            // Use normals if they are provided
            if (useNormals) {
                float[] normalsArray = newNormals.toArray(new float[newNormals.size()]);
                int[] smGroups = SmoothingGroups.calcSmoothGroups(mesh.getFaces(), faceNormalArrays, normalsArray);
                mesh.getFaceSmoothingGroups().setAll(smGroups);
            } else {
                int length = smoothingGroups.size() - smoothingGroupsStart;
                mesh.getFaceSmoothingGroups().setAll(smoothingGroups, smoothingGroupsStart, length);
            }

            if (debug) {
                System.out.println("mesh.points = " + mesh.getPoints());
                System.out.println("mesh.texCoords = " + mesh.getTexCoords());
                System.out.println("mesh.faces: ");
                for (int[] face : mesh.getFaces()) {
                    System.out.println("    face:: " + Arrays.toString(face));
                }
            }

            int keyIndex = 2;
            String keyBase = key;
            while (polygonMeshes.get(key) != null) {
                key = keyBase + " (" + keyIndex++ + ")";
            }
            polygonMeshes.put(key, mesh);
            meshNames.add(key);

            meshNamesToMaterialNames.put(key, materialData.getName());
            addMaterial(materialData.getName(), materialData.getMaterial());

            log("Added mesh '" + key + "' of " + (mesh.getPoints().size() / 3) + " vertices, "
                    + (mesh.getTexCoords().size() / 2) + " uvs, "
                    + mesh.getFaces().length + " faces, "
                    + 0 + " smoothing groups.");
            log("material diffuse color = " + ((PhongMaterial) materialData.getMaterial()).getDiffuseColor());
            log("material diffuse map = " + ((PhongMaterial) materialData.getMaterial()).getDiffuseMap());

            facesStart = facesPolygon.size();
            facesNormalStart = faceNormalsPolygon.size();
            smoothingGroupsStart = smoothingGroups.size();
        }

        @Override
        protected void parseFace(String value) {
            String[] split = value.split(" +");
            int[] faceIndexes = new int[split.length * 2];
            int[] faceNormalIndexes = new int[split.length];
            for (int i = 0; i < split.length; i++) {
                String[] split2 = split[i].split("/");
                faceIndexes[i * 2] = vertexIndex(Integer.parseInt(split2[0]));
                faceIndexes[(i * 2) + 1] = (split2.length > 1 && split2[1].length() > 0) ? uvIndex(Integer.parseInt(split2[1])) : -1;
                faceNormalIndexes[i] = (split2.length > 2 && split2[2].length() > 0) ? normalIndex(Integer.parseInt(split2[2])) : -1;
            }

            facesPolygon.add(faceIndexes);
            faceNormalsPolygon.add(faceNormalIndexes);
            smoothingGroups.addAll(currentSmoothGroup);
        }
    }
}
