import java.io.*;

/**
 * Класс предназначен для разбора входящих запросов от пользователя
 */
public class JixyRequest {
	private DataInputStream	in;
	private String			line;
	private	JixyRequestType	type;
	private	String			nickname;
	private String			name;
	private String			surname;
	private String			room;
	private int				age;
	private boolean			gender;
	private String			email;
	private	String			code;
	private	String			message;
	private	String			password;
	private	String			new_password;

	/**
	 * Стандартный конструктор для класса JixyRequest
	 * Сохраняет в себе поток ввода, с которого в последствии
	 * будут читаться входящие запросы
	 * @param in	Поток ввода, с которого будут читаться входящие запросы
	 */
	public JixyRequest(DataInputStream in) {
		if (in == null) {
			throw new NullPointerException();
		}
		this.in = in;
		this.type = JixyRequestType.NONE;
	}

	/**
	 * @return Тип запроса
	 */
	public JixyRequestType getType() {
		return this.type;
	}

	/**
	 * @return Имя (nickname) пользователя
	 */
	public String getNickname() {
		return this.nickname;
	}

	/**
	 * @return Текст сообщения
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * @return Пароль
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * @return Реальное имя пользователя
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return Фамилию пользователя
	 */
	public String getSurname() {
		return this.surname;
	}

	/**
	 * @return Название комнаты
	 */
	public String getRoom() {
		return this.room;
	}

	/**
	 * @return Возраст пользователя
	 */
	public int getAge() {
		return this.age;
	}

	/**
	 * @return Пол пользователя
	 */
	public boolean getGender() {
		return this.gender;
	}

	/**
	 * @return Email запроса
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 * @return Новый пароль
	 */
	public String getNewPassword() {
		return new_password;
	}

