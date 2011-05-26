import java.io.*;
import java.util.*;

/**
 * Класс описывает комнату чата
 */
public class Room {
	/**
	 * Имя комнаты
	 */
	public String roomname;
	/**
	 * Список пользователей, находящихся в комнате
	 */
	public Hashtable<String, JixyUser> users;

	/**
	 * Стандартный конструктор. Устанавливает имя комнаты и создает пустой список пользователей
	 * @param roomname Имя комнаты
	 */
	public Room(String roomname) {
		this.roomname = roomname;
		this.users = new Hashtable<String, JixyUser>();
	}

	/**
	 * Отправляет сообщение всем пользователям в комнате
	 * @param from Имя отправителя
	 * @param message Текст сообщения
	 * @throws IOException
	 */
	public void sendMessage(String from, String message) throws IOException {
		JixyUser usr = null;
		for ( Enumeration e = this.users.elements(); e.hasMoreElements(); ) {
			usr = (JixyUser)e.nextElement();
			if ( usr != null && usr.getResponse() != null ) {
				usr.roomMessage(this.roomname, from, message);
			}
		}
	}

	/**
	 * Уведомляет пользователей, которые уже находятся в комнате о том, что к ним
	 * присоединился ещё один пользователь
	 * @param user Объект-пользователь
	 * @throws IOException
	 */
	public void userAttached(JixyUser user) throws IOException {
		JixyUser usr = null;
		for ( Enumeration e = this.users.elements(); e.hasMoreElements(); ) {
			usr = (JixyUser)e.nextElement();
			if ( usr != null && usr.getResponse() != null ) {
				usr.userAttachedRoom(this.roomname, user.getNickname() );
			}
		}
		this.users.put(user.getNickname(), user);
	}

	/**
	 * Уведомляет пользователей, которые уже находятся в комнате о том, что другой
	 * пользователь вышел из комнаты
	 * @param user Объект-пользователь
	 * @throws IOException
	 */
	public void userDetached(JixyUser user) throws IOException {
		JixyUser usr = null;
		this.users.remove(user.getNickname());
		for ( Enumeration e = this.users.elements(); e.hasMoreElements(); ) {
			usr = (JixyUser)e.nextElement();
			if ( usr != null && usr.getResponse() != null ) {
				usr.userDetachedRoom(this.roomname, user.getNickname() );
			}
		}
	}

	/**
	 * Возвращает булевое значение того, пуста ли комната
	 * @return Комната пуста?
	 */
	public boolean isEmpty() {
		return this.users.isEmpty();
	}
}

