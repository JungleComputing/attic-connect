/* $Id$ */

package ibis.connect.plainSocketFactories;

import ibis.connect.BrokeredSocketFactory;
import ibis.connect.IbisServerSocket;
import ibis.connect.IbisSocket;
import ibis.util.IPUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.log4j.Logger;

// SocketType descriptor for plain TCP sockets
// -------------------------------------------
public class PlainTCPSocketFactory extends BrokeredSocketFactory {

    static Logger logger = ibis.util.GetLogger.getLogger(PlainTCPSocketFactory.class
            .getName());

    public PlainTCPSocketFactory() {
    }

    
    public IbisSocket createClientSocket(InetAddress destAddr, int destPort,
            InetAddress localAddr, int localPort, int timeout, Map properties)
            throws IOException {
        logger.info("creating socket from " + localAddr + ":" + localPort + " to: " + destAddr + ":" + destPort);
        IbisSocket s = new PlainTCPSocket(destAddr, destPort, localAddr, localPort, timeout, properties);
        tuneSocket(s);
        return s;
    }
    
    public IbisSocket createClientSocket(InetAddress addr, int port, Map p)
            throws IOException {
        logger.info("creating socket to: " + addr + ":" + port);
        IbisSocket s = new PlainTCPSocket(addr, port, p);
        tuneSocket(s);
        return s;
    }

    public IbisServerSocket createServerSocket(InetSocketAddress addr, int backlog, Map p)
            throws IOException {
        logger.info("creating server socket to: " + addr);

        IbisServerSocket s = new PlainTCPServerSocket(p);
        s.setReceiveBufferSize(0x10000);
        s.bind(addr, backlog);
        return s;
    }

    public IbisSocket createBrokeredSocket(InputStream in, OutputStream out,
            boolean hintIsServer, Map p)
            throws IOException {
        IbisSocket s = null;
        if (hintIsServer) {
            IbisServerSocket server = createServerSocket(
                    new InetSocketAddress(IPUtils.getLocalHostAddress(), 0), 1, p);

            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(out));
            dos.writeUTF(server.getInetAddress().getHostAddress());
            dos.writeInt(server.getLocalPort());
            dos.flush();

            s = (IbisSocket) server.accept();
            tuneSocket(s);
        } else {
	    DataInputStream di = new DataInputStream(new BufferedInputStream(in));
            String addr = di.readUTF();
            int rport = di.readInt();

            InetAddress raddr = null;
            try {
                raddr = InetAddress.getByName(addr);
            } catch(Exception e) {
                throw new Error("EEK, could not create an inet address"
                                    + "from a IP address. This shouldn't happen", e);
            }

            s = createClientSocket(raddr, rport, p);
        }
        return s;
    }
}
