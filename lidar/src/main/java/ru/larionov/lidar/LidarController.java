package ru.larionov.lidar;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class LidarController extends Thread{

    public static final String PORT_DESCRIPTOR = "COM3";
    private final SerialPort port;

    private static final byte[] STOP = new byte[] {(byte) 0xaa, 0x55, (byte) 0xf5, 0x0a};
    private static final byte[] STOP_WRITE = new byte[] {(byte) 0xaa, 0x55, 0x55, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xad};
    private static final byte[] START = new byte[] {(byte) 0xaa, 0x55, (byte) 0xf0, 0x0f};
    private static final byte[] TWO_ZERO = new byte[] {(byte) 0, (byte) 0};

    private static final double ANGLE_OFFSET = 90;

    private static final byte FIRST_SYMBOL = (byte) 0xaa;
    private static final byte SECOND_SYMBOL = 0x55;

    private final List<Point> points;
    private int size;
    private int currentPosition;

    private InputStream is;

    public LidarController() {
        points = new ArrayList<>();
        size = 0;
        currentPosition = -1;

        System.out.println("Start");

        port = SerialPort.getCommPort(PORT_DESCRIPTOR);
        port.setBaudRate(115200);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING,0,0);
        boolean b = port.openPort();
        if (!b) {
            System.out.println("Не удалось открыть");
        } else {
            is = port.getInputStream();
        }
    }

    @Override
    public void run() {
        super.run();
        byte symbol = 0;
        int ct, lsn, fsa, lsa, cs, si, dist;
        double angle, aCorrect, F, L;
        try {
        while (!isInterrupted()) {
//            int length = port.bytesAvailable();
//            int curIndex = 0;

            boolean end = false;
            while (!end) {

                if (symbol != FIRST_SYMBOL) {
                    symbol = is.readNBytes(1)[0];
                    continue;
                }
                else {
                    symbol = is.readNBytes(1)[0];
                    if (symbol == SECOND_SYMBOL) {
                        ct = is.readNBytes(1)[0];
                        lsn = is.readNBytes(1)[0];

                        fsa = getByteBufferForInt(is.readNBytes(2))
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt();
                        lsa = getByteBufferForInt(is.readNBytes(2))
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt();
                        cs = getByteBufferForInt(is.readNBytes(2))
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt();

                        for (int i = 0; i < lsn; i++) {
                            si = getByteBufferForInt(is.readNBytes(2))
                                    .order(ByteOrder.LITTLE_ENDIAN)
                                    .getInt();
                            dist = si >> 2;
                            aCorrect = dist == 0 ? 0 : Math.atan(19.16 * (dist - 90.15) / (dist * 90.15));
                            F = (fsa >> 1) / 64.0;
                            L = (lsa >> 1) / 64.0;
                            angle = F + ((L - F) / (lsn)) * (i) - aCorrect;
                            angle += ANGLE_OFFSET;
                            addPoint(dist, angle, ct);
                        }
                    }
                }
                end = true;
            }
//            if (length > 0) {
//                byte[] bytes = new byte[length];
//                port.readBytes(bytes, length);
//                while (curIndex < length) {
//                    boolean found = false;
//                    for (int i = curIndex; i < bytes.length - 1; i++) {
//                        if (bytes[i] == (byte) 0xaa && bytes[i + 1] == 0x55) {
//                            curIndex = i + 2;
//                            found = true;
//                            break;
//                        }
//                    }
//                    if (!found) break;
//                    if (bytes.length < curIndex + 8) break;
//                    //ByteBuffer CT = getByteBufferForInt(bytes, curIndex, 1).order(ByteOrder.LITTLE_ENDIAN);
//                    int ct = bytes[curIndex];
//                    //ByteBuffer LSN = getByteBufferForInt(bytes, curIndex + 1, 1).order(ByteOrder.LITTLE_ENDIAN);
//                    int lsn = bytes[curIndex + 1];
//
//                    ByteBuffer FSA = getByteBufferForInt(bytes, curIndex + 2, 2).order(ByteOrder.LITTLE_ENDIAN);
//                    int fsa = FSA.getInt();
//                    ByteBuffer LSA = getByteBufferForInt(bytes, curIndex + 4, 2).order(ByteOrder.LITTLE_ENDIAN);
//                    int lsa = LSA.getInt();
//                    ByteBuffer CS = getByteBufferForInt(bytes, curIndex + 6, 2).order(ByteOrder.LITTLE_ENDIAN);
//                    int cs = LSA.getInt(0);
////                    System.out.println("CT: " + ct);
////                    System.out.println("LSN: " + lsn);
//
//                    int ii = 0;
//                    for (int i = curIndex + 8; i < curIndex + 7 + lsn * 2; i += 2) {
//                        if (bytes.length > i + 2) {
//                            ByteBuffer SI = getByteBufferForInt(bytes, i, 2).order(ByteOrder.LITTLE_ENDIAN);
//                            int si = SI.getInt(0);
//                            int dist = si >> 2;
//                            double Acorrect = dist == 0 ? 0 : Math.atan(19.16 * (dist - 90.15) / (dist * 90.15));
//                            double F = (fsa >> 1) / 64.0;
//                            double L = (lsa >> 1) / 64.0;
//                            double angle = F + ((L - F) / (lsn)) * (ii) - Acorrect;
//                            //System.out.println(String.format("l: %d mm. a: %.3f", dist, angle));
//                            addPoint(dist, angle, ct);
//                            ii++;
//                        }
//                    }
//                    curIndex += 7 + lsn;
//                }
//            }
//            try {
//               Thread.sleep(100);
//            } catch (InterruptedException e) {
//                interrupt();
//            }
        }
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addPoint(int distance, double angle, int ct) {
        if (ct == 1) {
            size = currentPosition + 1;
            currentPosition = 0;
        }
        List<Point> points = getPoints();
        if (currentPosition > -1) {
            if (points.size() >= currentPosition + 1) {
                points.get(currentPosition).setDistance(distance);
                points.get(currentPosition).setAngle(angle);
            } else {
                points.add(new Point(distance, angle));
            }
            if (size < currentPosition + 1) size++;
            currentPosition++;
        }
    }

    private static ByteBuffer getByteBufferForInt(byte[] bytes) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4 - bytes.length; i++) {
            b[i + bytes.length] = (byte) 0;
            b[i] = bytes[i];
        }
        return ByteBuffer.wrap(b);
    }

    private static double getCorrect(int l) {
        if (l == 0) return 0;
        return Math.atan(19.16 * ((l - 90.15)/(90.15 * l)));
    }

    public List<Point> getPoints() {
        return points;
    }

    public int getSize() {
        return size;
    }
}

