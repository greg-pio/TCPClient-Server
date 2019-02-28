import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;

public class Host {

    private int port;
    private int num;
    private String dir;
    private Thread sl;
    private Thread cl;
    private ServerSocket ss;
    private DataInputStream dis;
    private DataInputStream dis2;
    private FileOutputStream fos;
    private DataOutputStream dos;
    private FileInputStream fis;

    public Host (int num, int port) {
        this.num = num;
        this.port = port;
        dir = Paths.get(".").toAbsolutePath().normalize().toString() + "/" + "TORrent_" + num;
        new File(dir).mkdir();
    }

    public void startHost() {
        System.out.println("New host is online... host id: " + num + ", host port: " + port);
        System.out.println("Working directory for this host is: " + dir);

        sl = new Thread(() -> {
            try {
                slStart(port);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        sl.start();

        System.out.println("Type 'help' for available commands.");

        Scanner scan = new Scanner(System.in);
        String command;
        while(true) {
            System.out.println("Please type in new command...");
            command = scan.nextLine();
            executeCom(command);
        }
    }

    public void executeCom(String command) {
        String[] commandParts = command.split(" ");
        switch (commandParts[0].toLowerCase()) {
            case "help":
                printHelp();
            break;

            case "push":
                cl = new Thread(() -> {
                    try {
                        push(commandParts[1], commandParts[2]);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                cl.start();
            break;

            case "pull":
                cl = new Thread(() -> {
                    try {
                        pull(commandParts[1], commandParts[2]);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                cl.start();
            break;

            case "exit":
                System.out.println("Bye, bye...");
                System.exit(0);
            break;
            default:
                System.out.println("Unknown command...");
        }
    }

    public void printHelp() {
        System.out.println("Available commands:");
        System.out.println("'help' - prints available commands,");
        System.out.println("'push' - uploads a file to another host, usage: push target_host_port filename");
        System.out.println("'pull' - downloads a file from another host, usage: pull target_host_port filename");
        System.out.println("'exit' - ends the program");

    }

    public void push(String host_port, String filename) {

        int port = Integer.parseInt(host_port);
        String ip = "127.0.0.1";

        try {
            Socket s = new Socket(ip, port);

            System.out.println("Sending new file: " + filename + ".");

            dos = new DataOutputStream(s.getOutputStream());

            byte b[] = new byte[1024];

            String filepath = dir + "/" + filename;
            dos.writeUTF("push");
            dos.writeUTF(filename);

            fis = new FileInputStream(filepath);
            int count;
            while ((count = fis.read(b, 0, b.length)) > 0)
                dos.write(b, 0, count);
            dos.close();
            fis.close();

        } catch (Exception e) {}
    }

    public void pull(String host_id, String filename) {

        int port = Integer.parseInt(host_id);
        String ip = "127.0.0.1";

        try {
            Socket s = new Socket(ip, port);

            System.out.println("Requesting new file: " + filename + ".");

            dos = new DataOutputStream(s.getOutputStream());

            dos.writeUTF("pull");
            dos.writeUTF(filename);

            dis2 = new DataInputStream(s.getInputStream());

            String filepath = dir + "/" + filename;

            byte[] b = new byte[1024];
            fos = new FileOutputStream(filepath);
            int count;
            while ((count = dis2.read(b, 0, b.length)) > 0)
                fos.write(b, 0, count);
            dis2.close();
            fos.close();

        } catch (Exception e) {}
    }

    private void slStart(int port) throws Exception {
        System.out.println("Server running on port: " + port);
        ss = new ServerSocket(port);
        do {
            Socket clientSocket;
            try {
                clientSocket = ss.accept();
                dis = new DataInputStream(clientSocket.getInputStream());
                String command = dis.readUTF();

                switch (command) {

                    case "push" :
                        String filename = dis.readUTF();
                        byte[] b = new byte[1024];
                        String filepath = dir + "/" + filename;
                        fos = new FileOutputStream(filepath);
                        int count;
                        while ((count = dis.read(b, 0, b.length)) > 0)
                            fos.write(b, 0, count);
                        dis.close();
                        fos.close();
                        clientSocket.close();
                        System.out.println("Received new file: " + filename);
                        break;

                    case "pull" :
                        String filename2 = dis.readUTF();
                        dos = new DataOutputStream(clientSocket.getOutputStream());

                        byte c[] = new byte[1024];

                        String filepath2 = dir + "/" + filename2;

                        fis = new FileInputStream(filepath2);
                        int count2;
                        while ((count2 = fis.read(c, 0, c.length)) > 0)
                            dos.write(c, 0, count2);
                        dos.close();
                        fis.close();
                        clientSocket.close();
                        System.out.println("Sent new fie: " + filename2);
                        break;
                }
            } catch (Exception e) {
                return;
            }
        } while (true);
    }
}
