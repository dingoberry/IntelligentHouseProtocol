import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class KuKai {

    private static final int SERVER_PORT = 1980;
    private static final int CLIENT_PORT = 45235;

    private static final String ALIVE_CHECK = "{\"cmd\":\"aliveCheck\",\"param\":\"\",\"type\":\"alive\"}";

    private byte[] mBuffer = new byte[1024];
    private InetAddress mAddress;
    private int mWorkPort;

    private int requestPort(DatagramSocket socket) {
        int i = 0;
        InetAddress inetAddress = mAddress;
        if (inetAddress != null) {
            byte[] bArr = new byte[] {
                    (byte) 123, (byte) 34, (byte) 99, (byte) 109, (byte) 100, (byte) 34, (byte) 58, (byte) 34,
                    (byte) 67, (byte) 79, (byte) 78, (byte) 78, (byte) 69, (byte) 67, (byte) 84, (byte) 83,
                    (byte) 80, (byte) 34, (byte) 44, (byte) 34, (byte) 112, (byte) 97, (byte) 114, (byte) 97,
                    (byte) 109, (byte) 34, (byte) 58, (byte) 34, (byte) 123, (byte) 92, (byte) 34, (byte) 99,
                    (byte) 108, (byte) 105, (byte) 101, (byte) 110, (byte) 116, (byte) 78, (byte) 97,
                    (byte) 109, (byte) 101, (byte) 92, (byte) 34, (byte) 58, (byte) 92, (byte) 34, (byte) -26,
                    (byte) -126, (byte) -97, (byte) -25, (byte) -87, (byte) -70, (byte) -23, (byte) -127,
                    (byte) -91, (byte) -26, (byte) -114, (byte) -89, (byte) 92, (byte) 34, (byte) 44,
                    (byte) 92, (byte) 34, (byte) 115, (byte) 101, (byte) 114, (byte) 118, (byte) 105,
                    (byte) 99, (byte) 101, (byte) 115, (byte) 78, (byte) 97, (byte) 109, (byte) 101,
                    (byte) 92, (byte) 34, (byte) 58, (byte) 92, (byte) 34, (byte) 83, (byte) 101, (byte) 114,
                    (byte) 118, (byte) 101, (byte) 114, (byte) 83, (byte) 101, (byte) 114, (byte) 118,
                    (byte) 105, (byte) 99, (byte) 101, (byte) 92, (byte) 34, (byte) 44, (byte) 92, (byte) 34,
                    (byte) 118, (byte) 101, (byte) 114, (byte) 115, (byte) 105, (byte) 111, (byte) 110,
                    (byte) 92, (byte) 34, (byte) 58, (byte) 92, (byte) 34, (byte) 52, (byte) 50, (byte) 49,
                    (byte) 48, (byte) 48, (byte) 48, (byte) 48, (byte) 48, (byte) 48, (byte) 92, (byte) 34,
                    (byte) 125, (byte) 34, (byte) 44, (byte) 34, (byte) 116, (byte) 121, (byte) 112,
                    (byte) 101, (byte) 34, (byte) 58, (byte) 34, (byte) 99, (byte) 111, (byte) 110,
                    (byte) 110, (byte) 101, (byte) 99, (byte) 116, (byte) 34, (byte) 125
            };
            DatagramPacket packet = new DatagramPacket(bArr, bArr.length, inetAddress, SERVER_PORT);

            try {
                if (socket != null) {
                    socket.send(packet);
                    packet = new DatagramPacket(mBuffer, mBuffer.length);
                    socket.receive(packet);
                    String str = new String(packet.getData(), 0, packet.getLength());
                    if (!TextUtils.isEmpty(str)) {
                        JSONObject jSONObject = new JSONObject(str);
                        if (jSONObject.getString("cmd").equals("CONNECTSP")) {
                            str = jSONObject.getString("param");
                            if (!TextUtils.isEmpty(str)) {
                                JSONObject jSONObject2 = new JSONObject(str);
                                if (jSONObject.getString("type").equals("connect")
                                        && jSONObject2.getString("response").equals("accepted")) {
                                    i = packet.getPort();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return i;
    }

    private boolean retrievePort() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(CLIENT_PORT));
            socket.setSoTimeout(3000);
            for (int i = 0; i < 3; i++) {
                int port = requestPort(socket);
                if (port != 0) {
                    mWorkPort = port;
                    return true;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                socket.close();
            }
        }
        return false;
    }

    private void buildConnect() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(CLIENT_PORT));
            socket.setSoTimeout(3000);

            byte[] bytes = ALIVE_CHECK.getBytes();
            DatagramPacket packet = new DatagramPacket(bytes,
                    bytes.length, mAddress, mWorkPort);
            socket.send(packet);
            packet = new DatagramPacket(mBuffer,
                    mBuffer.length);
            socket.receive(packet);
            String str = new String(packet.getData(), 0, packet.getLength());
            if (!TextUtils.isEmpty(str) && str.contains("yes")) {
                System.out.println(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                socket.close();
            }
        }
    }

    public static void main(String[] args) {
        try {
            KuKai kuKai = new KuKai();
            kuKai.mAddress = InetAddress.getByName("");
            if (kuKai.retrievePort()) {
                kuKai.buildConnect();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
