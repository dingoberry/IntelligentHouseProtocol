import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class SSDPProtocol {

    public static void main(String[] args) {
        new DlnaDeviceDiscovery().startDiscover();
    }

    private static class DlnaDeviceDiscovery {

//        private static final String ID = "DLNA";
        private static final String SERVICE_FILTER = "urn:schemas-upnp-org:device:MediaRenderer:1";

        private final int PORT = 1900;
        private final String ADDRESS = "239.255.255.250";
        private final AtomicBoolean mRunning = new AtomicBoolean();

        private MulticastSocket mMultiCastSocket;
        private DatagramSocket mResponseSocket;
        private Set<String> mInformation;

        InetAddress getLocalIpAddress() {
            try {
                for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e
                        .hasMoreElements();) {
                    for (Enumeration<InetAddress> i = e.nextElement().getInetAddresses(); i
                            .hasMoreElements();) {
                        InetAddress address = i.nextElement();
                        if (address.isLoopbackAddress()) {
                            continue;
                        }
                        if (address.isSiteLocalAddress()) {
                            return address;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private String getFoundData() {
            StringBuilder sb = new StringBuilder();
            final String NEWLINE = "\r\n";
            sb.append("M-SEARCH * HTTP/1.1" + NEWLINE);
            sb.append("HOST: 239.255.255.250:" + PORT + NEWLINE);
            sb.append("MAN: \"ssdp:discover\"" + NEWLINE);
            sb.append("ST: ").append(SERVICE_FILTER).append(NEWLINE);
            sb.append("MX: ").append(5).append(NEWLINE);
            if (SERVICE_FILTER.contains("udap")) {
                sb.append("USER-AGENT: UDAP/2.0" + NEWLINE);
            }
            sb.append(NEWLINE);
            return sb.toString();
        }
        
        private void print(DatagramPacket dp) {
            String msg = new String(dp.getData(), 0, dp.getLength());
            int size = mInformation.size();
            mInformation.add(msg);
            if (size != mInformation.size()) {
                System.out.println(msg);
            }
        }

        private Runnable[] makeResponseWatcher() {
            return new Runnable[] {
                    new Runnable() {
                        @Override
                        public void run() {
                            while (mRunning.get()) {
                                try {
                                    byte[] buf = new byte[1024];
                                    DatagramPacket dp = new DatagramPacket(buf, buf.length);
                                    mResponseSocket.receive(dp);
                                    print(dp);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            while (mRunning.get()) {
                                try {
                                    byte[] buf = new byte[1024];
                                    DatagramPacket dp = new DatagramPacket(buf, buf.length);
                                    mMultiCastSocket.receive(dp);
                                    print(dp);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            };
        }

        void startDiscover() {
            if (!mRunning.compareAndSet(false, true)) {
                return;
            }

            try {
                InetAddress localAddress = getLocalIpAddress();
                mMultiCastSocket = new MulticastSocket(1900);
                mInformation = Collections.synchronizedSet(new HashSet<>());
                SocketAddress groupAddress = new InetSocketAddress(ADDRESS, 1900);
                mMultiCastSocket.joinGroup(groupAddress,
                        NetworkInterface.getByInetAddress(localAddress));

                mResponseSocket = new DatagramSocket(null);
                mResponseSocket.setReuseAddress(true);
                mResponseSocket.bind(new InetSocketAddress(localAddress, 0));

                String data = getFoundData();
                DatagramPacket p = new DatagramPacket(data.getBytes(), data.length(), groupAddress);
                mResponseSocket.send(p);

                for (Runnable r : makeResponseWatcher()) {
                    new Thread(r).start();
                }

                new Thread() {
                    public void run() {

                    }
                }.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
