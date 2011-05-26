import java.net.*;
import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * Класс-поток сервера, обслуживает пользователей
 */
class JixyWorker implements Runnable {
	/**
	 * Обслуживаемый пользователь
	 */
	private JixyUser user;
	
	/**
	 * Объект описывающий запрос от клиента
	 */
	private JixyRequest request;
	
	/**
	 * Обект для связи с базой данных
	 */
	private Database db;

	/**
	 * Стандартный конструктов
	 * @param socket Сокет, связанный с клиентской машиной
	 * @throws IOException
	 * @throws SQLException
	 */
	public JixyWorker(Socket socket) throws IOException, SQLException {
		if (socket == null) {
			throw new NullPointerException();
		}
		// Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиенту.
		InputStream sin = socket.getInputStream();
		OutputStream sout = socket.getOutputStream();
		// Конвертируем потоки в другой тип, чтоб легче обрабатывать текстовые сообщения.
		DataInputStream in = new DataInputStream(sin);
		DataOutputStream out = new DataOutputStream(sout);
		// Создаем объект-запрос
		request = new JixyRequest(in);
		// Создаем объект описывающий пользователя
		user = new JixyUser(null);
		user.setOutputStream(out);
		// Создаем объект для связи с базой данных
		db = new Database();
	}

	/**
	 * Метод для запуска потока
	 */
	public void run() {
		try {
			handleClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			quit();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		JixyLogger.log(LogType.NONE, "User " + user.getNickname() + " disconnected!");
	}

	/**
	 * Занимается обработкой запросов
	 * @throws IOException
	 * @throws SQLException
	 */
	private void handleClient() throws IOException, SQLException {
		JixyRequestErrno errno = null;
		JixyUser usr = null;

        // Сначала выполняется регистрация или аутентификация нового участника
		while (true) {
			while ((errno = request.next()) != JixyRequestErrno.NONE) {
				handleError(errno);
			}
			boolean success = false;
			switch (request.getType()) {
				case REG:
					JixyLogger.log(LogType.NONE, "Запрос регистрации от пользователя с именем " + request.getNickname());
					// Проверяем существует ли пользователь с таким именем
					usr = Jixy.users.get(request.getNickname());
					if (usr != null) {
						user.sendError(JixyErrno.ERR_USER_ALREADY_EXISTS);
						break;
					}
					user.setNickname(request.getNickname());
					Jixy.users.put(user.getNickname(), user);
					db.newUser(user.getNickname(), request.getPassword(), request.getName(), request.getSurname(), request.getAge(),
							request.getGender(), request.getEmail());
					user.comfirmReg();
					success = true;
					break;
				case AUTH:
					JixyLogger.log(LogType.NONE, "Запрос авторизации от пользователя с именем " + request.getNickname());
					user.setNickname(request.getNickname());
					String pass = db.getPasswordByNickname(user.getNickname());
					JixyLogger.log(LogType.NONE, "Пароль в базе " + pass);
					JixyLogger.log(LogType.NONE, "Пароль в запросе " + request.getPassword());
					if (pass != null) {
						if ( pass.compareTo(request.getPassword()) != 0) {
							user.sendError(JixyErrno.ERR_WRONG_PASS);
							return;
						}
					} else {
						user.sendError(JixyErrno.ERR_FAILED_AUTH);
						return;
					}
					usr = Jixy.users.get(request.getNickname());
					usr.setOutputStream(user.getOutputStream());
					usr.setStatement(Jixy.getNewStatement());
					user = usr;
					user.confirmAuth();
					user.notifyLogin();
					success = true;
					break;
				default:
					user.sendError(JixyErrno.ERR_NOT_REG_OR_AUTH);
					success = false;
					break;
			}
			if (success) {
				break;
			}
		}
		
		user.newCache();
		user.newRooms();
		
		// Отправка сообщений другим пользователям
		while (true) {
			while ((errno = request.next()) != JixyRequestErrno.NONE) {
				handleError(errno);
			}
			switch (this.request.getType()) {
				case MSG:
					msg();
					break;
				case ROOMMSG:
					roommsg();
					break;
				case QUIT:
					quit();
					// Завершаем поток!
					return;
				case ADD:
					addToContactList(request.getNickname());
					break;
				case DEL:
					delFromContactList(request.getNickname());
					break;
				case ATTACH:
					attach();
					break;
				case DETACH:
					detach(request.getRoom());
					break;
				case CANCEL:
					cancelThisAccount();
					// Завершаем поток!
					return;
				case GETCONTACTS:
					JixyLogger.log(LogType.NONE, "Запрос списка контактов от пользователя с именем " + user.getNickname());
					user.getResponse().send("resp: contacts");
					for (Enumeration e = user.contacts.elements(); e.hasMoreElements();) {
						user.getResponse().send(((JixyUser)e.nextElement()).getNickname());
					}
					user.getResponse().send("");
					break;
				case GETROOMS:
					JixyLogger.log(LogType.NONE, "Запрос списка комнат от пользователя с именем " + user.getNickname());
					user.getResponse().send("resp: rooms");
					Iterator iter = Jixy.rooms.entrySet().iterator();
					while (iter.hasNext()) {
						Map.Entry pairs = (Map.Entry)iter.next();
						user.getResponse().send((String)pairs.getKey());
					}
					user.getResponse().send("");
					break;
				case GETSTATUS:
					JixyLogger.log(LogType.NONE, "Запрос статуса пользователя " + request.getNickname() + " от пользователя с именем " + user.getNickname());
					usr = user.contacts.get(request.getNickname());
					if (usr != null) {
						if (usr.contacts.get(user.getNickname()) != null) {
							if (usr.login()) {
								user.getResponse().send("resp: status", "name: " + usr.getNickname(), "status: online");
							} else {
								user.getResponse().send("resp: status", "name: " + usr.getNickname(), "status: offline");
							}
						} else {
							user.getResponse().send("resp: status", "name: " + usr.getNickname(), "status: offline");
						}
					} else {
						user.getResponse().send("resp: status", "name: " + usr.getNickname(), "status: offline");
					}
					break;
				case INFO:
					String[] info = db.getInfo(request.getNickname());
					if (info != null) {
						if (info[4].compareTo("f") == 0) {
							user.getResponse().send("resp: info",
								"name: " + info[0],
								"realname: " + info[1],
								"surname: " + info[2],
								"age: " + info[3],
								"gender: female",
								"email: " + info[5]);
						} else {
							user.getResponse().send("resp: info",
								"name: " + info[0],
								"realname: " + info[1],
								"surname: " + info[2],
								"age: " + info[3],
								"gender: male",
								"email: " + info[5]);
						}
					} else {
						user.sendError(JixyErrno.ERR_USER_DOES_NOT_EXIST);
					}
					break;
				case CHANGEINFO:
					String pass = db.getPasswordByNickname(user.getNickname());
					if (request.getPassword().compareTo(pass) != 0) {
						user.sendError(JixyErrno.ERR_WRONG_PASS);
					} else {
						db.changeInfo(user.getNickname(), request.getNewPassword(),
								request.getName(),
								request.getSurname(),
								request.getAge(),
								request.getGender(),
								request.getEmail());
						user.getResponse().send("resp: info has changed");
					}
					break;
				default:
					// Для порядка =)
					break;
			}
		}
	}

	/**
	 * Отправляет сообщение соответствующему пользователю
	 * @throws IOException
	 */
	private void msg() throws IOException {
		JixyUser receiver = getUser(request.getNickname());
			if (receiver == null) {
			user.sendError(JixyErrno.ERR_USER_DOES_NOT_EXIST);
		} else {
			if (receiver.login()) {
				receiver.sendMessage(user.getNickname(), request.getMessage());
			} else {
				user.sendError(JixyErrno.ERR_USER_OFFLINE);
			}
		}
	}

	/**
	 * Отправляет сообщение в соответствующую комнату
	 * @throws IOException
	 */
	private void roommsg() throws IOException {
		Room room = user.rooms.get(request.getRoom());
			if (room != null) {
			room.sendMessage(user.getNickname(), request.getMessage());
		} else {
			this.user.sendError(JixyErrno.ERR_YOU_ARE_NOT_IN_THIS_ROOM);
		}
	}

	/**
	 * Обрабатывает запрос на подключение к комнате
	 * @throws IOException
	 */
	private void attach() throws IOException {
		Room room = user.rooms.get(request.getRoom());
		if (room == null) {
			room = Jixy.rooms.get(request.getRoom());
			if (room == null) {
				room = new Room(request.getRoom());
				Jixy.rooms.put(request.getRoom(), room);
			}
			user.rooms.put(request.getRoom(), room);
			user.youAttacheRoom(request.getRoom());
			// Отправляем оповещения об уже общающихся пользователях
			// новому пользователю
			for (Enumeration e = room.users.elements(); e.hasMoreElements();) {
				JixyUser usr = (JixyUser)e.nextElement();
				user.userAttachedRoom(request.getRoom(), usr.getNickname());
			}
			// Отправляем общающимся пользователям информацию о том,
			// что к ним подключился новый пользователь
			room.userAttached(user);
		} else {
			user.sendError(JixyErrno.ERR_YOU_ARE_ALREADY_IN_THIS_ROOM);
		}
	}

	/**
	 * Обрабатывает запрос на выход из комнаты
	 * @param roomname Имя комнаты, из который выходит пользователь
	 * @throws IOException
	 */
	private void detach(String roomname) throws IOException {
		Room room = user.rooms.get(roomname);
		if (room != null) {
			user.rooms.remove(roomname);
			room.userDetached(user);
			/*if (room.isEmpty()) {
				Jixy.rooms.remove(roomname);
			}*/
		} else {
			user.sendError(JixyErrno.ERR_YOU_ARE_NOT_IN_THIS_ROOM);
		}
	}

	/**
	 * @param name Имя пользователя
	 * @return Объект класса JixyUser, который описывает пользователя с именем name
	 */
	private JixyUser getUser(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		JixyUser receiver = null;
		// Ищем пользователя в локальном кэше
		receiver = user.getUser(name);
		if (receiver == null) {
			// Поиск во всем списке пользователей
			receiver = Jixy.users.get(name);
			// Добавляем пользователя в локальный кэш
			user.addUser(receiver);
		}
		return receiver;
	}

	/**
	 * Обрабатывает ошибки
	 * @param errno Тип ошибки
	 * @throws IOException
	 */
	private void handleError(JixyRequestErrno errno) throws IOException {
		if (errno == null) {
			throw new NullPointerException();
		}
		switch (errno) {
			case WRONG_REQ:
				user.sendError(JixyErrno.ERR_WRONG_REQ);
				break;
			case MISSING_NAME:
				user.sendError(JixyErrno.ERR_MISSING_NAME);
				break;
			case MISSING_MSG:
				user.sendError(JixyErrno.ERR_MISSING_MSG);
				break;
			case MISSING_PASS:
				user.sendError(JixyErrno.ERR_MISSING_PASS);
				break;
			default:
				user.sendError(JixyErrno.ERR_UNKNOWN_ERR);
				break;
		}
	}

	/**
	 * Добавляет пользователя в список контактов
	 * @param nickname Имя пользователя
	 * @throws IOException
	 * @throws SQLException
	 */
	private void addToContactList(String nickname) throws IOException, SQLException {
		if (nickname == null) {
			throw new NullPointerException();
		}
		JixyUser usr = Jixy.users.get(nickname);
		if (usr == null) {
			user.sendError(JixyErrno.ERR_USER_DOES_NOT_EXIST);
		} else {
			// если пользователя ещё нет в списке контактов - добавить его
			if (user.contacts.get(usr.getNickname()) == null) {
				db.addUserToContactsByNickname(user.getNickname(), nickname);
				user.addUserToContacts(usr);
			}
		}
	}

	/**
	 * Удаляет пользователя из списка контактов
	 * @param nickname Имя пользователя
	 * @throws IOException
	 * @throws SQLException
	 */
	private void delFromContactList(String nickname) throws IOException, SQLException {
		if (nickname == null) {
			throw new NullPointerException();
		}
		JixyUser usr = Jixy.users.get(nickname);
		if (usr == null) {
			user.sendError(JixyErrno.ERR_USER_DOES_NOT_EXIST);
		} else {
			db.deleteUserFromContactsByNickname(nickname, usr.getNickname());
			user.delUserFromContacts(usr);
		}
	}

	/**
	 * Удаляет аккаунт
	 * @throws IOException
	 * @throws SQLException
	 */
	private void cancelThisAccount() throws IOException, SQLException {
		synchronized (this) {
			String pass = db.getPasswordByNickname(user.getNickname());
			if (request.getPassword().compareTo(pass) != 0) {
				user.sendError(JixyErrno.ERR_WRONG_PASS);
			} else {
				// Удаляем пользователя из общего списка пользователей сервера
				Jixy.users.remove(user.getNickname());
				// удаляем пользователя из БД
				db.deleteFromUsersByNickname(user.getNickname());
				// Удаляем все контакты пользователя
				db.deleteFromContactsByNickname(user.getNickname());
				// Удаляем пользователя из контактов других пользователей
				db.deleteFromContactsByContactname(user.getNickname());
				user.confirmCancellation();
				quit();
			}
		}
	}

	/**
	 * Обрабатывает запрос на выход из сети
	 * @throws IOException
	 * @throws SQLException
	 */
	private void quit() throws IOException, SQLException {
		synchronized (this) {
			// Отправляем оповещение во все комнаты
			Room room;
			for ( Enumeration e = user.rooms.elements(); e.hasMoreElements(); ) {
				room = (Room)e.nextElement();
				//room.userDetached(user);
				detach(room.roomname);
			}
			room = null;
			
			// Отправляем оповещение всем пользователям
			for (Enumeration e = user.contacts.elements(); e.hasMoreElements();) {
				JixyUser usr = (JixyUser)e.nextElement();
				// На случай, если пользователь удалил свой аккаунт
				if (usr != null && usr.login()) {
					if (usr.contacts.get(user.getNickname()) != null) {
						usr.userLogout(user);
					}
				}
			}
			
			// Очишаем локальный кэш
			user.users = null;
			user.rooms = null;
			// Предотвращаем дальнейшую отправку сообщений
			user.setOutputStream(null);
		}
		JixyLogger.log(LogType.NONE, "Пользователь " + user.getNickname() + " вышел из сети");
	}
}