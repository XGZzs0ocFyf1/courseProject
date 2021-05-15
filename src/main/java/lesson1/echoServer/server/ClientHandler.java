package lesson1.echoServer.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {

    //Это сокет подключенного клиента
    private final Socket socket;


    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())
        ) {
            while (true){
                String command = in.readUTF();

                //логируем команду
                System.out.println(command);

                if ("upload".equalsIgnoreCase(command)){
                    upload(out, in);
                }

                if ("download".equalsIgnoreCase(command)){
                    download(out, in);
                }

                //некий произвольный выход по команде "exit"
                if ("exit".equals(command)){
                    System.out.printf("Client %s disconnected correctly \n", socket.getInetAddress());

                    //Отправляем обратно клиенту тикет что мы закрываем коннект
                    out.writeUTF("DONE");

                    //отключаем клиента
                    disconnected();
                    break;
                }

                //посылаем команду обратно
                out.writeUTF(command);

            }

        }catch (SocketException socketException){
            System.out.printf("Client %s disconnected \n", socket.getInetAddress());
        }


        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void upload(DataOutputStream out, DataInputStream in) throws IOException {
        try{
            File file = new File("server/"+ in.readUTF()); // читаем имя файла
            if (!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            long size = in.readLong();
            byte[] buffer = new byte[8 * 1024];
            for (int i = 0; i < ((size+ (8 * 1024  - 1)) / (8*1024) ); i++) {
                int read = in.read(buffer);
                fos.write(buffer, 0, read);

            }
            fos.close();
            out.writeUTF("OK");
        }catch (Exception e){
           out.writeUTF("WRONG");
           e.printStackTrace();
        }
    }

    private void download(DataOutputStream out, DataInputStream in) {

    }



    /**
     * just close socket here
     */
    private void disconnected() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
     //   File file = new File("\\server\\"+"qwe.txt");
        File file = new File("server/1.png");
        try {

            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
