
package database;

import gui.Gui;
import procatering.*;

import javax.swing.*;
import java.sql.*;

import static procatering.Helper.DATABASE_NUMBER;


/**
 * @author TEAM 17
 */
public class Database {
	private final String username = "q20";
	private final String password = "mFW6fL3";
	private final String URL = "jdbc:mysql://mysql.stud.aitel.hist.no:3306/q20";
	private final DBClean cleanup = new DBClean();

	//TODO create documentation for all classes
	public Database() {
		try {
			String dbDriver = "com.mysql.jdbc.Driver";
			Class.forName(dbDriver);
		} catch (Exception error) {
			System.out.println(error);
		}
	}

	/**
	 * addCustomer
	 *
	 * @param input a customer object's attributes are inserted to customer table in DB
	 * @return false if the addition of the customer somehow fails. Returns true if everything went fine.
	 */
	public boolean addCustomer(Customer input) {
		if (input == null) {
			return false;
		}
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (
					PreparedStatement prepStat = con.prepareStatement("INSERT INTO customer"
							+ "(firstname, lastname, clean_fn, clean_ln, phonenumber, email, address, postalcode, note)"
							+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)")
			) {
				con.setAutoCommit(false);
				prepStat.setString(1, input.getFirstName());
				prepStat.setString(2, input.getLastName());
				prepStat.setString(3, input.getFirstName().toUpperCase());
				prepStat.setString(4, input.getLastName().toUpperCase());
				prepStat.setString(5, input.getPhoneNumber());
				prepStat.setString(6, input.getEmail());
				prepStat.setString(7, input.getAddress());
				prepStat.setInt(8, input.getPostalCode());
				prepStat.setString(9, input.getNote());
				prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				return true;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return false;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return false;
		}
	}

