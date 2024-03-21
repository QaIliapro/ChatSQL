package ru.java_two.chat.client;

import ru.java_two.chat.comman.Library;
import ru.java_two.network.SocketThread;
import ru.java_two.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 300;

    private final JTextArea log = new JTextArea();
    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));

    private final JTextField tfIPAddress = new JTextField("127.0.0.1");
    private final JTextField tfPort = new JTextField("8000");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top");
    private final JTextField tfLogin = new JTextField("Ilia");
    private final JPasswordField tfPassword = new JPasswordField("12345");
    private final JButton btnLogin =new JButton("Login");

    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("<html><b>Disconnect</b></html>");
    private final JTextField tfMessage = new JTextField();
    private final JButton btnSend = new JButton("Send");

    private final JList<String> userList = new JList<>();
    private boolean shownIoErrors = false;
    private SocketThread socketThread;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private final String WINDOW_TITLE = "Chat";

    public static void main(String[] args)  {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
    }

    private ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH,HEIGHT);
        JScrollPane scrollLog = new JScrollPane(log);
        JScrollPane scrollUser = new JScrollPane(userList);
        scrollUser.setPreferredSize(new Dimension(150, 0));
        log.setEditable(false);
        log.setLineWrap(true);

        String[] users = {"user1","user2", "user3", "user4", "user5",
        };

        userList.setListData(users);


        cbAlwaysOnTop.addActionListener(this); // нет у Ник
        btnSend.addActionListener(this);
        tfMessage.addActionListener(this);
        btnLogin.addActionListener(this);
        btnDisconnect.addActionListener(this);
        panelBottom.setVisible(false);

        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(cbAlwaysOnTop); //верный блок
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnLogin);

        panelBottom.add(btnDisconnect, BorderLayout.WEST);
        panelBottom.add(tfMessage, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);

        add(scrollLog, BorderLayout.CENTER);
        add(scrollUser, BorderLayout.EAST); //верный блок
        add(panelTop, BorderLayout.NORTH);
        add(panelBottom, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) { //верно
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (src == btnSend || src == tfMessage) {
            sendMessage();
        } else if (src == btnLogin) {
            connect();
        }else if (src == btnDisconnect) {
            socketThread.close();
        }else {
            throw new RuntimeException("Unknown source" + src);
        }
    }

    private void connect() { //верно
        try {
            Socket socket = new Socket(tfIPAddress.getText(),Integer.parseInt(tfPort.getText()));
            socketThread = new SocketThread(this, "Client", socket);
        }catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    private void sendMessage() {
        String msg = tfMessage.getText();
        // String username = tfLogin.getText();
        if ("".equals(msg)) return;
        tfMessage.setText(null);
        tfMessage.grabFocus();
        socketThread.sendMessage(Library.getTypeClientBroadcast(msg));// msg ебёт мозги
        // wrtMsgToLogfile(msg, username);
    }

    private void wrtMsgToLogfile(String msg, String username) { //верно
        try (FileWriter out = new FileWriter("log.txt",true )) {
            out.write(username + ": " + msg + "\n");
            out.flush();
        } catch (IOException e) {
            if (!shownIoErrors) {
                shownIoErrors = true;
                showException(Thread.currentThread(), e);
            }
        }
    }

    private void putLog (String msg) { //try
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    private void  sender () { //try
        String msg = tfMessage.getText();
        tfMessage.setText("");

        log.append(msg + "\n");
    }

    @Override //try
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        showException(t, e);
        System.exit(1);
    }

    private void showException (Thread t, Throwable e) { //try
        String msg;
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        if (stackTraceElements.length == 0)
            msg = "Empty Stacktrace";
        else {
            msg = String.format(" Exception in \"%s\" %s\n\tat $s \n ", t.getName(), e.getClass().getCanonicalName(),
                    e.getMessage(),stackTraceElements[0]);
            JOptionPane.showMessageDialog(this, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        }
        JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
    }

    private void handleMessage(String msg){
        String[] arr = msg.split(Library.DELIMITER);
        String messageType = arr[0];
        switch (messageType){
            case Library.AUTH_ACCEPT:
                setTitle(WINDOW_TITLE + " entered with nickname: " + arr[1]);
                break;
            case Library.AUTH_DENIED:
                putLog("Authorisation failed ");
                break;
            case Library.MSG_FORMAT_ERROR:
                putLog(msg);
                socketThread.close();
                break;
            case Library.TYPE_BROADCAST:
                putLog(DATE_FORMAT.format(Long.parseLong(arr[1])) + arr[2] + ": " + arr[3]);
                break;
            case Library.USER_LIST:
                msg = msg.substring(Library.USER_LIST.length() + Library.DELIMITER.length());
                String[] usersArray  = msg.split(Library.DELIMITER);
                Arrays.sort(usersArray);
                userList.setListData(usersArray);
                break;
            default:
                throw  new RuntimeException("Unknown msg type: " + msg);
        }
    }

    @Override//try
    public void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Start");
    }

    @Override
    public void onSocketStop(SocketThread thread) {

        setTitle(WINDOW_TITLE);
        userList.setListData(new String[0]);

        panelBottom.setVisible(false);
        panelTop.setVisible(true);
    }

    @Override //try
    public void onSocketReady(SocketThread thread, Socket socket) {
        panelBottom.setVisible(true);
        panelTop.setVisible(false);
        String login = tfLogin.getText();
        String password = new String(tfPassword.getPassword());
        thread.sendMessage(Library.getAuthRequest(login, password));
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        handleMessage(msg);
    }//try

    @Override //try
    public void onSocketException(SocketThread thread, Exception exception) {
        showException(thread,exception);
    }
}
