/* $Id$ */

package ibis.connect.routedMessages;

import ibis.connect.BrokeredSocketFactory;
import ibis.connect.IbisServerSocket;
import ibis.connect.IbisSocket;
import ibis.util.IPUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

// SocketType descriptor for Routed Messages
// ------------------------------------------
public class RoutedMessagesSocketFactory extends BrokeredSocketFactory {
    public RoutedMessagesSocketFactory() throws IOException {

        if (!HubLinkFactory.isConnected()) {
            System.out
                    .println("RoutedMessages: cannot initialize- hub is not connected.");
            throw new IOException("Hub not connected.");
        }
    }

    public void destroySocketType() {
        HubLinkFactory.destroyHubLink();
    }

    public IbisSocket createClientSocket(InetAddress destAddr, int destPort,
            InetAddress localAddr, int localPort, int timeout, Map properties)
            throws IOException {
//	System.err.println("WARNING, bind to local IP address not implemented, using default");
        IbisSocket s = new RoutedMessagesSocket(destAddr, destPort, localAddr, localPort, properties);
	return s;
    }

    public IbisSocket createClientSocket(InetAddress addr, int port, Map p)
            throws IOException {
        IbisSocket s = new RoutedMessagesSocket(addr, port, p);
        return s;
    }

    public IbisServerSocket createServerSocket(InetSocketAddress addr, int backlog, Map p)
            throws IOException {
        IbisServerSocket s = new RoutedMessagesServerSocket(addr.getPort(), addr
                .getAddress(), p);
        return s;
    }

    public IbisSocket createBrokeredSocket(InputStream in, OutputStream out,
            boolean hintIsServer, Map properties)
            throws IOException {

        IbisSocket s = null;
        if (hintIsServer) {
            IbisServerSocket server = createServerSocket(
                    new InetSocketAddress(IPUtils.getLocalHostAddress(), 0), 1,
                    properties);

	    DataOutputStream dos = new DataOutputStream(out);
	    dos.writeUTF(server.getInetAddress().getHostAddress());
	    dos.writeInt(server.getLocalPort());
	    dos.flush();

            s = (IbisSocket) server.accept();
        } else {
	    DataInputStream di = new DataInputStream(in);
	    String addr = di.readUTF();
            InetAddress address = null;
	    try {
		address = InetAddress.getByName(addr);
	    } catch(Exception e) {
		throw new Error("EEK, could not create an inet address"
				    + "from a IP address. This shouldn't happen", e);
	    }
	    int port = di.readInt();
            s = createClientSocket(address, port, properties);
        }
        return s;
    }
}
