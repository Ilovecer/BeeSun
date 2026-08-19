package ru.amiloxs.wellsun.model;

public final class HologramLine {
    private final String content;
    private final double height;
    private final double offsetX;
    private final double offsetZ;

    public HologramLine(String content, double height, double offsetX, double offsetZ) {
        this.content = content != null ? content : "";
        this.height = height;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
    }

    public String getContent() {
        return content;
    }

    public double getHeight() {
        return height;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetZ() {
        return offsetZ;
    }
}
