public class Starter {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Wrong number of arguments... usage: java Starter host_number port");
            return;
        }

        int num = Integer.parseInt(args[0]);

        int p = Integer.parseInt(args[1]);
        int port;

        if (p >= 10000 && p <= 65000)
            port = p;
        else {
            System.out.println("Wrong port number... choose number between 10000 and 65000");
            return;
        }

        Host host = new Host(num, port);
        host.startHost();

    }
}
