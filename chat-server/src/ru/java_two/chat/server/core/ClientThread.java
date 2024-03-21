package ru.java_two.chat.server.core;

import ru.java_two.chat.common.Library;
import ru.java_two.chat.network.SocketThread;
import ru.java_two.chat.network.SocketThreadListener;

import java.net.Socket;

public class ClientThread extends SocketThread {
    private String nickname;
    private boolean isAuthorized;
    private boolean isReconnecting;

    public ClientThread(SocketThreadListener listener, String name, Socket socket) {
        super(listener, name, socket);
        //try
    }

    public String getNickname() { //try
        return nickname;
    }

    public boolean isAuthorized() { //try
        return isAuthorized;
    }

    public boolean isReconnecting() {//try
        return isReconnecting;
    }

    void reconnect() { // try
        isReconnecting = true;
        close();
    }

    void authAccept(String nickname) { // authAccept надо сверить с ваньком
        isAuthorized = true;
        this.nickname = nickname;
        sendMessage(Library.getAuthAccept(nickname));
    }

    void authFail() {//rty
        sendMessage(Library.getAuthDenied());
        close();
    }
    void msgFormatError(String msg) {//try
        sendMessage(Library.getMsgFormatError(msg));
        close();
    }
}
