import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class HaiXin {

    private static final int SERVER_PORT = 60030;
    private static final int CLIENT_PORT = 9538;
    private static final int WORK_CLIENT_PORT = 9138;

    private InetAddress mAddress;
    private byte[] mBuffer = new byte[1024];
    private int mWorkPort;
    private DatagramSocket mSocket;

    private boolean buildConnect() {
        boolean connected = false;

        if (null != mAddress) {
            DatagramSocket clientSocket = null;
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(null);
                socket.setReuseAddress(true);
                socket.setSoTimeout(3000);
                socket.bind(new InetSocketAddress(9537));
                byte[] bytes = "CTCREATE\\r\\nID: 38224294\\r\\n\\r\\n0".getBytes();
                socket.send(new DatagramPacket(bytes, bytes.length, mAddress, SERVER_PORT));
                DatagramPacket packet = new DatagramPacket(mBuffer, mBuffer.length);
                socket.receive(packet);
                String result = new String(packet.getData());
                if (result.contains("\r")) {
                    String[] split = result.split("\r");
                    if (split.length > 1) {
                        String str = split[1];
                        if (str.contains(":")) {
                            split = str.split(":");
                            if (split.length >= 1) {
                                try {
                                    int intValue = Integer.parseInt(
                                            split[1].contains(" ") ? split[1].replace(" ", "") : split[1]);
                                    clientSocket = new DatagramSocket(CLIENT_PORT);
                                    clientSocket.setSoTimeout(3000);

                                    byte[] requestBytes = new byte[] {
                                            (byte) 83, (byte) 85, (byte) 83, (byte) 48
                                    };
                                    clientSocket.send(
                                            new DatagramPacket(requestBytes, requestBytes.length, mAddress,
                                                    intValue));

                                    bytes = "CTCREATE\r\nMAC: appmac_appmac_app\r\nVERSION: 0001\r\n\r\n0"
                                            .getBytes();
                                    clientSocket
                                            .send(new DatagramPacket(bytes, bytes.length, mAddress,
                                                    intValue));
                                    packet = new DatagramPacket(mBuffer, mBuffer.length);
                                    clientSocket.receive(packet);
                                    str = new String(packet.getData());

                                    bytes = "CCCREATE\r\nID: 38224294\r\n\r\n0".getBytes();
                                    clientSocket.send(
                                            new DatagramPacket(bytes, bytes.length, mAddress, intValue));
                                    clientSocket.receive(packet);

                                    clientSocket.send(
                                            new DatagramPacket(requestBytes, requestBytes.length, mAddress,
                                                    intValue));
                                    if (str.contains("\r")) {
                                        split = str.split("\r");
                                        if (split.length > 1) {
                                            str = split[1];
                                            if (str.contains(":")) {
                                                split = str.split(":");
                                                if (split.length >= 1) {
                                                    try {
                                                        mWorkPort = Integer.valueOf(split[1].contains(" ")
                                                                ? split[1].replace(" ", "") : split[1])
                                                                .intValue();
                                                        if (mWorkPort > 0) {
                                                            mSocket = new DatagramSocket(null);
                                                            mSocket.setReuseAddress(true);
                                                            mSocket.bind(
                                                                    new InetSocketAddress(WORK_CLIENT_PORT));
                                                            mSocket.setSoTimeout(2000);
                                                            requestBytes = new byte[] {
                                                                    (byte) 83, (byte) 85, (byte) 83, (byte) 0
                                                            };
                                                            mSocket.send(new DatagramPacket(requestBytes,
                                                                    requestBytes.length,
                                                                    mAddress, mWorkPort));
                                                            connected = true;
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                } finally {
                                    if (null != clientSocket) {
                                        clientSocket.close();
                                        clientSocket = null;
                                    }
                                }
                            }
                        }
                    }
                }

                int port = 0;
                byte[] requestBytes = new byte[] {
                        (byte) 83, (byte) 85, (byte) 83, (byte) 48
                };

                clientSocket = new DatagramSocket(CLIENT_PORT);
                clientSocket.setSoTimeout(3000);
                clientSocket.send(new DatagramPacket(requestBytes, requestBytes.length, mAddress, port));

                bytes = "CTCREATE\r\nMAC: appmac_appmac_app\r\nVERSION: 0001\r\n\r\n0".getBytes();
                clientSocket.send(new DatagramPacket(bytes, bytes.length, mAddress, port));
                packet = new DatagramPacket(mBuffer, mBuffer.length);
                clientSocket.receive(packet);

                String str = new String(packet.getData());
                bytes = "CCCREATE\r\nID: 38224294\r\n\r\n0".getBytes();
                clientSocket.send(new DatagramPacket(bytes, bytes.length, mAddress, port));
                clientSocket.receive(packet);
                clientSocket.send(new DatagramPacket(requestBytes, requestBytes.length, mAddress, port));
                if (str.contains("\r")) {
                    String[] split = str.split("\r");
                    if (split.length > 1) {
                        str = split[1];
                        if (str.contains(":")) {
                            split = str.split(":");
                            if (split.length >= 1) {
                                if (split[1].contains(" ")) {
                                }
                                mWorkPort = Integer
                                        .parseInt(
                                                split[1].contains(" ") ? split[1].replace(" ", "")
                                                        : split[1]);
                                if (mWorkPort > 0) {
                                    if (null != mSocket) {
                                        mSocket.close();
                                    }
                                    mSocket = new DatagramSocket(null);
                                    mSocket.setReuseAddress(true);
                                    mSocket.bind(new InetSocketAddress(WORK_CLIENT_PORT));
                                    mSocket.setSoTimeout(2000);
                                    requestBytes = new byte[] {
                                            (byte) 83, (byte) 85, (byte) 83, (byte) 0
                                    };
                                    mSocket.send(new DatagramPacket(requestBytes, requestBytes.length,
                                            mAddress, mWorkPort));
                                    connected = true;
                                }
                            }
                        }
                    }
                }
            } catch (IOException e3) {
                e3.printStackTrace();
            } finally {
                if (null != socket) {
                    socket.close();
                }

                if (null != clientSocket) {
                    clientSocket.close();
                }
            }
        }
        return connected;
    }

    public static void main(String[] args) {
        try {
            HaiXin haiXin = new HaiXin();
            haiXin.mAddress = InetAddress.getByName("");
            haiXin.buildConnect();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}
