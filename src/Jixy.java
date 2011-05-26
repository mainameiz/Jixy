import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.*;
import java.sql.*;

/**
 * Главный класс сервера
 */
public class Jixy {
	/**
	 * Список пользователей
	 */
	public static Map<String, JixyUser> users;
	/**
	 * Список комнат
	 */
	public static Map<String, Room> rooms;
	/**
	 * Объект для связи с базой данных
	 */
	private static Connection conn;
	
	public static void main(String[] args) {
		// Случайный порт (может быть любое число от 1025 до 65535)
		int port = 0;
		String dbport = null;
		String dbip = null;
		String dbname = null;
		String dbuser = null;
		String dbpasswd = null;
		String dburl = null;
		Statement stmt = null;
		ExecutorService pool = null;
		boolean createTables = false;

		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith("--listen")) {
					port = Integer.parseInt(args[i].substring(args[i].indexOf('=') + 1));
				}
				if (args[i].startsWith("--db-ip")) {
					dbip = args[i].substring(args[i].indexOf('=') + 1);
				}
				if (args[i].startsWith("--db-port")) {
					dbport = args[i].substring(args[i].indexOf('=') + 1);
				}
				if (args[i].startsWith("--db-name")) {
					dbname = args[i].substring(args[i].indexOf('=') + 1);
				}
				if (args[i].startsWith("--db-user")) {
					dbuser = args[i].substring(args[i].indexOf('=') + 1);
				}
				if (args[i].startsWith("--db-passwd")) {
					dbpasswd = args[i].substring(args[i].indexOf('=') + 1);
				}
				if (args[i].startsWith("--create-tables")) {
					createTables = true;
				}
			}

			if (dbip == null) {
				System.out.println("You need to specify database IP adresss (--db-ip option).");
				System.exit(1);
			}
			if (dbport == null) {
				System.err.println("Database port is not specified. Default port (5432) will be used.");
				dbport = "5432";
			}
			if (dbname == null) {
				System.err.println("Database name is not specified. Default name (jixydb) will be used.");
				dbname = "jixydb";
			}
			if (dbuser == null) {
				System.out.println("You need to specify database user (--db-user option).");
				System.exit(1);
			}
			if (dbpasswd == null) {
				System.out.println("You need to specify database password (--db-passwd option).");
				System.exit(1);
			}

			dburl = "jdbc:postgresql://" + dbip + ":" + dbport + "/" + dbname;

			Class.forName("org.postgresql.Driver");
			Jixy.conn = DriverManager.getConnection(dburl, dbuser, dbpasswd);

			stmt = getNewStatement();

			if (createTables) {
					// Создаем необходимые таблицы в базеданных
					// Таблица пользователей
					//stmt.executeUpdate("CREATE TABLE users(user_id SERIAL PRIMARY KEY, nickname VARCHAR(20), password VARCHAR(20), name VARCHAR(20), surname VARCHAR(20), age INTEGER, gender BOOLEAN, email VARCHAR(40))");
					stmt.executeUpdate("CREATE TABLE users(nickname VARCHAR(20), password VARCHAR(20), name VARCHAR(20), surname VARCHAR(20), age INTEGER, gender BOOLEAN, email VARCHAR(40))");
					// Таблица контактов
					//stmt.executeUpdate("CREATE TABLE contacts(contact_id SERIAL PRIMARY KEY, nickname VARCHAR(20), contactname VARCHAR(20))");
					stmt.executeUpdate("CREATE TABLE contacts(nickname VARCHAR(20), contactname VARCHAR(20))");
					System.out.println("Tables was created.");
					System.exit(0);
			}

			if (port == 0) {
				System.out.println("You need to specify listen port (--listen option).");
				System.exit(1);
			}

			Jixy.users = Collections.synchronizedMap(new HashMap<String, JixyUser>());
			Jixy.rooms = Collections.synchronizedMap(new HashMap<String, Room>());

			ArrayList list = new ArrayList();
			ResultSet result = stmt.executeQuery("SELECT nickname FROM users");
			String nickname;
			while (result.next()) {
				nickname = result.getString(1);
				list.add(nickname);
				Jixy.users.put(nickname, new JixyUser(nickname));
				System.out.println( Jixy.users.get(nickname).getNickname());
			}

			for (int i = 0; i < list.size(); i++) {
				JixyUser usr = Jixy.users.get((String)list.get(i));
				System.out.println("Contacts of " + usr.getNickname() + ":");
				result = stmt.executeQuery("SELECT contactname FROM contacts WHERE nickname = '" + usr.getNickname() + "'");
				while (result != null && result.next()) {
					String contactname = result.getString(1);
					System.out.println(contactname);
					JixyUser contact = Jixy.users.get(contactname);
					if (contact != null) {
						usr.contacts.put(contactname, contact);
					}
				}
			}
			
			// Создаем сокет сервера и привязываем его к вышеуказанному порту
			ServerSocket ss = new ServerSocket(port);
			pool = Executors.newCachedThreadPool();
			System.out.println("Jixy started.");
			while (true) {
				// Заставляем сервер ждать подключений
				Socket socket = ss.accept();
				JixyLogger.accessLog(socket);
				pool.execute( new JixyWorker(socket) );
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (NumberFormatException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	/**
	 * @return Новый объект, предназначенный для отправки команд к базе данных
	 * @throws SQLException
	 */
	public static synchronized Statement getNewStatement() throws SQLException {
		return Jixy.conn.createStatement();
	}

	public static Map getUsers() {
		return Jixy.users;
	}
}