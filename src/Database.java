import java.sql.*;

/**
 * Объект для работы с базой данных
 */
public class Database {
	/**
	 * Объект предназначенный для отправки запросов к базе данных
	 */
	private Statement stmt;

	/**
	 * Стандартный конструктор
	 * @throws SQLException
	 */
	public Database() throws SQLException {
		stmt = Jixy.getNewStatement();
	}

	
	/**
	 * Регистрация нового пользователя
	 * @param nickname Ник пользователя
	 * @param password Пароль пользователя
	 * @param name Имя пользователя
	 * @param surname Фамилия пользователя
	 * @param age Возраст пользователя
	 * @param gender Пол пользователя
	 * @param email Email-адрес пользователя
	 * @throws SQLException
	 */
	public void newUser(String nickname, String password, String name, String surname, int age, boolean gender, String email) throws SQLException {
		if ( nickname == null || password == null || name == null || surname == null || email == null ) {
			throw new NullPointerException();
		}
		JixyLogger.log(LogType.NONE, "Регистрируем нового пользователя на сервере и в БД...");
		String query = "INSERT INTO users (nickname, password, name, surname, age, gender, email) VALUES ('" + nickname + "','" + password + "','" +
						name + "','" + surname + "'," + age + "," + gender + ",'" + email + "')";
		stmt.executeUpdate(query);
	}

	/**
	 * Возвращает пароль пользователя с соответствующим именем
	 * @param nickname Имя пользователя
	 * @return Пароль пользователя
	 * @throws SQLException
	 */
	public String getPasswordByNickname(String nickname) throws SQLException {
		String query = "SELECT password FROM users WHERE nickname = '" + nickname + "';";
		JixyLogger.log(LogType.DB, query);
		ResultSet result = stmt.executeQuery(query);
		String ret = null;
		if ( result != null && result.next() ) {
			ret = result.getString(1);
		}
		return ret;
	}

	/**
	 * Добавляет пользователя в список контактов другого пользователя
	 * @param nickname Ник пользователя, которому добавляется новый контакт
	 * @param contactname Ник пользователя, который добавляется в список контактов
	 * @throws SQLException
	 */
	public void addUserToContactsByNickname(String nickname, String contactname) throws SQLException {
		if (nickname == null || contactname == null) {
			throw new NullPointerException();
		}
		String query = "INSERT INTO contacts(nickname, contactname) VALUES ('" + nickname + "', '" + contactname + "');";
		JixyLogger.log(LogType.DB, query);
		stmt.executeUpdate(query);
	}

	/**
	 * Удаляет пользователя из списока контактов другого пользователя
	 * @param nickname Ник пользователя, из списка контактов которого удаляется контакт
	 * @param contactname Ник пользователя, который удаляется из списка контактов другого пользователя
	 * @throws SQLException
	 */
	public void deleteUserFromContactsByNickname(String nickname, String contactname) throws SQLException {
		if (nickname == null || contactname == null) {
			throw new NullPointerException();
		}
		String query = "DELETE FROM contacts WHERE ('" + nickname + "', '" + contactname + "');";
		JixyLogger.log(LogType.DB, query);
		stmt.executeUpdate(query);
	}

	/**
	 * Удаляет пользователя из базы данных
	 * @param nickname Ник пользователя
	 * @throws SQLException
	 */
	public void deleteFromUsersByNickname(String nickname) throws SQLException {
		String query = "DELETE FROM users WHERE nickname = '" + nickname + "';";
		JixyLogger.log(LogType.DB, query);
		this.stmt.executeUpdate(query);
	}

	/**
	 * Удаляет список контактов пользователя по нику
	 * @param nickname Ник пользователя
	 * @throws SQLException
	 */
	public void deleteFromContactsByNickname(String nickname) throws SQLException {
		String query = "DELETE FROM contacts WHERE nickname = '" + nickname + "';";
		JixyLogger.log(LogType.DB, query);
		this.stmt.executeUpdate(query);
	}

	/**
	 * Удаляет пользователя из списка контактов других пользователей
	 * @param nickname Ник пользователя
	 * @throws SQLException
	 */
	public void deleteFromContactsByContactname(String nickname) throws SQLException {
		String query = "DELETE FROM contacts WHERE contactname = '" + nickname + "';";
		JixyLogger.log(LogType.DB, query);
		stmt.executeUpdate(query);
	}

	/*public void sendSavedMessagesToUser(JixyUser user) {
		
	}*/
	public String[] getInfo(String nickname) throws SQLException {
		String query = "SELECT nickname, name, surname, age, gender, email FROM users WHERE nickname = '" + nickname + "'";
		JixyLogger.log(LogType.DB, query);
		ResultSet result = stmt.executeQuery(query);
		String[] info = null;
		if ( result != null && result.next()) {
			info = new String[6];
			for (int i = 0; i < 6; i++) {
				info[i] = result.getString(i+1);
			}
		}
		return info;
	}

	public void changeInfo(String nickname, String new_pass, String name, String surname, int age, boolean gender, String email) throws SQLException {
		String query = "UPDATE users SET password = '" + new_pass +
				"', name = '" + name +
				"', surname = '" + surname +
				"', age = " + age +
				", gender = " + gender +
				", email = '" + email + "'" +
				"WHERE nickname = '" + nickname + "'";
		JixyLogger.log(LogType.DB, query);
		stmt.executeUpdate(query);
	}
}
