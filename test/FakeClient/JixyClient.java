import java.net.*;
import java.io.*;
import java.sql.*;

public class JixyClient {
	public static boolean eq(String str1, String str2) {
		if ( str1.startsWith(str2) ) {
			return true;
		} else {
			return false;
		}
	}

	public static void dbUsers() throws SQLException {

	}

	public static void dbContacts() throws SQLException {

	}

	public static InetAddress ipAddress;
	public static int serverPort = 3001; // здесь обязательно нужно указать порт к которому привязывается сервер.
	public static String address = "127.0.0.1";   // это IP-адрес компьютера, где исполняется наша серверная программа.
                                        // Здесь указан адрес того самого компьютера где будет исполняться и клиент.
	public static Connection conn;
	public static Statement stmt;
	public static boolean error = false;

	// --- Описание пользователей ---

	// --- первый пользователь ---
	public static String username1 = "testname1";
	public static String userpass1 = "testpass1";
	public static String realname1 = "Вася";
	public static String surname1 = "Пупкин";
	public static int age1 = 40;
	public static String gender1 = "male";
	public static String email1 = "vasya@mail.ru";
	public static Socket socket1;
	public static InputStream sin1;
	public static OutputStream sout1;
	public static DataInputStream in1;
	public static DataOutputStream out1;
	public static String line1;
	public static String msg1 = "first message from user " + username1;
	public static String room1 = "default_room";

	// --- второй пользователь ---
	public static String username2 = "testname2";
	public static String userpass2 = "testpass2";
	public static String realname2 = "realname2";
	public static String surname2 = "surname2";
	public static int age2 = 4000;
	public static String gender2 = "female";
	public static String email2 = "myemail@mail.ru";
	public static Socket socket2;
	public static InputStream sin2;
	public static OutputStream sout2;
	public static DataInputStream in2;
	public static DataOutputStream out2;
	public static String line2;
	public static String msg2 = "second message from user " + username2;


	public static void connect1() throws IOException {
		// создаем сокет используя IP-адрес и порт сервера
		socket1 = new Socket(ipAddress, serverPort);
		// Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиентом.
		sin1 = socket1.getInputStream();
		sout1 = socket1.getOutputStream();
		// Конвертируем потоки в другой тип, чтоб легче обрабатывать текстовые сообщения.
		in1 = new DataInputStream(sin1);
		out1 = new DataOutputStream(sout1);
	}

	public static void connect2() throws IOException {
		socket2 = new Socket(ipAddress, serverPort);
		sin2 = socket2.getInputStream();
		sout2 = socket2.getOutputStream();
		in2 = new DataInputStream(sin2);
		out2 = new DataOutputStream(sout2);
	}

	public static void regUser1() throws IOException {
		System.out.println("reg: " + username1);
		out1.writeUTF("req: reg");
		out1.writeUTF("name: " + username1);
		out1.writeUTF("pass: " + userpass1);
		out1.writeUTF("realname: " + realname1);
        out1.writeUTF("surname: " + surname1);
		out1.writeUTF("age: " + age1);
		out1.writeUTF("gender: " + gender1);
		out1.writeUTF("email: " + email1);
		out1.flush();
		read1();
		if ( ! line1.startsWith("resp: reg is done") ) {
			System.err.println("Не удалось зарегистрироваться на сервере");
			System.err.println("Ответ сервера: " + line1);
			return;
		}
	}

	public static void regUser2() throws IOException {
		System.out.println("reg: " + username2);
		out2.writeUTF("req: reg");
		out2.writeUTF("name: " + username2);
		out2.writeUTF("pass: " + userpass2);
		out2.writeUTF("realname: " + realname2);
		out2.writeUTF("surname: " + surname2);
		out2.writeUTF("age: " + age2);
		out2.writeUTF("gender: " + gender2);
		out2.writeUTF("email: " + email2);
		out2.flush();
		read2();
		if ( ! line2.startsWith("resp: reg is done") ) {
			System.err.println("Не удалось зарегистрироваться на сервере");
			System.err.println("Ответ сервера: " + line2);
			return;
		}
	}
	public static void authUser1() throws IOException {
		System.out.println("auth: " + username1);
		out1.writeUTF("req: auth");
		out1.writeUTF("name: " + username1);
		out1.writeUTF("pass: " + userpass1);
		out1.flush();
		read1();
		if ( ! line1.startsWith("resp: auth is done") ) {
			System.err.println("Не удалось авторизоваться на сервере");
			System.err.println("Ответ сервера: " + line1);
			return;
		}
	}

