package ru.larionov.gui.sprites;

import ru.larionov.gui.RenderCanvas;
import ru.larionov.lidar.LidarController;

import java.awt.*;

public class PointSprite extends Sprite {
    private static final double MAX_MILLIMETERS = 4000;

    private final Color color = new Color(22, 37, 28);

    private LidarController lidarController;
    private int index;

    private int distance;
    private double angle;

    public PointSprite(LidarController lidarController, int index) {
        this.lidarController = lidarController;
        this.index = index;
        halfWidth = 2;
        halfHeight = halfWidth;
    }

    @Override
    public void update(RenderCanvas canvas, float deltaTime) {
        if (distance > 0) {
            int x0 = canvas.getWidth() / 2;
            int y0 = canvas.getHeight() / 2;
            double k = (Math.min(x0, y0) - 20) / (MAX_MILLIMETERS);

            double xCalc = distance * Math.cos(Math.toRadians(angle)) * k * -1;
            double yCalc = distance * Math.sin(Math.toRadians(angle)) * k * -1;

            x = x0 + (int) xCalc;
            y = y0 + (int) yCalc;
        }
    }

    @Override
    public void render(RenderCanvas canvas, Graphics g) {
        g.setColor(color);
        if (!(distance == 0 || lidarController.getSize() < index + 1))
            g.fillRect((int) getLeft(), (int) getTop(),
                (int) getWidth(), (int) getHeight());
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
