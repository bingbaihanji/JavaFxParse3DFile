package com.bingbaihanji.javafxparse3dfile;

import com.bingbaihanji.javafxparse3dfile.parse.Importer3D;
import com.bingbaihanji.javafxparse3dfile.view.BackgroundColorPicker;
import com.bingbaihanji.javafxparse3dfile.view.ModelView;
import com.bingbaihanji.javafxparse3dfile.view.menu.MenuNode;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.*;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main extends Application {
    private final PointLight pointLight = new PointLight();
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    ModelView.SmartGroup modelGroup;
    ModelView modelView = new ModelView(-25, 50);

    private String filePath;

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void init() throws Exception {
        super.init();
        // 获取命令行参数
        List<String> params = getParameters().getRaw();
        if (!params.isEmpty()) {
            filePath = params.get(0); // 从命令行参数中获取 filePath
            log.info("filePath为: {}", filePath);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        modelGroup = new ModelView.SmartGroup();
        modelGroup.setTranslateZ(-15);
        float WIDTH = 800;
        float HEIGHT = 600;

        BorderPane root = new BorderPane();
        root.setPrefHeight(HEIGHT);
        root.setPrefWidth(WIDTH);
        MenuNode menuNode = new MenuNode();
        MenuBar menuBar = menuNode.getMenuBar();

        root.setTop(menuBar);


        SubScene subScene = new SubScene(modelGroup, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setCache(true); // 开启缓存
        subScene.setCacheHint(CacheHint.QUALITY);


        // 绑定 SubScene 的宽度和高度到 root 的宽度和高度
        subScene.widthProperty().bind(root.widthProperty());
        subScene.heightProperty().bind(root.heightProperty());

        root.setCenter(subScene);
        Scene scene = new Scene(root);

        Camera camera = modelView.setupCamera();
        subScene.setFill(Color.DARKGRAY);
        subScene.setCamera(camera);
        modelView.initMouseControl(modelGroup, subScene.getScene(), primaryStage);
        modelView.setupKeyboardControls(subScene.getScene(), modelGroup);

        // 调用拖放文件方法，并传入剔除模式
        setupDragAndDrop(subScene.getScene(), CullFace.NONE);

        // 直接调用灯光控制和切换模式
        setupKeyLighting(subScene.getScene(), modelGroup);
        setKeyDrawMode(subScene.getScene(), modelGroup);
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/3d.png")));
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setScene(scene);
        primaryStage.setTitle("3D Model Viewer");
        primaryStage.show();
        // 如果 filePath 非空，则加载模型文件
        if (filePath != null) {
            loadModelFromFile(filePath, CullFace.NONE);
        }

        // 导入3D模型
        menuNode.getImportObj().setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("3D File", ".obj", "*.obj", ".ma", "*.ma", ".fxml", "*.fxml");
            fileChooser.getExtensionFilters().add(extensionFilter);
            File filePath = fileChooser.showOpenDialog(primaryStage);
            if (filePath != null) {
                loadModelFromFile(String.valueOf(filePath), CullFace.NONE);
            }
        });

        // 截图
        menuNode.getScreenshots().setOnAction(event -> {
            // 创建快照
            SnapshotParameters snapshotParameters = new SnapshotParameters();
            snapshotParameters.setFill(Color.TRANSPARENT); // 使用透明背景
            WritableImage image = subScene.snapshot(snapshotParameters, null);

            // 保存截图到剪切板
            Clipboard systemClipboard = Clipboard.getSystemClipboard(); // 获取系统剪切板
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putImage(image);
            systemClipboard.setContent(clipboardContent);

            // 保存到文件
            BufferedImage png = SwingFXUtils.fromFXImage(image, null);
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image File", ".png", "*.png")
            );
            File save = fileChooser.showSaveDialog(primaryStage);
            if (save != null) {
                try {
                    ImageIO.write(png, "png", save);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // 设置背景颜色
        menuNode.getSetBackgroundColor().setOnAction(event -> {
            BackgroundColorPicker backgroundColorPicker = new BackgroundColorPicker(subScene);
            backgroundColorPicker.show();
        });


        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                pointLight.setRotate(pointLight.getRotate() + 1);
            }
        };
        timer.start();
    }

    public void setupKeyLighting(Scene scene, ModelView.SmartGroup group) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F1) {
                modelView.setupLighting(group);
            }
            event.consume();
        });
    }

    public void setKeyDrawMode(Scene scene, ModelView.SmartGroup modelGroup) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F2) {
                modelView.setDrawMode(modelGroup);
            }
            event.consume();
        });
    }

    private void setupDragAndDrop(Scene scene, CullFace cullFace) {
        scene.setOnDragOver(event -> {
            if (event.getGestureSource() != scene && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        scene.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                filePath = file.getAbsolutePath();
                loadModelFromFile(filePath, cullFace);
            }
            event.setDropCompleted(true);
            event.consume();
        });
    }


    /**
     * @MethodName: loadModelFromFile
     * @Author 冰白寒祭
     * @Description: 加载模型文件
     * @Date: 2024-11-14 12:02:35
     */
    private void loadModelFromFile(String filePath, CullFace cullFace) {
        try {
            Group importedModel = Importer3D.load(filePath);
            modelGroup.getChildren().clear();
            modelGroup.getChildren().add(importedModel);
            modelGroup.centerModel(importedModel);
            log.info("加载文件为{}", filePath);
            modelView.setCullFace(cullFace, modelGroup);
            modelView.setupLighting(modelGroup); // 灯光控制
            modelView.setDrawMode(modelGroup); // 切换显示模式
        } catch (IOException e) {
            log.error("读取错误{}", e.getMessage(), e);
        }
    }
}
