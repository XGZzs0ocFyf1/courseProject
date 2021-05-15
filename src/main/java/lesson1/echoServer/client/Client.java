package lesson1.echoServer.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;


/**
 * Кастомный Swing клиент для эхо сервера
 *
 * Client commands: upload  filename - выгрузка файла по имени на сервер
 * download - загрузка файла по имени с сервера
 */
public class Client extends JFrame {

    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;


    public Client()  throws IOException {

        //инициализация
        socket = new Socket("localhost",6789);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        //создаем формочку
        setSize(300, 300);
        JPanel panel = new JPanel(new GridLayout(2,1));

        JTextField textField = new JTextField();
        JButton bntSend = new JButton("Send");

        bntSend.addActionListener( ae -> {
            var cmds = textField.getText().split(" ");

            //since java13 modern switch
            //fixme: внутри свитча стоит ловить эксепшены или это лучше делать до него?
            switch(cmds[0].toLowerCase()){
                case "upload" -> sendFile(cmds[1]);
                case "download" -> getFile(cmds[1]);
                default -> donothing();
            }



            var message = textField.getText();
            sendFile(message);
//            sendMessage(message);
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                sendMessage("exit");
            }
        });



        panel.add(textField);
        panel.add(bntSend);

        add(panel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }


    private void donothing() {
        //just a stub
    }

    /**
     * Метод загрузки файлов с сервера по имени
     * Сервер реализован в том же проекте, поэтому адрес из корня проекта  папки server/
     * @param filename имя файла для загрузки
     */
    private void getFile(String filename)  {



    }

    /**
     * Метод отправки сообщений на сервер
     * @param filename имя файла в папке client/ в корне проекта
     */
    private void sendFile(String filename) {
        try {

            File file = new File("client\\" + filename);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }

            long fileLength = file.length();
            FileInputStream fis = new FileInputStream(file);

            out.writeUTF("upload"); //send command
            out.writeUTF(filename);  //send filename
            out.writeLong(fileLength); //send fileLength

            var read = 0;
            byte[] buffer = new byte[8 * 1024];
            while ((read = fis.read(buffer)) != -1){
                out.write(buffer, 0 , read);

            }
            out.flush();
            var status = in.readUTF();
            System.out.println("Sending status: "+status);

        } catch (FileNotFoundException e) {
         System.err.println("File not found - /client/" + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Метод отправки сообщений на сервер
     * @param message строка сообщения
     */
    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
            String command = in.readUTF();
//            if ("done".equalsIgnoreCase(command)){
//                System.out.println(command);
//            }
            System.out.println(command);
        } catch (EOFException eofException){
            System.err.println("Reading command error from "+socket.getInetAddress());
        }

        catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) throws IOException {
        new Client();
    }
}
