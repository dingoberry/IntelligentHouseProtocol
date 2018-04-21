import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class PPTVRec {

    private static final int SERVER_PORT = 9101;

    private InetAddress mAddress;

    private int retrievePort() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(3000);

            byte[] dicoverBytes = ("discover _anymote._tcp "
                    + socket.getLocalPort() + "\n").getBytes();
            DatagramPacket packet = new DatagramPacket(dicoverBytes, dicoverBytes.length, mAddress, SERVER_PORT);
            socket.send(packet);

            byte[] bArr = new byte[1024];
            for (int i = 0; i < 3; i++) {
                try {
                    packet = new DatagramPacket(bArr, bArr.length);
                    socket.receive(packet);
                    String[] split = new String(packet.getData(), 0, packet.getLength())
                            .trim().split(" \\| ");
                    if (split[0].equals("_anymote._tcp")
                            && split.length > 3
                            && mAddress.equals(packet.getAddress())) {
                        int parseInt = Integer.parseInt(split[2]);
                        if (parseInt > 0) {
                            return parseInt;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                socket.disconnect();
                socket.close();
            }
        }
        return -1;
    }

    private void resolveAfter(byte[] bArr, int i) {
        bArr[i + 4] = bArr[5];
        for (int i2 = 1; i2 < i - 1; i2++) {
            bArr[i + 4] = (byte) (bArr[i + 4] ^ bArr[i2 + 5]);
        }
    }

    private void resolveBefore(byte[] bArr, int i, int i2) {
        bArr[i2 + 3] = (byte) (i >> 24);
        bArr[i2 + 2] = (byte) (i >> 16);
        bArr[i2 + 1] = (byte) (i >> 8);
        bArr[i2 + 0] = (byte) (i >> 0);
    }

    private byte[] convertMessage(String msg) {
        int length = msg.getBytes().length + 1;
        byte[] bArr = new byte[(length + 5)];
        bArr[0] = (byte) 83;
        resolveBefore(bArr, length, 1);
        System.arraycopy(msg.getBytes(), 0, bArr, 5, msg.getBytes().length);
        resolveAfter(bArr, length);
        return bArr;

    }

    private void connect(int port) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(mAddress, port), 3000);
            if (socket.isConnected()) {
                Thread.sleep(3000);
                OutputStream io = socket.getOutputStream();
                io.write(convertMessage("--check--"));
                io.flush();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            PPTVRec pptvRec = new PPTVRec();
            pptvRec.mAddress = InetAddress.getByName("");
            int port = pptvRec.retrievePort();
            if (-1 != port) {
                pptvRec.connect(port);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
