package ru.larionov.gui.sprites;

import ru.larionov.gui.RenderCanvas;

import java.awt.*;

public class CenterCircle extends Sprite {
    private final Color color;

    public CenterCircle() {
        halfWidth = 20;
        halfHeight = halfWidth;
        color = new Color (
                (int)(Math.random() * 255), //r
                (int)(Math.random() * 255), //g
                (int)(Math.random() * 255)  //b
        );
    }

    @Override
    public void update(RenderCanvas canvas, float deltaTime) {
        x = (canvas.getWidth() / 2);
        y = (canvas.getHeight() / 2);
    }

    @Override
    public void render(RenderCanvas canvas, Graphics g) {
        g.setColor(color);
        g.fillOval((int) getLeft(), (int) getTop(),
                (int) getWidth(), (int) getHeight());
    }
}
