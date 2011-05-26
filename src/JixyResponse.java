import java.net.*;
import java.io.*;
import java.sql.*;

/**
 * Класс предназначен для отправки сообщений
 * @author predator
 */
public class JixyResponse {
	/**
	 * Поток вывода
	 */
	private DataOutputStream out;
	/**
	 * Поток ввода
	 */
	private Statement stmt;

	/**
	 * Стандартный коструктор
	 * @throws SQLException
	 */
	public JixyResponse() throws SQLException {
		out = null;
		stmt = Jixy.getNewStatement();
	}

	/**
	 * Устанавливает поток вывода в новое значение
	 * @param out Поток вывода
	 */
	public void setOutputStream(DataOutputStream out) {
		this.out = out;
	}

	/**
	 * @return Поток вывода, используемый объектом
	 */
	public DataOutputStream getOutputStream() {
		return out;
	}

	/**
	 * Устанавливает объект для отправки команд к базе данных в новое значение
	 * @param stmt Объект для отправки команд к базе данных
	 * @throws SQLException
	 */
	public void setStatement(Statement stmt) throws SQLException {
		this.stmt = stmt;
	}

	/**
	 * @return Объект для отправки команд к базе данных
	 * @throws SQLException
	 */
	public Statement getStatement() throws SQLException {
		return stmt;
	}

	/**
	 * Записывает строки текста в поток вывода
	 * @param str Строки текста
	 * @throws IOException
	 */
	public void send(String... str) throws IOException {
		if (str == null) {
			throw new NullPointerException();
		}
		synchronized (out) {
			int i, len = str.length;
			System.out.println(" --- send ---");
			for ( i = 0; i < len; ++i ) {
				out.writeUTF(str[i]);
				System.out.println(str[i]);
			}
			System.out.println(" --- end of send ---");
			out.flush();
		}
	}

	/**
	 * Записыват сообщение об ошибке в поток вывода
	 * @param errno
	 * @throws IOException
	 */
	public void sendError(JixyErrno errno) throws IOException {
		if (errno == null) {
			throw new NullPointerException();
		}
		switch (errno) {
			case ERR_NOT_REG_OR_AUTH:
				send("resp: error", "errno: " + JixyErrno.ERR_NOT_REG_OR_AUTH);
				break;
			case ERR_USER_DOES_NOT_EXIST:
				send("resp: error", "errno: " + JixyErrno.ERR_USER_DOES_NOT_EXIST);
				break;
			case ERR_MISSING_NAME:
				send("resp: error", "errno: " + JixyErrno.ERR_MISSING_NAME);
				break;
			case ERR_MISSING_PASS:
				send("resp: error", "errno: " + JixyErrno.ERR_MISSING_PASS);
				break;
			case ERR_MISSING_MSG:
				send("resp: error", "errno: " + JixyErrno.ERR_MISSING_MSG);
				break;
			case ERR_WRONG_REQ:
				send("resp: error", "errno: " + JixyErrno.ERR_WRONG_REQ);
				break;
			case ERR_USER_ALREADY_EXISTS:
				send("resp: error", "errno: " + JixyErrno.ERR_USER_ALREADY_EXISTS);
				break;
			case ERR_YOU_ARE_NOT_IN_THIS_ROOM:
				send("resp: error", "errno: " + JixyErrno.ERR_YOU_ARE_NOT_IN_THIS_ROOM);
				break;
			case ERR_YOU_ARE_ALREADY_IN_THIS_ROOM:
				send("resp: error", "errno: " + JixyErrno.ERR_YOU_ARE_ALREADY_IN_THIS_ROOM);
				break;
			case ERR_WRONG_PASS:
				send("resp: error", "errno: " + JixyErrno.ERR_WRONG_PASS);
			case ERR_FAILED_AUTH:
				send("resp: error", "errno: " + JixyErrno.ERR_FAILED_AUTH);
				break;
			case ERR_USER_OFFLINE:
				send("resp: error", "errno: " + JixyErrno.ERR_USER_OFFLINE);
				break;
			default:
				break;
		}
	}
}

