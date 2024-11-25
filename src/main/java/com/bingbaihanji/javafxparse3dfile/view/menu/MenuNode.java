package com.bingbaihanji.javafxparse3dfile.view.menu;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * @author 冰白寒祭
 * @date 2024-11-05 03:39:53
 * @description //TODO
 */
public class MenuNode {

    private final LinearGradient linearGradient = new LinearGradient(
            0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, new Color(0.99, 0.55, 1.0, 1.0)),
            new Stop(0.5, new Color(0.17, 0.83, 0.99, 1.0)),
            new Stop(1.0, new Color(0.18, 1.0, 0.54, 1.0))
    );

    private final MenuItem importObj = new MenuItem("导入3D模型文件");
    private final MenuItem screenshots = new MenuItem("截图");
    private final MenuItem setBackgroundColor = new MenuItem("设置背景颜色");

    public MenuBar getMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setBackground(new Background(new BackgroundFill(linearGradient, null, null)));

        Menu file = new Menu("文件");
        Menu tools = new Menu("工具");
        Menu set = new Menu("工具");
        file.getItems().addAll(importObj);
        tools.getItems().addAll(screenshots);
        set.getItems().addAll(setBackgroundColor);

        menuBar.getMenus().addAll(file, tools, set);
        return menuBar;
    }

    public MenuItem getImportObj() {
        return importObj;
    }

    public MenuItem getScreenshots() {
        return screenshots;
    }

    public MenuItem getSetBackgroundColor() {
        return setBackgroundColor;
    }
}