	/**
	 * Читает из потока ввода следующий запрос и разбирает его на составляющие части
	 * @return Тип ошибки
	 * @throws IOException
	 */
	public JixyRequestErrno next() throws IOException {
		// Очищаем объект запроса
		this.clear();
		// Разбираем входящий запрос
		this.line = this.in.readUTF();
		// Определяем тип запроса
		if (this.line.startsWith("req: msg")) {
			// Отправка сообщения
			this.line = this.in.readUTF();
			if (this.line.startsWith("name: ")) {
				this.nickname = this.line.substring(6);
				this.line = this.in.readUTF();
				if (this.line.startsWith("msg: ")) {
					this.message = this.line.substring(5);
					this.type = JixyRequestType.MSG;
				} else {
					return JixyRequestErrno.MISSING_MSG;
				}
			} else {
				return JixyRequestErrno.MISSING_NAME;
			}
		} else if (this.line.startsWith("req: roommsg")) {
			// Отправка сообщения
			this.line = this.in.readUTF();
			if (this.line.startsWith("room: ")) {
				this.room = this.line.substring(6);
				this.line = this.in.readUTF();
				if (this.line.startsWith("msg: ")) {
					this.message = this.line.substring(5);
					this.type = JixyRequestType.ROOMMSG;
				} else {
					return JixyRequestErrno.MISSING_MSG;
				}
			} else {
				return JixyRequestErrno.MISSING_NAME;
			}
		} else if (this.line.startsWith("req: auth")) {
			// Идентификация зарегистрированного пользователя
			this.line = this.in.readUTF();
			if (this.line.startsWith("name: ")) {
				// Читаем имя пользователя
				this.nickname = this.line.substring(6);
				this.line = this.in.readUTF();
				if (this.line.startsWith("pass: ")) {
					// Читаем пароль
					this.password = this.line.substring(6);
						this.type = JixyRequestType.AUTH;
				} else {
					return JixyRequestErrno.MISSING_PASS;
				}
			} else {
				return JixyRequestErrno.MISSING_NAME;
			}
		} else if (this.line.startsWith("req: add")) {
			this.line = this.in.readUTF();
			if (this.line.startsWith("name: ")) {
				this.nickname = this.line.substring(6);
				this.type = JixyRequestType.ADD;
			} else {
				return JixyRequestErrno.MISSING_NAME;
			}
		} else if (this.line.startsWith("req: del")) {
			this.line = this.in.readUTF();
			if (this.line.startsWith("name: ")) {
				this.nickname = this.line.substring(6);
				this.type = JixyRequestType.DEL;
			} else {
				return JixyRequestErrno.MISSING_NAME;
			}
		} else if (this.line.startsWith("req: quit")) {
			this.type = JixyRequestType.QUIT;
		} else if (this.line.startsWith("req: attach")) {
			this.line = this.in.readUTF();
			if (this.line.startsWith("room: ")) {
				// Читаем имя пользователя
				this.room = this.line.substring(6);
				this.type = JixyRequestType.ATTACH;
			} else {
				return JixyRequestErrno.MISSING_NAME;
			}
		} else if (this.line.startsWith("req: detach")) {
			this.line = this.in.readUTF();
			if (this.line.startsWith("room: ")) {
				// Читаем имя пользователя
				this.room = this.line.substring(6);
				this.type = JixyRequestType.DETACH;
			} else {
				return JixyRequestErrno.MISSING_NAME;
			}
		} else if (this.line.startsWith("req: status")) {
			this.line = this.in.readUTF();
			if (this.line.startsWith("name: ")) {
				// Читаем имя пользователя
				this.nickname = this.line.substring(6);
				this.type = JixyRequestType.GETSTATUS;
			} else {
				return JixyRequestErrno.MISSING_NAME;
			}
		} else if (this.line.startsWith("req: cancel")) {
			this.line = this.in.readUTF();
			if (this.line.startsWith("name: ")) {
				// Читаем имя пользователя
				this.nickname = this.line.substring(6);
				this.line = this.in.readUTF();
				if (this.line.startsWith("pass: ")) {
					// Читаем пароль
					this.password = this.line.substring(6);
						this.type = JixyRequestType.CANCEL;
				} else {
					return JixyRequestErrno.MISSING_PASS;
				}
			} else {
				return JixyRequestErrno.MISSING_NAME;
			}
		} else if (this.line.startsWith("req: contacts")) {
			this.type = JixyRequestType.GETCONTACTS;
		} else if (this.line.startsWith("req: rooms")) {
			this.type = JixyRequestType.GETROOMS;
		} else if (this.line.startsWith("req: changeinfo")) {
			this.line = this.in.readUTF();
			if (this.line.startsWith("pass: ")) {
				this.password = this.line.substring(6);
				this.line = this.in.readUTF();
				if (this.line.startsWith("new_pass: ")) {
					this.new_password = this.line.substring(10);
					this.line = this.in.readUTF();
					if (this.line.startsWith("realname: ")) {
					this.name = this.line.substring(10);
					this.line = this.in.readUTF();
					if (this.line.startsWith("surname: ")) {
						this.surname = this.line.substring(9);
						this.line = this.in.readUTF();
						if (this.line.startsWith("age: ")) {
							try {
									this.age = Integer.parseInt(this.line.substring(5));
								} catch (NumberFormatException e) {
									return JixyRequestErrno.WRONG_FORMAT;
								}
								this.line = this.in.readUTF();
								if (this.line.startsWith("gender: ")) {
									String gender = this.line.substring(8);
									if ( gender.compareTo("male") == 0 ) {
										this.gender = true;
									} else if ( gender.compareTo("female") == 0 ) {
										this.gender = false;
									} else {
										return JixyRequestErrno.WRONG_FORMAT;
									}
									this.line = this.in.readUTF();
									if (this.line.startsWith("email: ")) {
										this.email = this.line.substring(7);
										this.type = JixyRequestType.CHANGEINFO;
									} else {
										return JixyRequestErrno.MISSING_INFORMATION_FIELD;
									}
								} else {
									return JixyRequestErrno.MISSING_INFORMATION_FIELD;
								}
							} else {
									return JixyRequestErrno.MISSING_INFORMATION_FIELD;
						}
						} else {
							return JixyRequestErrno.MISSING_INFORMATION_FIELD;
						}
					} else {
						return JixyRequestErrno.MISSING_INFORMATION_FIELD;
					}
				} else {
					return JixyRequestErrno.MISSING_INFORMATION_FIELD;
				}
			} else {
				return JixyRequestErrno.MISSING_PASS;
			}
		} else if (this.line.startsWith("req: info")) {
			this.line = this.in.readUTF();
			if (this.line.startsWith("name: ")) {
				this.nickname = this.line.substring(6);
				this.type = JixyRequestType.INFO;
			} else {
				return JixyRequestErrno.MISSING_NAME;
			}
		} else if (this.line.startsWith("req: reg")) {
			// Регистрируем нового пользователя
			this.line = this.in.readUTF();
			if (this.line.startsWith("name: ")) {
				// Читаем имя пользователя
				this.nickname = this.line.substring(6);
				this.line = this.in.readUTF();
				if (this.line.startsWith("pass: ")) {
					this.password = this.line.substring(6);
					this.line = this.in.readUTF();
					if (this.line.startsWith("realname: ")) {
						this.name = this.line.substring(10);
						this.line = this.in.readUTF();
						if (this.line.startsWith("surname: ")) {
							this.surname = this.line.substring(9);
							this.line = this.in.readUTF();
							if (this.line.startsWith("age: ")) {
								try {
									this.age = Integer.parseInt(this.line.substring(5));
								} catch (NumberFormatException e) {
									return JixyRequestErrno.WRONG_FORMAT;
								}
								this.line = this.in.readUTF();
								if (this.line.startsWith("gender: ")) {
									String gender = this.line.substring(8);
									if ( gender.compareTo("male") == 0 ) {
										this.gender = true;
									} else if ( gender.compareTo("female") == 0 ) {
										this.gender = false;
									} else {
										return JixyRequestErrno.WRONG_FORMAT;
									}
									this.line = this.in.readUTF();
									if (this.line.startsWith("email: ")) {
										this.email = this.line.substring(7);
										this.type = JixyRequestType.REG;
									} else {
										return JixyRequestErrno.MISSING_INFORMATION_FIELD;
									}
								} else {
									return JixyRequestErrno.MISSING_INFORMATION_FIELD;
								}
							} else {
								return JixyRequestErrno.MISSING_INFORMATION_FIELD;
							}
						} else {
							return JixyRequestErrno.MISSING_INFORMATION_FIELD;
						}
					} else {
						return JixyRequestErrno.MISSING_INFORMATION_FIELD;
					}
				} else {
					return JixyRequestErrno.MISSING_PASS;
				}
			} else {
				return JixyRequestErrno.MISSING_NAME;
			}
		} else {
			return JixyRequestErrno.WRONG_REQ;
		}
		return JixyRequestErrno.NONE;
	}

	/**
	 * Устанавливает значения поумолчанию для объекта
	 */
	private void clear() {
		this.type = JixyRequestType.NONE;
		this.nickname = null;
		this.code = null;
		this.message = null;
		this.name = null;
		this.surname = null;
		this.room = null;
		this.age = 0;
		this.gender = false;
		this.email = null;
		this.line = null;
	}
}

