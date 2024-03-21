package ru.java_two.network;

import java.net.ServerSocket;
import java.net.Socket;

public interface ServerSocketThreadListener {//try
    void onServerStart(ru.java_two.network.ServerSocketThread thread);
    void onServerStop(ru.java_two.network.ServerSocketThread thread);
    void onServerSocketCreated(ru.java_two.network.ServerSocketThread thread, ServerSocket server);
    void onServerTimeout(ru.java_two.network.ServerSocketThread thread, ServerSocket server);
    void onServerException(ru.java_two.network.ServerSocketThread thread, Throwable exception);
    void onSocketAccepted(ru.java_two.network.ServerSocketThread thread, ServerSocket server, Socket socket);
}
