import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * Класс описывает пользователя системы
 */
public class JixyUser {
	/**
	 * Имя пользователя
	 */
	private String nickname;

	/**
	 * Кэш пользователей не внесенных в список контактов
	 */
	public Hashtable<String, JixyUser> users;

	/**
	 * Список контактов
	 */
	public Hashtable<String, JixyUser> contacts;

	/**
	 * Список комнат
	 */
	public Hashtable<String, Room> rooms;

	/**
	 * Объект используемый для отправки сообщений пользователю
	 */
	private JixyResponse response;

	/**
	 * Стандартный конструктор
	 * @param nickname
	 * @throws SQLException
	 */
	public JixyUser(String nickname) throws SQLException {
		this.nickname = nickname;
		newCache();
		newRooms();
		contacts = new Hashtable<String, JixyUser>();
		response = new JixyResponse();
	}

	/**
	 * Созадет новый кэш пользователей
	 */
	public void newCache() {
		users = new Hashtable<String, JixyUser>();
	}

	/**
	 * Создает новый список комнат
	 */
	public void newRooms() {
		rooms = new Hashtable<String, Room>();
	}

	/**
	 * @return Поток вывода
	 */
	public DataOutputStream getOutputStream() {
		return response.getOutputStream();
	}

	/**
	 * Устанавливает новый поток вывода
	 * @param out
	 */
	public void setOutputStream(DataOutputStream out) {
		response.setOutputStream(out);
	}

	/**
	 * Устанавливает объект для отправки команд к базе данных в новое значение
	 * @param stmt Объект для отправки команд к базе данных
	 * @throws SQLException
	 */
	public void setStatement(Statement stmt) throws SQLException {
		response.setStatement(stmt);
	}

	/**
	 * @return Объект для отправки команд к базе данных
	 * @throws SQLException
	 */
	public Statement getStatement() throws SQLException {
		return response.getStatement();
	}

	/**
	 * @return Объект предназначенный для отправки сообщений
	 */
	public JixyResponse getResponse() {
		return response;
	}

	/**
	 * Устанавилвает новый ник пользователя
	 * @param nickname Новый ник пользователя
	 */
	public void setNickname(String nickname) {
		if (nickname == null) {
			throw new NullPointerException();
		}
		if (this.nickname == null) {
			this.nickname = nickname;
		}
	}

	/**
	 * @return Ник пользователя
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * Добавляет нового пользователя в список пользователей
	 * @param usr Пользователь
	 */
	public void addUser(JixyUser usr) {
		if (usr == null) {
			throw new NullPointerException();
		}
		users.put(usr.getNickname(), usr);
	}

	/**
	 * Возвращает пользователя из списка пользователей или из кэша
	 * @param nickname Ник пользователя
	 * @return Объект-пользователь
	 */
	public JixyUser getUser(String nickname) {
		if (nickname == null) {
			throw new NullPointerException();
		}
		// ищем в списке контактов
		JixyUser usr = contacts.get(nickname);
		if ( usr == null ) {
			// ищем в кэше остальных пользователе,
			// с которыми общался пользователь,
			// но которые ещё не находятся в его списке контактов
			usr = users.get(nickname);
		}
		return usr;
	}

	/**
	 * Добавляет пользователя в список контактов
	 * @param usr Объект-пользователь
	 * @throws IOException
	 * @throws SQLException
	 */
	public void addUserToContacts(JixyUser usr) throws IOException, SQLException {
		if (usr == null) {
			throw new NullPointerException();
		}
		contacts.put(usr.getNickname(), usr);
		JixyLogger.log(LogType.NONE, "Пользователь: " + usr.getNickname() + " добавлен в список контактов " + nickname);
		// оправить запрос авторизации
		response.send("resp: added", "name: " + usr.getNickname());
		usr.requestAuth(nickname);
	}

	/**
	 * Удаляет пользователя из списка контактов
	 * @param usr Объект-пользователь
	 * @throws SQLException
	 */
	public void delUserFromContacts(JixyUser usr) throws IOException, SQLException {
		if (usr == null) {
			throw new NullPointerException();
		}
		// если пользователь есть в списке контактов - удалить его
		contacts.remove(usr.getNickname());
		response.send("resp: deleted", "name: " + usr.getNickname());
	}

