/**
 * Created by slave00 on 26/10/16.
 */
public class Init {
    public static void main(String[] args) {
        Servidor server = new Servidor(8088);

        System.out.println("Aguardando clientes");

        server.aguardarClientes();
    }
}