	public static void authUser2() throws IOException {
		System.out.println("auth: " + username2);
		out2.writeUTF("req: auth");
		out2.writeUTF("name: " + username2);
		out2.writeUTF("pass: " + userpass2);
		out2.flush();
		read2();
		if ( ! line2.startsWith("resp: auth is done") ) {
			System.err.println("Не удалось авторизоваться на сервере");
			System.err.println("Ответ сервера: " + line2);
			return;
		}
	}

	public static void quitUser1() throws IOException {
		out1.writeUTF("req: quit");
		out1.flush();
		socket1.close();
	}

	public static void quitUser2() throws IOException {
		out2.writeUTF("req: quit");
		out2.flush();
		socket2.close();
	}

	public static void delUser1() throws IOException {
			System.out.println("Удаляем пользователя " + username1);
			out1.writeUTF("req: cancel");
			out1.writeUTF("name: " + username1);
			out1.writeUTF("pass: " + userpass1);
			out1.flush();
            read1();
            if ( ! line1.startsWith("resp: cancellation is done") ) {
            	System.err.println("Не удалось удалить аккаунт");
            	System.err.println("Ответ сервера: " + line1);
                error = true;
            }
	}

	public static void delUser2() throws IOException {
			System.out.println("Удаляем пользователя " + username2);
            out2.writeUTF("req: cancel");
			out2.writeUTF("name: " + username2);
			out2.writeUTF("pass: " + userpass2);
			out2.flush();
            read2();
            if ( ! line2.startsWith("resp: cancellation is done") ) {
            	System.err.println("Не удалось удалить аккаунт");
            	System.err.println("Ответ сервера: " + line2);
                error = true;
            }
	}

	public static void msgUser1() throws IOException {
		System.out.println("msgUser1()");
		out1.writeUTF("req: msg");
		out1.writeUTF("name: " + username2);
		out1.writeUTF("msg: " + msg1);
		out1.flush();
	}

	public static void msgUser2() throws IOException {
		System.out.println("msgUser2()");
		out2.writeUTF("req: msg");
		out2.writeUTF("name: " + username1);
		out2.writeUTF("msg: " + msg2);
		out2.flush();
	}

	public static void getMesUser1() throws IOException {
		System.out.println("getMesUser1()");
		read1();
		if ( line1.startsWith("resp: msg") ) {
			read1();
			if ( line1.startsWith("from: " + username2) ) {
				read1();
				if ( line1.startsWith("msg: " + msg2) ) {
					return;
				}
			}
		}
		System.err.println("Не удалось получить сообщение от пользователя " + username2);
		System.err.println("Ответ сервера: " + line1);
		error = true;
	}

	public static void getMesUser2() throws IOException {
		System.out.println("getMesUser2()");
		read2();
		if ( line2.startsWith("resp: msg") ) {
			read2();
			if ( line2.startsWith("from: " + username1) ) {
				read2();
				if ( line2.startsWith("msg: " + msg1) ) {
					return;
				}
			}
		}
		System.err.println("Не удалось получить сообщение от пользователя " + username1);
		System.err.println("Ответ сервера: " + line2);
		error = true;
	}

	public static void read1() throws IOException {
		line1 = in1.readUTF();
		System.out.println("line1: " + line1);
	}

	public static void read2() throws IOException {
		line2 = in2.readUTF();
		System.out.println("line2: " + line2);
	}

