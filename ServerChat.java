import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;

public class ServerChat extends UnicastRemoteObject implements IServerChat {
    private Map<String, IRoomChat> roomList;
    private static Registry registry;

    private JFrame frame;
    private JTextArea logArea;
    private JComboBox<String> roomComboBox;
    
    public ServerChat() throws RemoteException {
        super();
        roomList = new HashMap<>();
        setupGUI();
    }
    
    private void setupGUI() {
        frame = new JFrame("Servidor de Chat - Controle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Área de log
        logArea = new JTextArea();
        logArea.setEditable(false);
        mainPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        // Painel de controle
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        // Seletor de salas
        roomComboBox = new JComboBox<>();
        controlPanel.add(new JLabel("Salas:"));
        controlPanel.add(roomComboBox);
        
        // Botão para fechar sala
        JButton closeButton = new JButton("Fechar Sala");
        closeButton.addActionListener(e -> closeSelectedRoom());
        controlPanel.add(closeButton);
        
        // Botão para atualizar lista
        JButton refreshButton = new JButton("Atualizar");
        refreshButton.addActionListener(e -> updateRoomList());
        controlPanel.add(refreshButton);
        
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        frame.add(mainPanel);
        frame.setVisible(true);
        
        logMessage("Servidor iniciado. Aguardando conexões...");
    }
    
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
        });
    }
    
    private void updateRoomList() {
        SwingUtilities.invokeLater(() -> {
            roomComboBox.removeAllItems();
            for (String room : roomList.keySet()) {
                roomComboBox.addItem(room);
            }
        });
    }
    
    private void closeSelectedRoom() {
        String selectedRoom = (String) roomComboBox.getSelectedItem();
        if (selectedRoom != null && roomList.containsKey(selectedRoom)) {
            try {
                IRoomChat room = roomList.get(selectedRoom);
                room.closeRoom();
                roomList.remove(selectedRoom);
                registry.unbind(selectedRoom);
                updateRoomList();

                logMessage("Sala " + selectedRoom + " fechada com sucesso.");
            } catch (Exception e) {
                logMessage("Erro ao fechar sala " + selectedRoom + ": " + e.getMessage());
            }
        } else {
            logMessage("Nenhuma sala selecionada ou sala não encontrada.");
        }
    }
    
    public ArrayList<String> getRooms() throws RemoteException {
        return new ArrayList<>(roomList.keySet());
    }

    public void createRoom(String roomName) throws RemoteException {
        if (!roomList.containsKey(roomName)) {
            try {
                IRoomChat room = new RoomChat(roomName);
                roomList.put(roomName, room);
                registry.rebind(roomName, room);
                updateRoomList();
                System.out.println("Sala " + roomName + " criada com sucesso.");
            } catch (Exception e) {
                System.err.println("Erro no URL da sala: " + e.getMessage());
                throw new RemoteException("Erro ao criar sala: URL inválido", e);
            }
        } else {
            throw new RemoteException("Erro: Já existe uma sala chamada '" + roomName + "' no servidor.");
        }
    }
    
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", args[0]);
            registry = LocateRegistry.createRegistry(2020);
            IServerChat server = new ServerChat();
            registry.rebind("Servidor", server);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}