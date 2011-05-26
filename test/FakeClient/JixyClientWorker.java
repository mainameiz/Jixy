import java.net.*;
import java.io.*;

// Получает входящие сообщения с сервера
class JixyClientWorker implements Runnable {
    //private String name;    // имя потока
	private Thread thread;   // поток
	private DataInputStream in;

    public JixyClientWorker(DataInputStream in) {
		//this.name = threadname;
		this.thread = new Thread(this, "ClientWorker");
		this.in = in;
	}

	public void start() {
		this.thread.start();    // старт потока
	}

	public void run() {
		try {
			handleInputCommands();
		} catch (Exception e) {
			System.err.println("Поток входящих сообщений прерван");
		}

		//this.socket.close();
		System.err.println("Поток входящих сообщений прерван");
	}

	private void handleInputCommands() throws IOException {
	    String line = null; // строка сообщения от сервера
	    String from = null; // имя отправителя
	    String message_number = null; // номер сообщения для подтверждения получения
	    String message = null; // строка-сообщение
	    while (true) {
            line = in.readUTF(); // ждем сообщений от сервера
            //System.out.println(line);
            if (line.startsWith("resp: msg")) {
				System.err.println(line);
				//line = in.readUTF(); // читаем номер сообщения для подтверждения получения
				//if (line.startsWith("msgnum: ")) {
				//	System.out.println(line);
                //    message_number = line.substring(16);
                    line = in.readUTF(); // читаем имя отправителя
                    if (line.startsWith("from: ")) {
                        System.err.println(line);
                        from = line.substring(6);
                        line = in.readUTF();
                        if (line.startsWith("msg: ")) {
                            System.err.println(line);
                            message = line.substring(5);
                            //System.out.println("Вам новое сообщение от: " + from);
                            //System.out.println(" > " + from + ": " + message);
                            //System.out.println("successed!");
                            continue;
                        }
                    }
                //}
            }

            System.out.println("ERROR: " + line);
            continue;
        }
	}

}

