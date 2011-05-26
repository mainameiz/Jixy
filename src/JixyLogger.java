import java.io.*;
import java.net.*;

/**
 * Класс для регистрации производимых действий
 */
public class JixyLogger {
	/**
	 * Записывает сообщение о произведенном действии
	 * @param type Тип сообщения
	 * @param message Текст сообщения
	 */
	public static void log(LogType type, String message) {
		switch (type) {
			case DB:
				System.err.println("Query: " + message);
				break;
			case NONE:
				System.err.println("log: " + message);
				break;
			default:
				System.err.println("Unknown type: " + message);
				break;
		}

	}

	public static void accessLog(Socket socket) {
		
	}
}