	/**
	 * Отправляет уведомления о том, что пользователь вошел в сеть,
	 * всем пользователям из списка контактов
	 * @throws IOException
	 */
	public void notifyLogin() throws IOException {
		for (Enumeration e = contacts.elements(); e.hasMoreElements();) {
			JixyUser usr = (JixyUser)e.nextElement();
			if (usr.contacts.get(nickname) != null) {
				if (usr.login()) {
					usr.userLogin(nickname);
				}
			}
		}
	}

	/**
	 * Отправляет уведомление о том, что пользователь
	 * успешно зарегистрировался
	 * @throws IOException
	 */
	public void comfirmReg() throws IOException {
		response.send("resp: reg is done");
	}

	/**
	 * Отправляет уведомление о том, что пользователь
	 * успешно идентифицировался
	 * @throws IOException
	 */
	public void confirmAuth() throws IOException {
		response.send("resp: auth is done");
	}
	
	/**
	 * Отправляет уведомление о том, что пользователь
	 * успешно удалил свой аккаунт
	 * @throws IOException
	 */
	public void confirmCancellation() throws IOException {
		response.send("resp: cancellation is done");
	}

	/**
	 * Отправляет уведомление о том, что пользователь вышел из сети
	 * @param user Объект-пользователь
	 * @throws IOException
	 */
	public void userLogout(JixyUser user) throws IOException {
		if (user == null) {
			throw new NullPointerException();
		}
		response.send("resp: logout", "name: " + user.getNickname());
	}

	/**
	 * Отправляет уведомление о том, что пользователь вышел из сети
	 * @param user Объект-пользователь
	 * @throws IOException
	 */
	public void userLogin(String nickname) throws IOException {
		response.send("resp: login", "name: " + nickname );
	}

	/**
	 * Отправляет сообщение текущему пользователю
	 * @param from От кого сообщение
	 * @param message Текст сообщения
	 * @throws IOException
	 */
	public void sendMessage(String from, String message) throws IOException {
		if (from == null || message == null) {
			throw new NullPointerException();
		}
		response.send("resp: msg", "from: " + from, "msg: " + message);
	}

	/**
	 * Отправляет сообщение из комнаты текущему пользователю
	 * @param room Название комнаты
	 * @param nickname Ник пользоватеяя, который отправил сообщение
	 * @param message Текст сообщения
	 * @throws IOException
	 */
	public void roomMessage(String room, String nickname, String message) throws IOException {
		response.send("resp: room_msg", "room: " + room, "name: " + nickname, "msg: " + message);
	}

	/**
	 * Отправляет текущему пользователю сообщение о том, что
	 * другой пользователь вошел в комнату
	 * @param room Название комнаты
	 * @param nickname Ник пользователя
	 * @throws IOException
	 */
	public void userAttachedRoom(String room, String nickname) throws IOException {
		response.send("resp: room_user_in", "room: " + room, "name: " + nickname);
	}

	/**
	 * Отправляет уведомление о том, что текущий пользователь вышел из комнаты
	 * @param room Название комнаты
	 * @param nickname Ник пользователя
	 * @throws IOException
	 */
	public void userDetachedRoom(String room, String nickname) throws IOException {
		response.send("resp: room_user_out", "room: " + room, "name: " + nickname);
	}

	/**
	 * Отправляет уведомление о том, что пользователь хочет добавить
	 * текущего пользователя в список контактов
	 * @param nickname
	 * @throws IOException
	 */
	public void requestAuth(String nickname) throws IOException {
		response.send("resp: auth", "from: " + nickname );
	}

	/**
	 * Отправляет уведомление о том, что текущий пользователь вошел в комнату
	 * @param room Название комнаты
	 * @throws IOException
	 */
	public void youAttacheRoom(String room) throws IOException {
		response.send("resp: attached", "room: " + room );
	}

	/**
	 * Отправляет сообщение об ошибке
	 * @param errno Тип ошибки
	 * @throws IOException
	 */
	public void sendError(JixyErrno errno) throws IOException {
		if (errno == null) {
			throw new NullPointerException();
		}
		response.sendError(errno);
	}

	/**
	 * @return Значение того,
	 * находиться ли данный пользователь в сети
	 */
	public boolean login() {
		if (response.getOutputStream() != null) {
			return true;
		} else {
			return false;
		}
	}
}

