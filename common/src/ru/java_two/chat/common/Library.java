package ru.java_two.chat.comman;

public class Library {
    public static final String DELIMITER = "±";
    public static final String AUTH_REQUEST = "/auth_request";
    public static final String AUTH_ACCEPT = "/auth_accept";
    public static final String AUTH_DENIED = "/aut_denied";
    public static final String MSG_FORMAT_ERROR = "/msg_format_error";
    public static final String TYPE_BROADCAST = "/bcast";
    public static final String USER_LIST = "/user_list";
    public static final String TYPE_BROADCAST_CLIENT = "/clien_bcast";

    public static String getAuthRequest(String login, String password) { //try
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept (String nickname) {//try
        return AUTH_ACCEPT + DELIMITER + nickname;
    }

    public static String getAuthDenied() {//try
        return AUTH_DENIED;
    }

    public static String getMsgFormatError(String msg) {//try
        return MSG_FORMAT_ERROR + DELIMITER + msg;
    }

    public static String getTypeBroadcast(String src, String msg) {//try
        return TYPE_BROADCAST + DELIMITER + System.currentTimeMillis() + DELIMITER + src + DELIMITER + msg;
    }

    public static String getUserList(String users) {//try
        return USER_LIST + DELIMITER + users;
    }

    public static String getTypeClientBroadcast(String msg) { // not see
        return TYPE_BROADCAST_CLIENT + DELIMITER + msg; //getTypeClientBroadcast
    }

}
