package ru.java_two.chat.server.core;

import ru.java_two.chat.common.Library;
import ru.java_two.chat.network.ServerSocketThread;
import ru.java_two.chat.network.ServerSocketThreadListener;
import ru.java_two.chat.network.SocketThread;
import ru.java_two.chat.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private ServerSocketThread thread;
    private final ChatServerListener listener;
    private final Vector<SocketThread> clients;


    public ChatServer(ChatServerListener listener) { //try
        this.listener = listener;
        this.clients = new Vector<>();
    }

    public void start(int port) { //try
        if (thread != null && thread.isAlive()) {
            putLog("Server already started");
        }else {
            thread = new ServerSocketThread(this,"Thread of server", port, 2000);
        }
    }

    public void stop() { //try но у меня не стоял ! в (!thread)
        if (thread == null || !thread.isAlive()) {
            putLog("Server is not  running");
        }else {
            thread.interrupt();
        }
    }

    private void putLog(String msg) { //try
        msg = DATE_FORMAT.format(System.currentTimeMillis()) +
                Thread.currentThread().getName() + ": " + msg;
        listener.onChatServerMassage(msg);
    }

    /**
     *
     * Сервер
     */
    @Override //try
    public void onServerStart(ServerSocketThread thread) {
        putLog("Server thread started");
        SqlClient.connect();
    }

    @Override //try
    public void onServerStop(ServerSocketThread thread) {
        putLog("Server thread stopped");
        SqlClient.disconnect();
        //добавил не было
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).close();
        }
    }

    @Override //try
    public void onServerSocketCreated(ServerSocketThread thread, ServerSocket server) {
        putLog("Server timeout");
    }

    @Override //try
    public void onServerTimeout(ServerSocketThread thread, ServerSocket server) {

    }

    @Override //try
    public void onServerException(ServerSocketThread thread, Throwable exception) {
        exception.printStackTrace();
    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket) {
        putLog("Client connect");
        String name = "SocketThread " + socket.getInetAddress() + ":" + socket.getPort();
        new ClientThread(this, name, socket);
    }

    /**
     * сокеты
     */


    @Override //try
    public synchronized void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Socket created");
    }

    @Override// Проверять
    public synchronized void onSocketStop(SocketThread thread) {
        putLog("Socket stopped");
        clients.remove(thread);
        ClientThread clientThread = (ClientThread) thread;
        if(clientThread.isAuthorized() && !clientThread.isReconnecting()){
            sendToAllAuthorizedClients(Library.getTypeBroadcast("Server",
                    clientThread.getNickname() + " disconnected"));
        }
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));
    }

    @Override
    public synchronized void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Socket ready");
        clients.add(thread);
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));
    }

    @Override //Try
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized())
            handleAuthMessage(client, msg);
        else
            handleNonAuthMessage(client, msg);
//        for (int i = 0; i < clients.size(); i++) {
//            SocketThread client = clients.get(i);
//            client.sendMessage(message);
//        }
    }

    public void handleNonAuthMessage(ClientThread client,String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        if (arr.length != 3 || !arr[0].equals(Library.AUTH_REQUEST)) {
            client.msgFormatError(msg);
            return;
        }
        String login = arr[1];
        String password = arr[2];
        String nickname = SqlClient.getNickname(login, password);
        if (nickname == null) {
            putLog("Invalid login attempt" + login);
            client.authFail();
            return;
        }else {
            ClientThread oldClient = findClientByNickname(nickname);
            client.authAccept(nickname);//problema (В лабрари)
            if (oldClient == null) {
                sendToAllAuthorizedClients(Library.getTypeBroadcast("Server ", nickname + " connected"));
            }else {
                oldClient.reconnect();
                clients.remove(oldClient);
            }
        }
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));
    }

    private void handleAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Library.TYPE_BROADCAST_CLIENT:
                sendToAllAuthorizedClients(Library.getTypeBroadcast(
                        client.getNickname(), msg));
                break;
            default:
                client.msgFormatError(msg);
        }
    }


    public void sendToAllAuthorizedClients(String msg) { //try
        for (int i = 0; i < clients.size(); i++) {
            ClientThread recipient = (ClientThread)clients.get(i);
            if (!recipient.isAuthorized())continue;
            recipient.sendMessage(msg);
        }
    }

    private synchronized ClientThread findClientByNickname(String nickname) { // try
        for (int i = 0; i < clients.size(); i++) {
            ClientThread clientThread = (ClientThread) clients.get(i);
            if(!clientThread.isAuthorized()) continue;
            if(clientThread.getNickname().equals(nickname))
                return clientThread;// У ванька только клиент
        }
        return null;
    }

    private String getUsers() { //try
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) {
            ClientThread clientThread = (ClientThread) clients.get(i);
            if (!clientThread.isAuthorized()) continue;
            stringBuilder.append(clientThread.getNickname()).append(Library.DELIMITER);
        }
        return  stringBuilder.toString();
    }

    public synchronized void onSocketException(SocketThread thread, Exception exception) {
        exception.printStackTrace();

        //try
    }

}
