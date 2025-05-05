package client;

import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

import interfaces.*;


public class UserChat extends UnicastRemoteObject implements IUserChat {
    private String userName;
    private IServerChat server;
    private IRoomChat currentRoom;

    private String serverIP;

    private JFrame frame;
    private JTextArea chatArea;
    private JLabel currentRoomLabel;
    private JTextField messageField;
    private JComboBox<String> roomList;
    private JButton joinButton, leaveButton, sendButton, createButton, updateButton;
    
    public UserChat(String userName, String ip) throws RemoteException {
        super();
        this.userName = userName;
        this.serverIP = ip;
        System.out.println("Conectando no servidor ip:" + ip);
        setupGUI();
        connectToServer();
    }
    
    private void setupGUI() {
        frame = new JFrame("Chat - " + userName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLayout(new BorderLayout());
    
        // Painel principal dividido em áreas
        JSplitPane mainSplitPane = new JSplitPane();
        mainSplitPane.setDividerLocation(200);
    
        // --- Painel Esquerdo (Salas) ---
        JPanel roomPanel = new JPanel(new BorderLayout());
        roomPanel.setBorder(BorderFactory.createTitledBorder("Salas de Chat"));
        
        // Componentes de salas
        roomList = new JComboBox<>();
        
        JPanel roomControlsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        roomControlsPanel.add(new JLabel("Sala Atual:"));
        
        currentRoomLabel = new JLabel("--");
        roomControlsPanel.add(currentRoomLabel);
        
        roomControlsPanel.add(new JLabel("Salas Disponíveis:"));
        roomControlsPanel.add(roomList);
        
        JPanel roomButtonsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        joinButton = new JButton("Entrar");
        leaveButton = new JButton("Sair");
        createButton = new JButton("Criar Nova");
        updateButton = new JButton("Atualizar");

        joinButton.addActionListener(e -> joinRoom());
        leaveButton.addActionListener(e -> leaveRoom());
        createButton.addActionListener(e -> createRoom());
        updateButton.addActionListener(e -> updateRoomList());
        
        roomButtonsPanel.add(joinButton);
        roomButtonsPanel.add(leaveButton);
        roomButtonsPanel.add(createButton);
        roomButtonsPanel.add(updateButton);

        roomPanel.add(roomControlsPanel, BorderLayout.NORTH);
        roomPanel.add(roomButtonsPanel, BorderLayout.SOUTH);
    
        // --- Painel Central (Chat) ---
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder("Mensagens"));
        
        // Área de mensagens
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        
        // Área de envio de mensagens
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> sendMessage());
        
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);
    
        mainSplitPane.setLeftComponent(roomPanel);
        mainSplitPane.setRightComponent(chatPanel);
        frame.add(mainSplitPane, BorderLayout.CENTER);
    
        frame.setVisible(true);
    }
    
    
    private void connectToServer() {
        try {
            server = (IServerChat) Naming.lookup("rmi://" + serverIP + ":2020/Servidor");
            updateRoomList(); // RFA 5
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
    
    private void updateRoomList() {
        try {
            ArrayList<String> rooms = server.getRooms();
            roomList.removeAllItems();
            for (String room : rooms) {
                roomList.addItem(room);
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(frame, "Erro ao atualizar lista de salas: " + e.getMessage());
        }
    }
    
    // RFA 6
    private void joinRoom() {
        String selectedRoom = (String) roomList.getSelectedItem();
        if (selectedRoom != null) {
            try {
                currentRoom = (IRoomChat) Naming.lookup("rmi://" + serverIP + ":2020/" + selectedRoom);
                currentRoom.joinRoom(userName, this);
                chatArea.append("Você entrou na sala " + selectedRoom + "\n");
                currentRoomLabel.setText(selectedRoom);
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(frame, e.detail);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage());
            } 
        }
    }

    // RFA 7
    private void createRoom() {
        String roomName = JOptionPane.showInputDialog(frame, "Digite o nome da nova sala:");
        if (roomName != null && !roomName.isEmpty()) {
            try {
                server.createRoom(roomName);
                updateRoomList();
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(frame, e.detail);
            }
        }
    }

    // RFA 8
    private void sendMessage() {
        if (currentRoom != null) {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                try {
                    currentRoom.sendMsg(userName, message);
                    messageField.setText("");
                } catch (RemoteException e) {
                    JOptionPane.showMessageDialog(frame, "Erro ao enviar mensagem: " + e.getMessage());
                }
            }
        }
    } 

    // RFA 9
    public void deliverMsg(String senderName, String msg) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(senderName + ": " + msg + "\n");
        });
    }

    // RFA 11
    private void leaveRoom() {
        if (currentRoom != null) {
            try {
                currentRoom.leaveRoom(userName);
                chatArea.append("Você saiu da sala\n");
                currentRoom = null;
                currentRoomLabel.setText("--");
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(frame, "Erro ao sair da sala: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        String userName = JOptionPane.showInputDialog("Digite seu nome de usuário:");
        if (userName != null && !userName.isEmpty()) {
            try {
                new UserChat(userName, args[0]);
            } catch (RemoteException e) {
                System.err.println("Erro ao criar cliente: " + e.getMessage());
            }
        }
    }
}