	public static void main(String[] args) throws SQLException {
		//Class.forName("org.postgresql.Driver");
		try {
			ipAddress = InetAddress.getByName(address); // создаем объект который отображает вышеописанный IP-адрес.
			connect1();
			connect2();

			// --- Начало теста ---
			dbUsers();
			regUser1();
			regUser2();
			//read1();
			//read2();
            System.out.println("Регистрация прошла успешно");

			dbUsers();

            System.out.println("Отсоединяемся от сервера и закрываем соединение с сервером");
			quitUser1();
			quitUser2();

			System.out.println("Открываем новое соединение с сервером");
			connect1();
			connect2();
			authUser1();
			authUser2();
			System.out.println("Авторизация прошла успешно");

			System.out.println("Тестирование добавления в список контактов");
			out1.writeUTF("req: add");
			out1.writeUTF("name: " + username2);
			out1.flush();
			out2.writeUTF("req: add");
			out2.writeUTF("name: " + username1);
			out2.flush();

			read1();
            if ( line1.startsWith("resp: auth") ) {
            	read1();
            	if ( line1.startsWith("from: " + username2) ) {
					System.err.println("Пришел запрос авторизации от " + username2 + " к " + username1);
                } else {
                	System.err.println("От пользователя " + username2 + " не пришел запрос авторизации");
		        	System.err.println("Ответ сервера: " + line1);
		            return;
                }
            } else {
    	        	System.err.println("От пользователя " + username2 + " не пришел запрос авторизации");
		        	System.err.println("Ответ сервера: " + line1);
		            return;
            }

            read2();
            if ( line2.startsWith("resp: auth") ) {
            	read2();
            	if ( line2.startsWith("from: " + username1) ) {
		        	System.err.println("Пришел запрос авторизации от " + username1 + " к " + username2);
                } else {
                	System.err.println("От пользователя " + username1 + " не пришел запрос авторизации");
		        	System.err.println("Ответ сервера: " + line2);
		            return;
                }
            } else {
    	        	System.err.println("От пользователя " + username1 + " не пришел запрос авторизации");
		        	System.err.println("Ответ сервера: " + line2);
		            return;
            }

            dbContacts();

			out1.writeUTF("req: contacts");
			out1.flush();
			read1();
			System.out.println(line1);
			read1();
			System.out.println(line1);
			read1();
			System.out.println(line1);

			msgUser1();
			getMesUser2();
			if (error) {
				return;
			}
			msgUser2();
			getMesUser1();
			if (error) {
				return;
			}

			/* входим в комнату */
			System.out.println("Пользователь " + username1 + " входит в комнату");
			out1.writeUTF("req: attach");
			out1.writeUTF("room: " + room1);
			out1.flush();
			read1();
			if ( line1.startsWith("resp: attached") ) {
				read1();
				if ( line1.startsWith("room: " + room1 ) ) {
					;
				} else {
					System.err.println("Не пришел ответ о том, что " + username1 + " вошел в комнату " + room1);
	       		 	System.err.println("Ответ сервера: " + line1);
					return;
				}
			} else {
				System.err.println("Не пришел ответ о том, что " + username1 + " вошел в комнату " + room1);
	        	System.err.println("Ответ сервера: " + line1);
	            return;
			}

			System.out.println("Пользователь " + username2 + " входит в комнату");
			out2.writeUTF("req: attach");
			out2.writeUTF("room: " + room1);
			out2.flush();
			read2();
			if ( line2.startsWith("resp: attached") ) {
				read2();
				if ( line2.startsWith("room: " + room1 ) ) {
					;
				} else {
					System.err.println("Не пришел ответ о том, что " + username2 + " вошел в комнату " + room1 );
					System.err.println("Ответ сервера: " + line2);
					return;
				}
			} else {
				System.err.println("Не пришел ответ о том, что " + username2 + " вошел в комнату " + room1 );
	        	System.err.println("Ответ сервера: " + line2);
	            return;
			}



			System.out.println("Получаем сообщение о том, что " + username2 + " вошел в комнату");
			read1();
			if ( line1.startsWith("resp: room_user_in") ) {
				read1();
				if ( line1.startsWith("room: " + room1 ) ) {
					read1();
					if ( line1.startsWith("name: " + username2) ) {
						;
					} else {
						System.err.println("Не пришел ответ о том, что " + username2 + " вошел в комнату " + room1 );
						System.err.println("Ответ сервера: " + line1);
						return;
					}
				} else {
					System.err.println("Не пришел ответ о том, что " + username2 + " вошел в комнату " + room1 );
					System.err.println("Ответ сервера: " + line1);
					return;
				}
			} else {
				System.err.println("Не пришел ответ о том, что " + username2 + " вошел в комнату " + room1 );
	        	System.err.println("Ответ сервера: " + line1);
	            return;
			}

			System.out.println("Получаем сообщение о том, что " + username1 + " вошел в комнату");
			read2();
			if ( line2.startsWith("resp: room_user_in") ) {
				read2();
				if ( line2.startsWith("room: " + room1 ) ) {
					read2();
					if ( line2.startsWith("name: " + username1) ) {
						;
					} else {
						System.err.println("Не пришел ответ о том, что " + username1 + " вошел в комнату " + room1 );
						System.err.println("Ответ сервера: " + line2);
						return;
					}
				} else {
					System.err.println("Не пришел ответ о том, что " + username1 + " вошел в комнату " + room1 );
					System.err.println("Ответ сервера: " + line2);
					return;
				}
			} else {
				System.err.println("Не пришел ответ о том, что " + username1 + " вошел в комнату " + room1 );
	        	System.err.println("Ответ сервера: " + line2);
	            return;
			}







			out1.writeUTF("req: roommsg");
			out1.writeUTF("room: " + room1);
			out1.writeUTF("msg: " + msg1);
			out1.flush();

			System.out.println("Получаем сообщение от " + username1 + " для пользователя " + username1);
			read1();
			if ( line1.startsWith("resp: room_msg") ) {
				read1();
				if ( line1.startsWith("room: " + room1) ) {
					read1();
					if ( line1.startsWith("name: " + username1) ) {
						read1();
						if ( line1.startsWith("msg: " + msg1) ) {
							;
						} else {
							System.err.println("Не удалось получить сообщение от пользователя " + username1 + " в комнате " + room1);
							System.err.println("Ответ сервера: " + line1);
						}
					} else {
						System.err.println("Не удалось получить сообщение от пользователя " + username1 + " в комнате " + room1);
						System.err.println("Ответ сервера: " + line1);
					}
				} else {
					System.err.println("Не удалось получить сообщение от пользователя " + username1 + " в комнате " + room1);
					System.err.println("Ответ сервера: " + line1);
				}
			} else {
				System.err.println("Не удалось получить сообщение от пользователя " + username1 + " в комнате " + room1);
				System.err.println("Ответ сервера: " + line1);
			}



			System.out.println("Получаем сообщение от " + username1 + " для пользователя " + username2);
			read2();
			if ( line2.startsWith("resp: room_msg") ) {
				read2();
				if ( line2.startsWith("room: " + room1) ) {
					read2();
					if ( line2.startsWith("name: " + username1) ) {
						read2();
						if ( line1.startsWith("msg: " + msg1) ) {
							;
						} else {
							System.err.println("Не удалось получить сообщение от пользователя " + username1 + " в комнате " + room1);
							System.err.println("Ответ сервера: " + line2);
						}
					} else {
						System.err.println("Не удалось получить сообщение от пользователя " + username1 + " в комнате " + room1);
						System.err.println("Ответ сервера: " + line2);
					}
				} else {
					System.err.println("Не удалось получить сообщение от пользователя " + username1 + " в комнате " + room1);
					System.err.println("Ответ сервера: " + line2);
				}
			} else {
				System.err.println("Не удалось получить сообщение от пользователя " + username1 + " в комнате " + room1);
				System.err.println("Ответ сервера: " + line2);
			}





            System.out.println("Завершение тестирования и удаление временных аккаунтов");

			delUser1();

			/* после удаления временного аккаунта,
			 * второму должно придти уведомление о том,
			 * что пользователь вышел из комнаты и из сети */
			System.out.println("Получаем уведомление о том, что пользователь " + username1 + " вышел из комнаты");
			read2();
            if ( line2.startsWith("resp: room_user_out") ) {
            	read2();
            	if ( line2.startsWith("room: " + room1 ) ) {
            		read2();
		        	if ( line2.startsWith( "name: " + username1 ) ) {
		        		;
                	} else {
	                	System.err.println("От пользователя " + username1 + " не пришло сообщение о том, что он вышел из комнаты");
			        	System.err.println("Ответ сервера: " + line2);
			            return;
	                }
                } else {
                	System.err.println("От пользователя " + username1 + " не пришло сообщение о том, что он вышел из комнаты");
		        	System.err.println("Ответ сервера: " + line2);
		            return;
                }
            } else {
	            	System.err.println("От пользователя " + username1 + " не пришло сообщение о том, что он вышел из комнаты");
		        	System.err.println("Ответ сервера: " + line2);
		            return;
            }


			System.out.println("Получаем уведомление о том, что пользователь " + username1 + " вышел из сети");
			read2();
            if ( line2.startsWith("resp: logout") ) {
            	read2();
            	if ( line2.startsWith("name: " + username1) ) {
		        	;
                } else {
                	System.err.println("От пользователя " + username1 + " не пришло сообщение о том, что он вышел из сети");
		        	System.err.println("Ответ сервера: " + line2);
		            return;
                }
            } else {
	            	System.err.println("От пользователя " + username1 + " не пришло сообщение о том, что он вышел из сети");
		        	System.err.println("Ответ сервера: " + line2);
		            return;
            }


            System.out.println("Уведомление пришло");

			delUser2();
			dbUsers();

			System.out.println("Тест пройден успешно");
			return;

		} catch (Exception x) {
			System.out.println("Disconnected...");
			x.printStackTrace();
		}
	}
}

