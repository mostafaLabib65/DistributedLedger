public class ClientMain {

    public static void main(String[] args){
//        Client client = new Client(Integer.parseInt(args[0]));
        Client client = new Client(Integer.parseInt(args[0]), 4, "127.0.0.1", "127.0.0.1");
    }
}
