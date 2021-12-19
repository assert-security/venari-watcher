package com.assertsecurity.venariwatcher.rmiserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.server.ObjID;
import java.rmi.server.UID;
import java.util.Arrays;
import java.util.UUID;

import sun.rmi.transport.TransportConstants;

import javax.net.ServerSocketFactory;
import javax.xml.crypto.MarshalException;

import com.assertsecurity.venariwatcher.utils.DateTimeUtils;
import com.assertsecurity.venariwatcher.utils.PayloadMapper;

@SuppressWarnings ( {
    "restriction"
} )
public class RmiServer implements Runnable {
    
    private int _port;
    private String _ipAddress;
    private ServerSocket _ss;
    private Object _waitLock = new Object();
    private boolean _exit;
    
    public RmiServer (String ipAddress, int port ) throws IOException {
        _port = port;
        _ipAddress = ipAddress;
        _ss = ServerSocketFactory.getDefault().createServerSocket(this._port);
    }

    @Override
    public void run () {
        try {
			System.out.println(DateTimeUtils.getDateTimeString() + " [RMI Server] >> Listening on " + _ipAddress + ":" + _port);
            Socket s = null;
            try {
                while ( !this._exit && ( s = this._ss.accept() ) != null ) {
                    try {
                        s.setSoTimeout(5000);
                        InetSocketAddress remote = (InetSocketAddress) s.getRemoteSocketAddress();
                        System.out.println(DateTimeUtils.getDateTimeString() + " [RMI Server]  >> Have connection from " + remote);

                        InputStream is = s.getInputStream();
                        InputStream bufIn = is.markSupported() ? is : new BufferedInputStream(is);

                        // Read magic (or HTTP wrapper)
                        bufIn.mark(4);
                        try ( DataInputStream in = new DataInputStream(bufIn) ) {
                            int magic = in.readInt();

                            short version = in.readShort();
                            if ( magic != TransportConstants.Magic || version != TransportConstants.Version ) {
                                s.close();
                                continue;
                            }

                            OutputStream sockOut = s.getOutputStream();
                            BufferedOutputStream bufOut = new BufferedOutputStream(sockOut);
                            try ( DataOutputStream out = new DataOutputStream(bufOut) ) {

                                byte protocol = in.readByte();
                                switch ( protocol ) {
                                case TransportConstants.StreamProtocol:
                                    out.writeByte(TransportConstants.ProtocolAck);
                                    if ( remote.getHostName() != null ) {
                                        out.writeUTF(remote.getHostName());
                                    }
                                    else {
                                        out.writeUTF(remote.getAddress().toString());
                                    }
                                    out.writeInt(remote.getPort());
                                    out.flush();
                                    in.readUTF();
                                    in.readInt();
                                case TransportConstants.SingleOpProtocol:
                                    doMessage(s, in, out);
                                    break;
                                default:
                                case TransportConstants.MultiplexProtocol:
                                    System.out.println(DateTimeUtils.getDateTimeString() + " [RMI Server]  >> Unsupported protocol");
                                    s.close();
                                    continue;
                                }

                                bufOut.flush();
                                out.flush();
                            }
                        }
                    }
                    catch ( InterruptedException e ) {
                        return;
                    }
                    catch ( Exception e ) {
                        e.printStackTrace(System.err);
                    }
                    finally {
                        System.out.println(DateTimeUtils.getDateTimeString() + " [RMI Server]  >> Closing connection");
                        s.close();
                    }

                }

            }
            finally {
                if ( s != null ) {
                    s.close();
                }
                if ( this._ss != null ) {
                    this._ss.close();
                }
            }

        }
        catch ( SocketException e ) {
            return;
        }
        catch ( Exception e ) {
            e.printStackTrace(System.err);
        }
    }

    private void doMessage ( Socket s, DataInputStream in, DataOutputStream out ) throws Exception {
        System.out.println(DateTimeUtils.getDateTimeString() + " [RMI Server]  >> Reading message...");

        int op = in.read();

        switch ( op ) {
        case TransportConstants.Call:
            // service incoming RMI call
            doCall(in, out);
            break;

        case TransportConstants.Ping:
            // send ack for ping
            out.writeByte(TransportConstants.PingAck);
            break;

        case TransportConstants.DGCAck:
            UID.read(in);
            break;

        default:
            throw new IOException(DateTimeUtils.getDateTimeString() + " [RMI Server]  >> unknown transport op " + op);
        }

        s.close();
    }

    private void doCall ( DataInputStream in, DataOutputStream out ) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(in) {

            @Override
            protected Class<?> resolveClass ( ObjectStreamClass desc ) throws IOException, ClassNotFoundException {
                if ( "[Ljava.rmi.jndi.ObjID;".equals(desc.getName()) ) {
                    return ObjID[].class;
                }
                else if ( "java.rmi.jndi.ObjID".equals(desc.getName()) ) {
                    return ObjID.class;
                }
                else if ( "java.rmi.jndi.UID".equals(desc.getName()) ) {
                    return UID.class;
                }
                else if ( "java.lang.String".equals(desc.getName()) ) {
                    return String.class;
                }
                throw new IOException(DateTimeUtils.getDateTimeString() + " [RMI Server]  >> Not allowed to read object");
            }
        };

        ObjID read;
        try {
            read = ObjID.read(ois);
        }
        catch ( IOException e ) {
            throw new MarshalException(DateTimeUtils.getDateTimeString() + " [RMI Server]  >> unable to read objID", e);
        }

        if ( read.hashCode() == 2 ) {
            // DGC
            handleDGC(ois);
        }
        else if ( read.hashCode() == 0 ) {
            if ( handleRMI(ois, out) ) {
                synchronized ( this._waitLock ) {
                    this._waitLock.notifyAll();
                }
                return;
            }
        }

    }

    /**
     * @param ois
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void handleDGC ( ObjectInputStream ois ) throws IOException, ClassNotFoundException {
        ois.readInt(); // method
        ois.readLong(); // hash
        System.out.println(DateTimeUtils.getDateTimeString() + " [RMI Server]  >> Is DGC call for " + Arrays.toString((ObjID[]) ois.readObject()));
    }

    /**
     * @param ois
     * @param out
     * @throws IOException
     * @throws ClassNotFoundException
//     * @throws NamingException
     */
    private boolean handleRMI ( ObjectInputStream ois, DataOutputStream out ) throws Exception {
        int method = ois.readInt(); // method
        ois.readLong(); // hash

        if ( method != 2 ) { // lookup
            return false;
        }

        String object = (String) ois.readObject();

        try
        {
            UUID id = UUID.fromString(object);
            PayloadMapper.Set(id);
            System.out.println(DateTimeUtils.getDateTimeString() + " [RMI Server]  >> Payload received " + id.toString());
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }

        return true;
    }

}