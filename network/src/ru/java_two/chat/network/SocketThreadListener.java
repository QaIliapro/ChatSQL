package ru.java_two.network;

import java.net.Socket;

public interface SocketThreadListener {//try

    void onSocketStart(ru.java_two.network.SocketThread thread, Socket socket);
    void onSocketStop(ru.java_two.network.SocketThread thread);

    void onSocketReady(ru.java_two.network.SocketThread thread, Socket socket);
    void onReceiveString(ru.java_two.network.SocketThread thread, Socket socket, String msg);

    void onSocketException(ru.java_two.network.SocketThread thread, Exception exception);
}
