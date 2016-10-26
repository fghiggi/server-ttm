
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by slave00 on 26/10/16.
 */
public class Servidor {
    public static List<ClienteConectado> clientes;
    private int porta;
    ServerSocket serverSocket;

    public Servidor(int porta) {
        this.porta = porta;
        setup();
    }

    private void setup() {
        try {
            serverSocket = new ServerSocket(porta);
            clientes = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void aguardarClientes() {
        Socket socket;

        try {
            while (true) {
                socket = serverSocket.accept();
                ClienteConectado cli = new ClienteConectado(socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
