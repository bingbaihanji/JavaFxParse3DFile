package com.bingbaihanji.javafxparse3dfile.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author 冰白寒祭
 * @date 2024-11-08 07:55:46
 * @description 控制Group中的3D模型进行 放大缩小旋转等
 */

public class ModelView {
    private static final Logger log = LoggerFactory.getLogger(ModelView.class);
    private static final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private static final DoubleProperty angleY = new SimpleDoubleProperty(0);
    private double anchorX, anchorY; // 上次鼠标位置
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    // 最远距离
//    private final double maximumDistance = 50.0;
//    // 最近距离
//    private final double closestDistance = -25;

    private double closestDistance;
    private double maximumDistance;

    boolean isLightOn = false;  // 记录灯光状态
    boolean isDrawModeIsLINE = false;  // 记录是否是线框模式


    private boolean isRightMousePressed = false;  // 标记右键是否按下

    public ModelView() {
    }

    public ModelView(double closestDistance, double maximumDistance) {
        this.closestDistance = closestDistance;
        this.maximumDistance = maximumDistance;
    }

    /**
     * 配置摄像机
     */
    public Camera setupCamera() {
        // 如果为 true：相机会固定在 (0,0,0) 点，即摄像机的眼睛位置会被固定在原点
        // 如果为 false：相机的眼睛位置可以移动（即相机可以在 3D 空间中移动）
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setFarClip(1000);
        camera.translateZProperty().set(-20);
        return camera;
    }

    /**
     * 初始化鼠标控制功能，用于实现3D模型的旋转和缩放。
     *
     * @param group 要操作的SmartGroup对象，包含3D模型的节点
     * @param scene 当前的Scene对象，用于监听鼠标事件
     * @param stage 当前的Stage对象，用于监听滚轮事件
     */
    public void initMouseControl(SmartGroup group, Scene scene, Stage stage) {
        // 创建沿X轴和Y轴旋转的Rotate对象，初始角度为0
        Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);

        // 将旋转对象添加到group的变换集合中，使group对象的旋转受其控制
        group.getTransforms().addAll(xRotate, yRotate);

        // 将xRotate的角度属性绑定到angleX变量，使得旋转角度与angleX同步
        xRotate.angleProperty().bind(angleX);

        // 将yRotate的角度属性绑定到angleY变量，使得旋转角度与angleY同步
        yRotate.angleProperty().bind(angleY);

