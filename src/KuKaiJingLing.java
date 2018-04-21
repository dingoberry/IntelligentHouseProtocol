import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class KuKaiJingLing {

    private static final int SERVER_PORT = 1988;
    private static final String SEARCH_ACTION = "{\"lafite_cmd\":\"FINDSP\",\"lafite_type\":\"lafite_search\"}";

    private InetAddress mAddress;

    private boolean buildConnect() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(SERVER_PORT));
            socket.setSoTimeout(3000);
            byte[] bytes = SEARCH_ACTION.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(bytes,
                    bytes.length, mAddress, SERVER_PORT);
            if (socket != null) {
                byte[] bArr = new byte[2048];
                socket.send(datagramPacket);
                DatagramPacket datagramPacket2 = new DatagramPacket(bArr, bArr.length);
                for (int i = 0; i < 3; i++) {
                    socket.receive(datagramPacket2);
                    String str = new String(datagramPacket2.getData(), 0, datagramPacket2.getLength());
                    if (!TextUtils.isEmpty(str)) {
                        System.out.println(str);
                        return str.contains("FINDOK");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                socket.close();
            }
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            KuKaiJingLing jingLing = new KuKaiJingLing();
            jingLing.mAddress = InetAddress.getByName("");
            jingLing.buildConnect();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}
