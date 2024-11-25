package com.bingbaihanji.javafxparse3dfile.view;

import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * @author 冰白寒祭
 * @date 2024-11-15 14:38:23
 * @description //TODO
 */

public class BackgroundColorPicker {

    private final SubScene subScene;

    public BackgroundColorPicker(SubScene subScene) {
        this.subScene = subScene;
    }

    /**
     * 显示颜色选择器对话框
     */
    public void show() {
        // 创建颜色选择器
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue((Color) subScene.getFill()); // 初始化为当前背景颜色

        // 创建对话框窗口
        Stage colorStage = new Stage();
        colorStage.getIcons().add(new Image("/setBackgroundColor.png"));

        BorderPane colorRoot = new BorderPane(colorPicker);

        // 设置渐变背景
        LinearGradient linearGradient = new LinearGradient(
                0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, new Color(0.98, 0.0, 0.99, 1.0)),
                new Stop(1.0, new Color(0.01, 0.86, 0.87, 1.0))
        );
        colorRoot.setBackground(new Background(new BackgroundFill(linearGradient, null, null)));

        // 设置对话框
        Scene colorScene = new Scene(colorRoot, 300, 100);

        colorStage.setResizable(false);
        colorStage.setScene(colorScene);
        colorStage.setTitle("选择背景颜色");
        colorStage.initModality(Modality.APPLICATION_MODAL);
        colorStage.show();

        // 添加颜色选择监听器
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            subScene.setFill(newValue); // 更新背景颜色
        });

        // 点击确认关闭对话框
        colorPicker.setOnAction(e -> colorStage.close());
    }
}