	/**
	 * Creates a list of customers for the GUI.
	 *
	 * @param input Takes letters or numbers and searches the customer table for matches within first name, last name, phone number, postal code, and corporate name and number
	 * @return DefaultListModel containing customer objects
	 */
	public DefaultListModel<Customer> findCustomer(String input) {
			/*Adds wildcard on both sides of the search phrase*/
		input = "%" + input + "%";

            /*tries to setup a connection to the database*/
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
				/*tries to create a prepared statement.*/
			try (
					PreparedStatement prepStat = con.prepareStatement("SELECT * FROM customer LEFT JOIN corporate_register " +
							"ON customer.corporatenumber = corporate_register.corporatenumber " +
							"WHERE clean_fn LIKE ? OR clean_ln LIKE ? OR phonenumber LIKE ?" +
							"OR postalcode LIKE ? OR corporatename LIKE ? OR customer.corporatenumber LIKE ?")
			) {
					/*Inserts the input search string to the SQL in the prepared statement*/
				con.setAutoCommit(false);
				prepStat.setString(1, input);       //clean firstname
				prepStat.setString(2, input);       //clean lastname
				prepStat.setString(3, input);       //phone number
				prepStat.setString(4, input);       //postal code
				prepStat.setString(5, input);       //corporate number / customer id
				prepStat.setString(6, input);       //corporate name
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);

                    /* Declares and initializes the return DefaultListModel*/
				DefaultListModel<Customer> output = new DefaultListModel<>();

                    /*creates the objects that has matching attributes to the search phrase*/
				while (rs.next()) {
					Customer inputObject = new Customer(rs.getString("address"), rs.getString("firstname"), rs.getString("lastname"), rs.getString("phonenumber"), rs.getString("email"), rs.getInt("postalcode"), rs.getString("note"), rs.getInt("customer_id"));
							/*if the customer object has a corporate connection, add to attributes*/
					if (rs.getString("corporate_register.corporatename") != null) {
						inputObject.setCorporateNum(rs.getInt("corporate_register.corporatenumber"));
						inputObject.setCorporateName(rs.getString("corporate_register.corporatename"));
					}
                            /*Adds the object to the DefaultListModel*/
					output.addElement(inputObject);
				}
                    /*returns the List of cusomer objects with a match to the search phrase*/
				return output;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

	public boolean addSubscription(procatering.Subscription sub) {
		return false;
		//TODO: MAKE method .. Ted
	}

	public Customer getCustomer(int cid) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (
					PreparedStatement prepStat = con.prepareStatement("SELECT * FROM customer WHERE customer_id = ?")
			) {
				con.setAutoCommit(false);
				prepStat.setInt(1, cid);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				rs.first();
				return new Customer(rs.getString("address"), rs.getString("firstname"), rs.getString("lastname"), rs.getString("phonenumber"), rs.getString("email"), rs.getInt("postalcode"), rs.getString("note"), rs.getInt("customer_id"));
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

	/**
	 * Uses a Employee object and a string with password to add a new employee into the database
	 *
	 * @param input
	 * @param pw
	 * @return true if successfully added, else it will return false.
	 *         employee_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	 */
	public boolean addEmployee(Employee input, String pw) {
		if (input == null || employeeExist(input) == true) {
			return false;
		}
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("INSERT INTO employee (firstname, lastname, clean_fn, clean_ln, password, phonenumber, postalcode, dob, email) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				con.setAutoCommit(false);
				prepStat.setString(1, Helper.capitalFirst(input.getFirstName()));
				prepStat.setString(2, Helper.capitalFirst(input.getLastName()));
				prepStat.setString(3, input.getFirstName().toUpperCase());
				prepStat.setString(4, input.getLastName().toUpperCase());
				prepStat.setString(5, SecurityChecker.MD5(pw));
				prepStat.setString(6, input.getPhoneNumber());
				prepStat.setInt(7, input.getPostalCode());
				prepStat.setString(8, input.getDob());
				prepStat.setString(9, input.getEmail().toLowerCase());

				prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				return true;
			} catch (Exception ePrepState) {
//				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return false;
			}
		} catch (SQLException eCon) {
//			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return false;
		}
	}

	/**
	 * getEmploye creates a new Employee object by using the employee id to identify the employe in the database.
	 *
	 * @param id
	 * @return Employe if correct employee id have been given, else it will return null.
	 */
	public Employee getEmployee(int id) {
		if (id < -1) {
			return null;
		}
        /*creating strings for every attribute in the customer object:*/
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (
					PreparedStatement prepStat = con.prepareStatement("SELECT * FROM employee WHERE employee_id = ?")
			) {
				prepStat.setInt(1, id);
				try (ResultSet rs = prepStat.executeQuery()) {

					while (rs.next()) {
						int employeeId = rs.getInt("employee_id");
						String fn = rs.getString("firstname");
						String ln = rs.getString("lastname");
						String dob = rs.getString("dob");
						String phone = rs.getString("phonenumber");
						String mail = rs.getString("email");
						int pCode = rs.getInt("postalcode");

						if (fn == null || ln == null || dob == null || phone == null || mail == null) {
							return null;
						} else {
							return new Employee(employeeId, fn, ln, phone, pCode, dob, mail);
						}
					}

				} catch (SQLException ePrepState) {

					gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
					cleanup.dbRollback(con);
					return null;
				}

			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
		return null;
	}

	//TODO FIX DOK: updateEmployee
	public boolean updateEmployee(procatering.Employee input) {
		if (input == null) {
			return false;
		}
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("UPDATE employee SET firstname = ?, lastname = ?, clean_fn = ?, clean_ln = ?, phonenumber = ?, postalcode = ?, dob = ?, email = ? WHERE employee_id = ?;")) {
				con.setAutoCommit(false);
				prepStat.setString(1, input.getFirstName());
				prepStat.setString(2, input.getLastName());
				prepStat.setString(3, input.getFirstName().toUpperCase());
				prepStat.setString(4, input.getLastName().toUpperCase());
				prepStat.setString(5, input.getPhoneNumber());
				prepStat.setInt(6, input.getPostalCode());
				prepStat.setString(7, input.getDob());
				prepStat.setString(8, input.getEmail());
				prepStat.setInt(9, input.getEmployeeId());
				int check = prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				if (check > 0) {
					return true;
				}
				return false;
			} catch (SQLException ePrepState) {
				System.out.println(ePrepState);
//				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return false;
			}
		} catch (SQLException eCon) {
//			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return false;
		}
	}

