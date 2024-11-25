package com.bingbaihanji.javafxparse3dfile.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class JavaFXPrismConfig {

    private final JavaFXPrismProperties prismProperties;

    public JavaFXPrismConfig(JavaFXPrismProperties prismProperties) {
        this.prismProperties = prismProperties;
        applyPrismProperties();
    }

    private void applyPrismProperties() {
        System.setProperty("prism.dirtyopts", String.valueOf(prismProperties.isDirtyopts()));
        System.setProperty("prism.order", prismProperties.getOrder());
        System.setProperty("prism.allowhidpi", String.valueOf(prismProperties.isAllowhidpi()));
        System.setProperty("prism.forceGPU", String.valueOf(prismProperties.isForceGPU()));
        System.setProperty("prism.verbose", String.valueOf(prismProperties.isVerbose()));
        System.setProperty("prism.renderquality", prismProperties.getRenderquality());
        if (prismProperties.isIfPrint()) {
            // 可选：输出调试信息
            System.out.println("JavaFX Prism 应用的设置:");
            System.out.println("prism.dirtyopts: " + prismProperties.isDirtyopts());
            System.out.println("prism.order: " + prismProperties.getOrder());
            System.out.println("prism.allowhidpi: " + prismProperties.isAllowhidpi());
            System.out.println("prism.forceGPU: " + prismProperties.isForceGPU());
            System.out.println("prism.verbose: " + prismProperties.isVerbose());
            System.out.println("prism.renderquality: " + prismProperties.getRenderquality());
        }
    }
}
