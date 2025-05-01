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

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JComboBox<String> roomList;
    private JButton joinButton, leaveButton, sendButton, createButton, updateButton;
    
    public UserChat(String userName) throws RemoteException {
        super();
        this.userName = userName;
        setupGUI();
        connectToServer();
    }
    
    private void setupGUI() {
        frame = new JFrame("Chat - " + userName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4));
        
        roomList = new JComboBox<>();
        joinButton = new JButton("Entrar");
        leaveButton = new JButton("Sair");
        sendButton = new JButton("Enviar");
        createButton = new JButton("Criar Sala");
        updateButton = new JButton("Atualizar");
        
        buttonPanel.add(roomList);
        buttonPanel.add(joinButton);
        buttonPanel.add(leaveButton);
        buttonPanel.add(sendButton);
        buttonPanel.add(createButton);
        buttonPanel.add(updateButton);
        
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        
        joinButton.addActionListener(e -> joinRoom());
        leaveButton.addActionListener(e -> leaveRoom());
        sendButton.addActionListener(e -> sendMessage());
        createButton.addActionListener(e -> createRoom());
        updateButton.addActionListener(e -> updateRoomList());
        
        frame.setVisible(true);
    }
    
    private void connectToServer() {
        try {
            server = (IServerChat) Naming.lookup("rmi://localhost:2020/Servidor");
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
                currentRoom = (IRoomChat) Naming.lookup("rmi://localhost:2020/" + selectedRoom);
                currentRoom.joinRoom(userName, this);
                chatArea.append("Você entrou na sala " + selectedRoom + "\n");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Erro ao entrar na sala: " + e.getMessage());
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
                JOptionPane.showMessageDialog(frame, "Erro ao criar sala: " + e.getMessage());
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
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(frame, "Erro ao sair da sala: " + e.getMessage());
            }
        }
    }
    

    


    
    public static void main(String[] args) {
        String userName = JOptionPane.showInputDialog("Digite seu nome de usuário:");
        if (userName != null && !userName.isEmpty()) {
            try {
                new UserChat(userName);
            } catch (RemoteException e) {
                System.err.println("Erro ao criar cliente: " + e.getMessage());
            }
        }
    }
}