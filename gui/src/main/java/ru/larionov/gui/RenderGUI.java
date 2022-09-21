package ru.larionov.gui;


import ru.larionov.gui.sprites.CenterCircle;
import ru.larionov.gui.sprites.PointSprite;
import ru.larionov.gui.sprites.Sprite;
import ru.larionov.lidar.LidarController;
import ru.larionov.lidar.Point;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RenderGUI extends JFrame {

    private static final int POS_X = 0;
    private static final int POS_Y = 0;
    private static final int WINDOW_WIDTH = 1980;
    private static final int WINDOW_HEIGHT = 1020;

    private List<Sprite> sprites = new ArrayList<>();
    private List<PointSprite> points = new ArrayList<>();
    private final LidarController lidarController;

    public RenderGUI() throws HeadlessException {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(POS_X, POS_Y, WINDOW_WIDTH, WINDOW_HEIGHT);
        RenderCanvas canvas = new RenderCanvas(this);
        add(canvas);
        lidarController = new LidarController();
        lidarController.start();
        initApplication();
        setTitle("Lidar M1C1");
        setVisible(true);

    }

    private void initApplication() {
        sprites.add(new CenterCircle());
    }

    public void onDrawFrame(RenderCanvas canvas, Graphics g, float deltaTime) {
        updatePoints();
        update(canvas, deltaTime);
        render(canvas, g);
    }

    private void updatePoints() {
        List<Point> pointsCloud = lidarController.getPoints();
        for (int i = 0; i < pointsCloud.size(); i++) {
            if (i + 1 > points.size())
                points.add(new PointSprite(lidarController, points.size()));
            points.get(i).setDistance(pointsCloud.get(i).getDistance());
            points.get(i).setAngle(pointsCloud.get(i).getAngle());
        }
    }

    private void update(RenderCanvas canvas, float deltaTime) {
        sprites.forEach(s -> s.update(canvas, deltaTime));
        points.forEach(p -> p.update(canvas, deltaTime));
    }

    private void render(RenderCanvas canvas, Graphics g) {
        sprites.forEach(s -> s.render(canvas, g));
        points.forEach(p -> p.render(canvas, g));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { // Event Dispatching Thread
            new RenderGUI();
        });
    }
}
