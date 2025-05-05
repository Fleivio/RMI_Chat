import java.rmi.*;
import java.rmi.server.*;
import java.util.HashMap;
import java.util.Map;

public class RoomChat extends UnicastRemoteObject implements IRoomChat {
    private String roomName;
    private Map<String, IUserChat> userList;
    
    public RoomChat(String roomName) throws RemoteException {
        super();
        this.roomName = roomName;
        this.userList = new HashMap<String, IUserChat>();
    }
    
    // RFA 10
    public void sendMsg(String usrName, String msg) throws RemoteException {
        for (IUserChat user : userList.values()) {
            user.deliverMsg(usrName, msg);
        }
    }
    
    public void joinRoom(String userName, IUserChat user) throws RemoteException {
        if (!userList.containsKey(userName)) {
            userList.put(userName, user);
            sendMsg("Sistema", userName + " entrou na sala.");
        } else {
            throw new RemoteException("Erro: O nome de usuário '" + userName + "' já está em uso nesta sala.");
        }
    }
    
    public void leaveRoom(String usrName) throws RemoteException {
        if (userList.containsKey(usrName)) {
            userList.remove(usrName);
            sendMsg("Sistema", usrName + " saiu da sala.");
        }
    }
    
    public String getRoomName() throws RemoteException {
        return roomName;
    }
    
    public void closeRoom() throws RemoteException {
        if (!userList.isEmpty()) {
            for (Map.Entry<String, IUserChat> entry : userList.entrySet()) {
                try {
                    entry.getValue().deliverMsg("Servidor", "Sala fechada pelo servidor.");
                } catch (RemoteException e) {
                    System.err.println("Erro ao notificar usuário " + entry.getKey() + " sobre fechamento da sala");
                }
            }
            userList.clear();
        }
    }
}