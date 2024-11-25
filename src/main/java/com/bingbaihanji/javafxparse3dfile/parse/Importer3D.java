package com.bingbaihanji.javafxparse3dfile.parse;

import com.bingbaihanji.javafxparse3dfile.importers.Importer;
import com.bingbaihanji.javafxparse3dfile.importers.Model3D;
import com.bingbaihanji.javafxparse3dfile.importers.fxml.FXMLImporter;
import com.bingbaihanji.javafxparse3dfile.importers.maya.MayaImporter;
import com.bingbaihanji.javafxparse3dfile.importers.obj.ObjImporter;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.MeshView;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * 3d模型导入工具
 */
public final class Importer3D {

    /**
     * 导入3d模型文件
     *
     * @param fileUrl
     */

    public static Group load(final String fileUrl) throws IOException {
        final int dot = fileUrl.lastIndexOf('.');
        if (dot <= 0) {
            throw new IOException("未知的 3D 文件格式, url [" + fileUrl + "] 拓展名缺失");
        }
        final String extension = fileUrl.substring(dot + 1).toLowerCase();
        switch (extension) {
            case "obj" -> {
                Importer importer = new ObjImporter();
                return getGroup(fileUrl, importer);
            }
            case "fxml" -> {
                Importer importer = new FXMLImporter();
                return getGroup(fileUrl, importer);
            }
            case "ma" -> {
                Importer importer = new MayaImporter();
                return getGroup(fileUrl, importer);
            }
        }

        throw new IOException("Unsupported 3D file format [" + extension + "]");
    }


    private static Group getGroup(String fileUrl, Importer importer) throws IOException {
        URL url = new File(fileUrl).toURI().toURL();
        System.out.println(url);
        Model3D load = importer.load(url);
        Group root = new Group();
        for (Node n : load.getMeshViews()) {
            if (n instanceof MeshView) {
                root.getChildren().add(n);
            }
        }
        return root;
    }
}