        // 设置鼠标按下事件处理，记录初始的鼠标位置和旋转角度
        scene.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                anchorX = event.getSceneX();  // 鼠标按下时的X坐标
                anchorY = event.getSceneY();  // 鼠标按下时的Y坐标
                anchorAngleX = angleX.get();  // 当前的X轴旋转角度
                anchorAngleY = angleY.get();  // 当前的Y轴旋转角度
            } else if (event.getButton() == MouseButton.SECONDARY) { // 右键按下事件处理
                isRightMousePressed = true;
                anchorX = event.getSceneX();
                anchorY = event.getSceneY();
            }
        });

        // 设置鼠标拖动事件处理，根据鼠标移动计算新的旋转角度
        scene.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // 鼠标Y轴移动量用于更新X轴旋转角度，向上拖动会增加X轴旋转角度
                angleX.set(anchorAngleX - (anchorY - event.getSceneY()) * 0.3);

                // 鼠标X轴移动量用于更新Y轴旋转角度，向右拖动会增加Y轴旋转角度
                angleY.set(anchorAngleY + (anchorX - event.getSceneX()) * 0.3);
            } else if (isRightMousePressed) {
                double deltaX = event.getSceneX() - anchorX;
                double deltaY = event.getSceneY() - anchorY;

                // 平移模型
                group.translateXProperty().set(group.getTranslateX() + deltaX * 0.003);
                group.translateYProperty().set(group.getTranslateY() + deltaY * 0.003);

                anchorX = event.getSceneX();
                anchorY = event.getSceneY();
            }
        });

        // 右键松开事件处理
        scene.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                isRightMousePressed = false;
            }
        });

        // 设置滚轮事件处理，用于缩放模型
        // 设置滚轮事件处理，用于缩放模型
        stage.addEventHandler(ScrollEvent.SCROLL, event -> {
            // 优化版本
            // 每次滚轮滚动缩放的增量
            double delta = event.getDeltaY() > 0 ? 0.5 : -0.5;
            // 计算新的Z轴位置
            double newDistance = group.getTranslateZ() + delta;
            // 限制Z轴的平移范围，避免模型过小或过大
            newDistance = Math.max(closestDistance, Math.min(maximumDistance, newDistance));
            // 更新group的Z轴平移属性
            group.translateZProperty().set(newDistance);

        });
    }


    /**
     * 设置键盘控制，用于3D模型的平移和旋转
     */
    public void setupKeyboardControls(Scene scene, SmartGroup group) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (!event.isShiftDown()) { // 检测是否按下 Shift 键
                switch (event.getCode()) { // 没有按下
                    case W -> {
                        // 向上平移 3D 模型
                        group.translateYProperty().set(group.getTranslateY() - 0.03);
                    }
                    case S -> {
                        // 向下平移 3D 模型
                        group.translateYProperty().set(group.getTranslateY() + 0.03);
                    }
                    case A -> {
                        // 向左平移 3D 模型
                        group.translateXProperty().set(group.getTranslateX() - 0.03);
                    }
                    case D -> {
                        // 向右平移 3D 模型
                        group.translateXProperty().set(group.getTranslateX() + 0.03);
                    }
                }
            } else {
                switch (event.getCode()) { // 按下Shift 键
                    case W -> {
                        // 避免模型太大看不见
                        if (group.getTranslateZ() < closestDistance) {
                            group.translateZProperty().set(closestDistance);
                        } else {
                            group.translateZProperty().set(group.getTranslateZ() - 0.3);
                        }
                    }
                    case S -> {
                        // 避免模型太小看不见
                        if (group.getTranslateZ() > maximumDistance) {
                            group.translateZProperty().set(maximumDistance);
                        } else {
                            group.translateZProperty().set(group.getTranslateZ() + 0.3);
                        }
                    }
                }
            }
            switch (event.getCode()) {
                case DOWN -> group.rotateByX(10);
                case UP -> group.rotateByX(-10);
                case RIGHT -> group.rotateByY(-10);
                case LEFT -> group.rotateByY(10);
            }
        });
    }

    /**
     * @MethodName: setDrawMode
     * @Author 冰白寒祭
     * @Description: 遍历SmartGroup 将里边的3D模型切换成线框或者面
     * @Date: 2024-11-12 08:49:40
     */
    public void setDrawMode(SmartGroup modelGroup) {
        modelGroup.getChildren().forEach(node -> {
            if (node instanceof Group group) {
                ObservableList<Node> mesh = group.getChildren();
                if (isDrawModeIsLINE) {
                    mesh.forEach(meshNode -> {
                        if (meshNode instanceof MeshView meshView) {
                            meshView.setDrawMode(DrawMode.FILL);
                        }
                    });
                    isDrawModeIsLINE = !isDrawModeIsLINE;
                    log.info("面模式");
                } else {
                    mesh.forEach(meshNode -> {
                        if (meshNode instanceof MeshView meshView) {
                            meshView.setDrawMode(DrawMode.LINE);
                        }
                    });
                    isDrawModeIsLINE = !isDrawModeIsLINE;
                    log.info("线框模式");
                }
            }
        });
    }

    /**
     * @MethodName: setCullFace
     * @Author 冰白寒祭
     * @Description: 设置3D模型剔除模式
     * @Date: 2024-11-12 09:06:51
     */
    public void setCullFace(CullFace cullFace, SmartGroup modelGroup) {
        modelGroup.getChildren().forEach(node -> {
            if (node instanceof Group group) {
                ObservableList<Node> mesh = group.getChildren();
                mesh.forEach(meshNode -> {

                    if (meshNode instanceof MeshView meshView) {
                        meshView.setCullFace(cullFace);
                    }
                });
                isDrawModeIsLINE = true;
                log.info("剔除模式为{}", cullFace);
            }
        });
    }


    /**
     * @MethodName: setDotPlots
     * @Author 冰白寒祭
     * @Description: 遍历Group中的MeshView，将其顶点读取出来并用小球表示
     * @Date: 2024-11-12 18:13:02
     */
    public SmartGroup setDotPlots(Group modelGroup) {
        Group root = new Group();
        // 遍历模型组的子节点
        modelGroup.getChildren().forEach(meshs -> {
            if (meshs instanceof MeshView mesh) {
                mesh.setVisible(false); // 隐藏 MeshView 自身，只显示顶点
                // 检查并转换 MeshView 的 Mesh 为 TriangleMesh
                Mesh meshData = mesh.getMesh();
                if (meshData instanceof TriangleMesh triangleMesh) {
                    float[] points = triangleMesh.getPoints().toArray(null);

                    // 遍历顶点并创建 Sphere 表示每个顶点
                    for (int i = 0; i < points.length; i += 3) {
                        Sphere vertexSphere = new Sphere(0.002); // 设置小球体表示顶点
                        vertexSphere.setTranslateX(points[i]);
                        vertexSphere.setTranslateY(points[i + 1]);
                        vertexSphere.setTranslateZ(points[i + 2]);

                        // 设置顶点颜色（可选）
                        // PhongMaterial material = new PhongMaterial();
                        // material.setDiffuseColor(Color.CYAN);
                        // vertexSphere.setMaterial(material);

                        root.getChildren().add(vertexSphere);
                    }
                }
            }
        });
        return new SmartGroup(root);
    }


    /**
     * @MethodName: setupLighting
     * @Author 冰白寒祭
     * @Description: 遍历SmartGroup 给里边的3D模型添加灯光
     * @Date: 2024-11-12 08:57:09
     */
    public void setupLighting(SmartGroup group) {
        if (!isLightOn) {
            AmbientLight ambientLight = new AmbientLight(Color.WHITE);
            ambientLight.getScope().addAll(group);  // 让环境光照亮整个模型组
            group.getChildren().add(ambientLight);
            isLightOn = true;
            log.info("灯光已开启");
        } else {
            // 遍历modelGroup的子节点，手动移除AmbientLight实例 jdk8 之前
            // Iterator<Node> iterator = group.getChildren().iterator();
            // while (iterator.hasNext()) {
            //     Node node = iterator.next();
            //     if (node instanceof AmbientLight) {
            //         iterator.remove();
            //     }
            // }
            group.getChildren().removeIf(node -> node instanceof AmbientLight);
            isLightOn = false;
            log.info("灯光已关闭");
        }
    }


    /**
     * 扩展Group类以支持沿X轴和Y轴旋转
     */

    public static class SmartGroup extends Group {
        private final Rotate xRotate;
        private final Rotate yRotate;

        public SmartGroup() {
            // 将 xRotate 和 yRotate 添加到变换中
            // 使用 Rotate 对象直接绑定到 SmartGroup 以便累积旋转
            xRotate = new Rotate(0, Rotate.X_AXIS);
            yRotate = new Rotate(0, Rotate.Y_AXIS);
            this.getTransforms().addAll(xRotate, yRotate);
        }

        public SmartGroup(Group modelGroup) {
            // 将 xRotate 和 yRotate 添加到变换中
            // 使用 Rotate 对象直接绑定到 SmartGroup 以便累积旋转
            xRotate = new Rotate(0, Rotate.X_AXIS);
            yRotate = new Rotate(0, Rotate.Y_AXIS);
            Translate translate = new Translate();
            this.getTransforms().addAll(translate, xRotate, yRotate);
            // 添加导入的模型
            this.getChildren().add(modelGroup);
            // 居中导入的模型
            centerModel(modelGroup);
        }

        void rotateByX(int ang) {
            // 更新 angleX 的值，保持与 xRotate 的角度同步
            angleX.set(angleX.get() + ang);
        }

        void rotateByY(int ang) {
            // 更新 angleY 的值，保持与 yRotate 的角度同步
            angleY.set(angleY.get() + ang);
        }

        /**
         * @MethodName: centerModel
         * @Author 冰白寒祭
         * @Description: 居中导入的模型
         */
        public void centerModel(Group modelGroup) {
            Bounds bounds = modelGroup.getBoundsInParent();
            double centerX = (bounds.getMinX() + bounds.getMaxX()) / 2;
            double centerY = (bounds.getMinY() + bounds.getMaxY()) / 2;
            double centerZ = (bounds.getMinZ() + bounds.getMaxZ()) / 2;
            // 将模型移动到 SmartGroup 的原点
            modelGroup.getTransforms().add(new Translate(-centerX, -centerY, -centerZ));
        }
    }
}