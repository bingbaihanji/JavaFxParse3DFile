package com.bingbaihanji.javafxparse3dfile.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冰白寒祭
 * @date 2024-11-16 10:11:16
 * @description //TODO
 */

@Configuration
@ConfigurationProperties(prefix = "javafx.prism")
public class JavaFXPrismProperties {

    private boolean ifPrint;
    private boolean dirtyopts;
    private String order;
    private boolean allowhidpi;
    private boolean forceGPU;
    private boolean verbose;
    private String renderquality;

    // Getters and Setters
    public boolean isDirtyopts() {
        return dirtyopts;
    }

    public void setDirtyopts(boolean dirtyopts) {
        this.dirtyopts = dirtyopts;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public boolean isAllowhidpi() {
        return allowhidpi;
    }

    public void setAllowhidpi(boolean allowhidpi) {
        this.allowhidpi = allowhidpi;
    }

    public boolean isForceGPU() {
        return forceGPU;
    }

    public void setForceGPU(boolean forceGPU) {
        this.forceGPU = forceGPU;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getRenderquality() {
        return renderquality;
    }

    public void setRenderquality(String renderquality) {
        this.renderquality = renderquality;
    }

    public boolean isIfPrint() {
        return ifPrint;
    }

    public void setIfPrint(boolean ifPrint) {
        this.ifPrint = ifPrint;
    }
}
