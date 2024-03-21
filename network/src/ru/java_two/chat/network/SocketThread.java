package ru.java_two.chat.network;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketThread extends Thread {

    private final SocketThreadListener listener;
    private final Socket socket;
    DataInputStream in;//get
    private DataOutputStream out;

    public SocketThread(SocketThreadListener listener, String name, Socket socket) {
        super((name));
        this.socket = socket;
        this.listener = listener;
        start();
    }//try

    @Override
    public void run() {
        try {
            listener.onSocketStart(this, socket);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            listener.onSocketReady(this, socket);
            while (!isInterrupted()) {
                String msg = in.readUTF();
                listener.onReceiveString(this, socket, msg);
            }
        }catch (IOException e) {
            listener.onSocketException(this, e);
        }finally {
            try {
                socket.close();
            }catch (IOException e) {
                listener.onSocketException(this, e);
            }
            listener.onSocketStop(this);
        }
    }

    public synchronized boolean sendMessage(String msg) {//try
        try {
            out.writeUTF(msg);
            out.flush();
            return true;
        }catch (IOException e) {
            listener.onSocketException(this, e);
            close();
            return  false;
        }
    }

    public synchronized void close() {//try
        interrupt();
        try {
            in.close();
            socket.close();
        }catch (IOException e ) {
            listener.onSocketException(this, e);
        }
    }
}