	/**
	 * Checks if an employee are already in the database. The method will check for firstname, lastname, and phonenumber of an Employee object.
	 *
	 * @param employee Employee Object
	 * @return false if the employee does not exist and true if employee exist or the statement is null
	 *         <p/>
	 */
	public boolean employeeExist(Employee employee) {
		if (employee == null) {
			return true;
		}
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (
					PreparedStatement prepStat = con.prepareStatement("SELECT * FROM employee WHERE clean_fn = ? AND clean_ln = ? AND phonenumber = ?")
			) {
				prepStat.setString(1, employee.getFirstName().toUpperCase());
				prepStat.setString(2, employee.getLastName().toUpperCase());
				prepStat.setString(3, employee.getPhoneNumber());
				try (ResultSet rs = prepStat.executeQuery()) {
					while (rs.next()) {
						return true;
					}

				} catch (SQLException ePrepState) {
					gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
					cleanup.dbRollback(con);
					return true;
				}

			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return true;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return true;
		}

		return false;
	}

	/**
	 * Uses the employee id to identify a row in the database and updated the password with the given string.
	 *
	 * @param input String Object
	 * @param id    Integer
	 * @return return true if successfully updated, else it will return false.
	 */
	public boolean changeEmployeePassword(String input, int id) {
		if (input == null) {
			return false;
		}
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("UPDATE employee SET password = ? WHERE employee_id = ?")) {
				con.setAutoCommit(false);
				prepStat.setString(1, input);
				prepStat.setInt(2, id);
				prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				return true;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return false;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return false;
		}
	}

	//TODO lag dokumentasjon
	public boolean updateCustomer(Customer input, int cid) {
		if (input == null || cid < 1) {
			return false;
		}
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (
					PreparedStatement prepStat = con.prepareStatement("UPDATE customer "
							+ "SET firstname = ?, lastname = ?, clean_fn = ?, clean_ln = ?, phonenumber = ?, email = ?, address = ?, postalcode = ?"
							+ " WHERE customer_id = ?")
			) {
				con.setAutoCommit(false);
				prepStat.setString(1, input.getFirstName());
				prepStat.setString(2, input.getLastName());
				prepStat.setString(3, input.getFirstName().toUpperCase());
				prepStat.setString(4, input.getLastName().toUpperCase());
				prepStat.setString(5, input.getPhoneNumber());
				prepStat.setString(6, input.getEmail());
				prepStat.setString(7, input.getAddress());
				prepStat.setInt(8, input.getPostalCode());
				prepStat.setInt(9, cid);
				prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				return true;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return false;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return false;
		}
	}

	//TODO Lag dokumentasjon
	public String getPasswordFromDatabase(int id) {
		String query = "SELECT password FROM employee WHERE employee_id = ?";
		try (Connection con = DriverManager.getConnection(URL, username, password);
			 PreparedStatement prep = con.prepareStatement(query)) {
			prep.setInt(1, id);
			ResultSet ans = prep.executeQuery();
			ans.first();
			return ans.getString(1);
		} catch (SQLException e) {
			Gui.showErrorMessage(DATABASE_NUMBER, 100, e);
			return null;
		}
	}

	//TODO lag dokumentasjon EIRIK! / add ordercontent også.

	public boolean addOrder(procatering.Order order) {
		if (order == null) {
			return false;
		}

		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (//TODO add a for-loop to add ordercontent/dishes
				 PreparedStatement prepStat = con.prepareStatement("INSERT INTO orders (employee_id, customer_id, time_of_order, status) VALUES (?,?,?,?)")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, order.getEmployeeId());
				prepStat.setInt(2, order.getCustomerId());
				prepStat.setTimestamp(3, order.getOrderDate());
				prepStat.setString(3, order.getStatus());
				con.commit();
				con.setAutoCommit(true);
				return true;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return false;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return false;
		}
	}

	//TODO DOK!
	public DefaultListModel<procatering.Order> getOrder(int cid) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM orders LEFT JOIN customer ON customer.customer_id = orders.customer_id WHERE orders.customer_id = ?")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, cid);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<procatering.Order> output = new DefaultListModel<>();
				while (rs.next()) {
					output.addElement(new procatering.Order(rs.getInt("customer_id"), rs.getInt("employee_id"), rs.getString("status"), rs.getTimestamp("time_of_order")));
				}
				return output;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}


	/**
	 * The methode check the dishName length, it has to be less than 255 signs.
	 *
	 * @param dishName String object
	 * @return Dish object, or null if dishName is not found in the database.
	 */

	public Dish getDish(String dishName) {
		if (Helper.stringChecker(dishName)) {
			try (Connection con = DriverManager.getConnection(URL, username, password)) {
				try (
						PreparedStatement prepStat = con.prepareStatement("SELECT * FROM dish WHERE dishname = ?")
				) {
					prepStat.setString(1, Helper.capitalFirst(dishName));
					try (ResultSet rs = prepStat.executeQuery()) {

						while (rs.next()) {
							int id = rs.getInt("dish_id");
							String name = rs.getString("dishname");
							double price = rs.getDouble("price");
							double cost = rs.getDouble("cost");

							if (name != null) {
								return new Dish(name, price, cost, id);
							} else {
								return null;
							}
						}

					} catch (SQLException ePrepState) {
						gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
						cleanup.dbRollback(con);
						return null;
					}

				} catch (SQLException ePrepState) {
					gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
					cleanup.dbRollback(con);
					return null;
				}
			} catch (SQLException eCon) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
				return null;
			}

		}
		return null;
	}

	/**
	 * edit an dish in the database. Require that the dish object obtain dish_id. The mothode only change price and cost of the dish.
	 *
	 * @param dish Dish object
	 * @return true if sucsessfully updated even if non of the value are different from what is allready in the database, else it will return false.
	 */
	public boolean editDish(Dish dish) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("UPDATE dish SET price = ?, cost = ? WHERE dish_id = ?")) {
				con.setAutoCommit(false);
				prepStat.setDouble(1, dish.getPrice());
				prepStat.setDouble(2, dish.getCost());
				prepStat.setInt(3, dish.getID());
				prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				return true;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return false;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return false;
		}

	}

	/**
	 * Search metode for dishes. uses a string value and include every dish with that value inside the dishname.
	 *
	 * @param value
	 * @return a DefaultListModel with dishes that include the string value in dishname, or null if something fails.
	 */
	public DefaultListModel<procatering.Dish> findDishes(String value) {
		value = "%" + value + "%";
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM dish WHERE dishname LIKE ?")) {
				con.setAutoCommit(false);
				prepStat.setString(1, value);
				ResultSet rs = prepStat.executeQuery();
				DefaultListModel<procatering.Dish> output = new DefaultListModel<>();
				while (rs.next()) {
					output.addElement(new procatering.Dish(rs.getString("dishname"), rs.getDouble("price"), rs.getDouble("cost")));
				}
				con.commit();
				con.setAutoCommit(true);
				return output;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

	/**
	 * Changes the status attrite value in the database from 1 to 0 for the given dishname.
	 *
	 * @param name String object
	 * @return true if sucsessfully updated; return false if nothing have been updated or if something went wrong.
	 */
	public boolean hideDish(String name) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("UPDATE dish SET status = ? WHERE dishname = ?")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, 0);
				prepStat.setString(2, Helper.capitalFirst(name));
				int check = prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				if (check == 0) {
					return false;
				}
				return true;

			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return false;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return false;
		}
	}

	/**
	 * Check the number of dishes sold of a given dish.
	 *
	 * @param dishId
	 * @returnes a integer of the number of dishes if it exists, 0 if it don't.
	 */
	public int numbOfDishes(int dishId) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT dish_id FROM order_dish WHERE dish_id = ?")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, dishId);
				ResultSet rs = prepStat.executeQuery();
				int count = 0;
				while (rs.next()) {
					count++;
				}
				con.commit();
				con.setAutoCommit(true);
				return count;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return 0;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return 0;
		}
	}


	/**
	 * Gives the top 10 dishes arranged by most sold in a given time frame.
	 *
	 * @param from is the start date and time.
	 * @param to   is the end date and time.
	 * @returnes strings in a DefaultListModel.
	 */
	public DefaultListModel<String> topDishesFromTo(Timestamp from, Timestamp to) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT time_of_order, order_dish.order_id, dish.dish_id, order_dish.dish_id, dishname, price, cost, SUM(amount) AS amountsum , SUM(order_dish.dish_id) AS 'in orders' FROM dish LEFT OUTER JOIN order_dish ON order_dish.dish_id = dish.dish_id JOIN orders WHERE time_of_order > ? AND time_of_order < ? GROUP BY dish.dish_id ORDER BY amountsum DESC LIMIT 10;")) {
				con.setAutoCommit(false);
				prepStat.setTimestamp(1, from);
				prepStat.setTimestamp(2, to);
				ResultSet rs = prepStat.executeQuery();
				DefaultListModel output = new DefaultListModel<>();
				while (rs.next()) {
					output.addElement("Number of dishes: " + rs.getInt("amountsum") + ". Dish: " + rs.getString("dishname") + ". Dish price: " + rs.getDouble("price") + " NOK" + ". Dish cost: " + rs.getDouble("cost") + " NOK");
				}
				con.commit();
				con.setAutoCommit(true);
				return output;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

	/**
	 * Gives the top five dishes arranged by most sold.
	 *
	 * @returnes strings in a DefaultListModel.
	 */
	public DefaultListModel<String> topDishes() {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT order_dish.order_id, dish.dish_id, order_dish.dish_id, dishname, price, cost, SUM(amount) AS amountsum , SUM(order_dish.dish_id) AS 'in orders' FROM dish LEFT OUTER JOIN order_dish ON order_dish.dish_id = dish.dish_id GROUP BY dish.dish_id ORDER BY amountsum DESC")) {
				con.setAutoCommit(false);
				ResultSet rs = prepStat.executeQuery();
				DefaultListModel output = new DefaultListModel<>();
				while (rs.next()) {
					if (rs.getRow() < 6) {
						output.addElement("Number of dishes: " + rs.getInt("amountsum") + ". Dish: " + rs.getString("dishname") + ". Dish price: " + rs.getDouble("price") + " NOK" + ". Dish cost: " + rs.getDouble("cost") + " NOK");
					}
				}
				con.commit();
				con.setAutoCommit(true);
				return output;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}


	/**
	 * Gives the top 10 dishes arranged by profit.
	 *
	 * @returnes strings in a DefaultListModel.
	 */
	public DefaultListModel<String> topProfit() {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT time_of_order, order_dish.order_id, dish.dish_id, order_dish.dish_id, dishname, price, cost, SUM(price - cost) AS profit , SUM(order_dish.dish_id) AS 'in orders' FROM dish LEFT OUTER JOIN order_dish ON order_dish.dish_id = dish.dish_id JOIN orders GROUP BY dish.dish_id ORDER BY profit DESC LIMIT 10;")) {
				con.setAutoCommit(false);
				ResultSet rs = prepStat.executeQuery();
				DefaultListModel output = new DefaultListModel<>();
				while (rs.next()) {
					output.addElement("Profit: " + rs.getDouble("profit") + " NOK" + ". Dish: " + rs.getString("dishname") + ". Dish price: " + rs.getDouble("price") + " NOK" + ". Dish cost: " + rs.getDouble("cost") + " NOK");
				}
				con.commit();
				con.setAutoCommit(true);
				return output;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}


	/**
	 * Lists out all the orders in a given time frame and arranges them in order of date.
	 *
	 * @param from is the start date and time.
	 * @param to   is the end date and time.
	 * @returnes strings in a DefaultListModel.
	 */
	public DefaultListModel<String> getOrdersFromTo(Timestamp from, Timestamp to) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT order_dish.order_id, customer.lastname, customer.firstname, employee.employee_id, time_of_order, COUNT(orders.order_id) \"nu\", SUM(amount*quantity) \"sum\" FROM orders LEFT OUTER JOIN order_dish ON order_dish.order_id = orders.order_id JOIN employee ON employee.employee_id = orders.employee_id JOIN customer ON customer.customer_id = orders.customer_id JOIN dish ON order_dish.dish_id = dish.dish_id WHERE time_of_order > ? AND time_of_order < ? GROUP BY order_dish.order_id ORDER BY orders.time_of_order DESC")) {
				con.setAutoCommit(false);
				prepStat.setTimestamp(1, from);
				prepStat.setTimestamp(2, to);
				ResultSet rs = prepStat.executeQuery();
				DefaultListModel output = new DefaultListModel<>();
				while (rs.next()) {
					output.addElement("Order ID: " + rs.getInt("order_id") + ". Order sum: " + rs.getDouble("sum") + " NOK" + ". Kunde: " + rs.getString("lastname") + ", " + rs.getString("firstname") + ". Employee ID: " + rs.getInt("employee_id") + ". Order time: " + rs.getTimestamp("time_of_order"));
				}
				con.commit();
				con.setAutoCommit(true);
				return output;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}


	/**
	 * Gives the top 10 customers arranged by money used.
	 *
	 * @returnes strings in a DefaultListModel.
	 */
	public DefaultListModel<String> bigSpender() {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT customer.customer_id, customer.lastname, customer.firstname, COUNT(customer.customer_id) 'nu', SUM(amount*quantity) 'sum' FROM orders LEFT OUTER JOIN order_dish ON order_dish.order_id = orders.order_id JOIN customer ON customer.customer_id = orders.customer_id GROUP BY customer.customer_id ORDER BY sum DESC LIMIT 10")) {
				con.setAutoCommit(false);
				ResultSet rs = prepStat.executeQuery();
				DefaultListModel output = new DefaultListModel<>();
				while (rs.next()) {
					output.addElement("Customer ID: " + rs.getInt("Customer ID: " + rs.getInt("customer_id") + ". Name: " + rs.getString("lastname") + ", " + rs.getString("firstname") + ". Sum of all orders: " + rs.getDouble("sum") + " NOK. In " + rs.getString("nu") + " order(s)"));
				}
				con.commit();
				con.setAutoCommit(true);
				return output;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

	/**
	 * Adds a new category into the database. This methode only uses the name of the category to create one.
	 *
	 * @param name String object
	 * @return
	 */
	public boolean addCategory(String name) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("INSERT INTO categories (catname) VALUES (?);")
			) {
				con.setAutoCommit(false);
				prepStat.setString(1, Helper.capitalFirst(name));
				prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				return true;
			} catch (SQLException ePrepState) {
//					gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return false;
			}
		} catch (SQLException eCon) {
//				gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return false;
		}


	}

	//TODO DOK!
	public String findPostPlace(Integer postInt) {
		String query = "SELECT place FROM postalcode WHERE postalcode = ?";
		try (Connection con = DriverManager.getConnection(URL, username, password);
			 PreparedStatement prep = con.prepareStatement(query)) {
			prep.setInt(1, postInt);
			ResultSet ans = prep.executeQuery();
			ans.first();
			return String.valueOf(ans.getString("place"));
		} catch (SQLException e) {
			System.out.println(e);
			return "N/A";
		}
	}

	/**
	 * Adds a dish to the database
	 *
	 * @param dish Dish object
	 * @return true if sucsessfully added, else it will return false.
	 */
	public boolean addDish(Dish dish) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("INSERT INTO dish (dishname, price, cost, status) VALUES (?, ?, ?, ?)")) {
				con.setAutoCommit(false);
				prepStat.setString(1, Helper.capitalFirst(dish.getName()));
				prepStat.setDouble(2, dish.getPrice());
				prepStat.setDouble(3, dish.getCost());
				prepStat.setInt(4, 1);
				prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				return true;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return false;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return false;
		}


	}

	/**
	 * Method getCategories returns a DefaltListModel containing a list of all the categories.
	 *
	 * @return a DefaultListModel with string, or null if no string found in the database
	 */
	public DefaultListModel getCategories() {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM categories ORDER BY catname")) {
				con.setAutoCommit(false);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<Category> output = new DefaultListModel<>();
				while (rs.next()) {
					Category cat = new Category(rs.getInt("cat_id"), rs.getString("catname"));
					output.addElement(cat);
				}
				return output;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

	/**
	 * Uses the first String to find the row in the database, and change the name to the second string
	 *
	 * @param name
	 * @param newname
	 * @return true if sucsesfully updated, else it will return false.
	 */
	public boolean editCategory(String name, String newname) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("UPDATE categories SET catname = ? WHERE catname = ?")
			) {
				con.setAutoCommit(false);
				prepStat.setString(1, Helper.capitalFirst(newname));
				prepStat.setString(2, Helper.capitalFirst(name));
				prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				return true;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return false;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return false;
		}

	}

	/**
	 * Removes a category from the database, based on the string value
	 *
	 * @param name string object
	 * @return true f sucsessfully removed, else it will return false.
	 */

	public boolean removeCategory(String name) {
		if (getCategory(name).getCategoryID() > -1) {
			int id = getCategory(name).getCategoryID();
			DefaultListModel<Dish> dishId = getAllDishesInACategory(id);
			try (Connection con = DriverManager.getConnection(URL, username, password)) {
				for (int i = 0; i < dishId.getSize(); i++) {
					try (PreparedStatement prepStat = con.prepareStatement("DELETE FROM cat_dish WHERE cat_id = ? AND dish_id = ?")) {
						con.setAutoCommit(false);
						prepStat.setInt(1, id);
						prepStat.setInt(2, dishId.get(i).getID());
						prepStat.executeUpdate();
					}
				}
				try (PreparedStatement prepStatTo = con.prepareStatement("DELETE FROM categories WHERE cat_id = ?")) {
					con.setAutoCommit(false);
					prepStatTo.setInt(1, id);
					prepStatTo.executeUpdate();
					con.commit();
					con.setAutoCommit(true);
					return true;

				} catch (SQLException ePrepState) {
					System.out.println(ePrepState);
//                    gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
					cleanup.dbRollback(con);
					return false;
				}
			} catch (SQLException eCon) {
//                gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
				return false;
			}
		}
		return false;

	}


	public boolean customerExist(Customer customer) {
		String sql = "SELECT clean_fn, phonenumber FROM customer WHERE clean_fn = ? AND phonenumber = ?";
		try (Connection con = DriverManager.getConnection(URL, username, password);
			 PreparedStatement prepStat = con.prepareStatement(sql)) {
			prepStat.setString(1, customer.getFirstName().toUpperCase());
			prepStat.setString(2, customer.getPhoneNumber());
			ResultSet res = prepStat.executeQuery();
			return res.next();
		} catch (SQLException e) {
			return false;
		}
	}


	/**
	 * Adds an ingredient into the database, useing only the name of the ingredient
	 *
	 * @param name String object
	 * @return true if sucsessfully added, else it will return false
	 */
	public boolean addIngredient(String name) throws SQLException {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("INSERT INTO ingredient (ingredientname) VALUES (?)")) {
				con.setAutoCommit(false);
				prepStat.setString(1, Helper.capitalFirst(name));
				prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				return true;
			}
		}
	}

	/**
	 * Insert dish_id and ingredient_id into dish_ingredient tabel.
	 *
	 * @param dishname       String object
	 * @param ingredientname String object
	 * @return true if sucsessfully inserted, else false
	 */
	public boolean insertDishIngredient(String dishname, String ingredientname) throws SQLException {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("INSERT INTO dish_ingredient (dish_id, ingredient_id) VALUES (?,?)")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, getDish(Helper.capitalFirst(dishname)).getID());
				prepStat.setInt(2, getIngredient(Helper.capitalFirst(ingredientname)).getIngredientID());
				prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				return true;
			}
		}

	}

	/**
	 * get an ingredient object
	 *
	 * @param ingredientName String object
	 * @return a Ingredient object, or null if that ingredient does not exist.
	 */
	public Ingredient getIngredient(String ingredientName) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM ingredient WHERE ingredientname = ?")) {
				prepStat.setString(1, Helper.capitalFirst(ingredientName));
				try (ResultSet rs = prepStat.executeQuery()) {

					while (rs.next()) {
						int id = rs.getInt("ingredient_id");
						String name = rs.getString("ingredientname");

						if (name != null) {
							return new Ingredient(id, name);
						}
					}

				} catch (SQLException ePrepState) {
//                            gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
					cleanup.dbRollback(con);
					return null;
				}

			} catch (SQLException ePrepState) {
//                        gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
//                    gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}


		return null;
	}

	/**
	 * Find and return a Category object using it's category name
	 *
	 * @param categoryName String object
	 * @return a Category object, else null
	 */
	public Category getCategory(String categoryName) {
		if (Helper.stringChecker(categoryName)) {
			try (Connection con = DriverManager.getConnection(URL, username, password)) {
				try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM categories WHERE catname = ?")) {
					prepStat.setString(1, Helper.capitalFirst(categoryName));
					try (ResultSet rs = prepStat.executeQuery()) {

						while (rs.next()) {
							int id = rs.getInt("cat_id");
							String name = rs.getString("catname");

							if (name != null) {
								return new Category(id, name);
							}
						}

					} catch (SQLException ePrepState) {

//                            gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
						cleanup.dbRollback(con);
						return null;
					}

				} catch (SQLException ePrepState) {
//                        gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
					cleanup.dbRollback(con);
					return null;
				}
			} catch (SQLException eCon) {
//                    gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
				return null;
			}
		}
		return null;
	}

	/**
	 * Insert cat_id and dish_id into cat_dish tabel
	 *
	 * @param dishname String object
	 * @param catname  String object
	 * @return true if sucsessfully added, else false.
	 */
	public boolean insertDishCat(String dishname, String catname) throws SQLException {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("INSERT INTO cat_dish (cat_id, dish_id) VALUES (?,?)")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, getCategory(Helper.capitalFirst(catname)).getCategoryID());
				prepStat.setInt(2, getDish(Helper.capitalFirst(dishname)).getID());
				prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				return true;
			}
		}
	}

	/**
	 * Checks if a category exist in the database (BE AWARE OF THE RETURN VALUE!)
	 *
	 * @param name String object
	 * @return true if category exist or if an exception have occured, else it will return false.
	 */
	public boolean cateogryExist(String name) throws SQLException {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM categories WHERE catname = ?")
			) {
                    /*Inserts the input search string to the SQL in the prepared statement*/
				con.setAutoCommit(false);
				prepStat.setString(1, Helper.capitalFirst(name));
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);

				while (rs.next()) {
					String catname = rs.getString("catname");
					if (catname.equals(name)) {
						return true;
					}
				}
                    
                    /*returns the List of cusomer objects with a match to the search phrase*/

			}
		}

		return false;
	}

	/**
	 * Checks if an ingredient exist in the database (BE AWARE OF THE RETURN VALUE!)
	 *
	 * @param name String object
	 * @return true if category exist or if an exception have occured, else it will return false.
	 */
	public boolean ingredientExist(String name) throws SQLException {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM ingredient WHERE ingredientname = ?")
			) {
                    /*Inserts the input search string to the SQL in the prepared statement*/
				con.setAutoCommit(false);
				prepStat.setString(1, name);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);

				while (rs.next()) {
					String ingredientname = rs.getString("ingredientname");
					if (ingredientname.equals(name)) {
						return true;
					}
				}
                    
                    /*returns the List of cusomer objects with a match to the search phrase*/

			}

		}
		return false;
	}

	/**
	 * Search the database for an existing dish. (BE AWERE OF THE RETURN VALUES)
	 *
	 * @param name String object
	 * @return true if the dish exist or an sql exception occur, else it will return false.
	 */
	public boolean dishExist(String name) throws SQLException {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM dish WHERE dishname = ?")
			) {
                    /*Inserts the input search string to the SQL in the prepared statement*/
				con.setAutoCommit(false);
				prepStat.setString(1, name);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);

				while (rs.next()) {
					String dishname = rs.getString("dishname");
					if (dishname.equals(name)) {
						return true;
					}
				}
                    
                    /*returns the List of cusomer objects with a match to the search phrase*/

			}
			return false;
		}
	}

	/**
	 * Find dishes who belongs to one spesefic category id.
	 *
	 * @param id Integer Category id
	 * @return //TODO docuemtation
	 */
	public DefaultListModel<Dish> getDishes(int id) {
		if (id < 0) {
			return null;
		}
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM dish LEFT OUTER JOIN cat_dish ON dish.dish_id = cat_dish.dish_id WHERE cat_id = ? AND status = 1;")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, id);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<Dish> output = new DefaultListModel<>();
				while (rs.next()) {
					output.addElement(new Dish(rs.getString("dishname"), rs.getDouble("price"), rs.getDouble("cost"), rs.getInt("status")));
				}

				return output;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

	//TODO GM
	public boolean removeEmployee(int id) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("DELETE FROM employee_types WHERE employee_id = ?")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, id);
				prepStat.executeUpdate();

				try (PreparedStatement prepStatTo = con.prepareStatement("DELETE FROM employee WHERE employee_id = ?")) {
					prepStatTo.setInt(1, id);
					prepStatTo.executeUpdate();
					con.commit();
					con.setAutoCommit(true);

					return true;
				}
			} catch (SQLException ePrepState) {
				System.out.println(ePrepState);
//					gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return false;
			}
		} catch (SQLException eCon) {
//				gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return false;
		}

	}

	//TODO GM
	public boolean activateDish(String name) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("UPDATE dish SET status = ? WHERE dishname = ?")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, 1);
				prepStat.setString(2, Helper.capitalFirst(name));
				int check = prepStat.executeUpdate();
				con.commit();
				con.setAutoCommit(true);
				if (check == 0) {
					return false;
				}
				return true;

			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return false;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return false;
		}
	}

	//TODO GM
	public DefaultListModel<Dish> getAllDishesInACategory(int id) {
		if (id < 0) {
			return null;
		}
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM dish LEFT OUTER JOIN cat_dish ON dish.dish_id = cat_dish.dish_id WHERE cat_id = ?")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, id);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<Dish> output = new DefaultListModel<>();
				while (rs.next()) {
					output.addElement(new Dish(rs.getString("dishname"), rs.getDouble("price"), rs.getDouble("cost"), rs.getInt("dish_id")));
				}
				return output;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				cleanup.dbRollback(con);
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}


}
      
