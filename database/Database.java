
package database;

import gui.Gui;
import procatering.*;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;

import static procatering.Helper.DATABASE_NUMBER;


/**
 * The database class contain all metodes with SQL commands to communicate with the database
 * <dl>
 * <dt>Constructor: modified default constructor</dt>
 * </dl>
 *
 * @author Team 17
 */
//TODO fix gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon); that are standing as a comment
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
					PreparedStatement prepStat = con.prepareStatement("INSERT INTO customer "
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

    /**
     * Adds a subscription to a customer
     * @param sub Is a subscription object
     * @return true if adding a subscription is successful, false if not
     */
	public boolean addSubscription(procatering.Subscription sub) {
		if (sub == null) {
			return false;
		}
		DefaultListModel<OrderContent> orderContent = sub.getContent();
		System.out.println("emp id: --->" + sub.getEmployeeId() + "<--");
		String sql = "INSERT INTO order_dish (order_id, dish_id, delivery, days, quantity, amount) VALUES (?,?,?,?,?,?)";
		String sql1 = "INSERT INTO orders (employee_id, customer_id, time_of_order, status, starts, ends) VALUES (?,?,?,?,?,?)";
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS);
				 PreparedStatement prepStat1 = con.prepareStatement(sql)) {
				con.setAutoCommit(false);
				prepStat.setInt(1, sub.getEmployeeId());
				prepStat.setInt(2, sub.getCustomerId());
				prepStat.setTimestamp(3, sub.getOrderDate());
				prepStat.setString(4, sub.getStatus());
				prepStat.setTimestamp(5, sub.getStartDate());
				prepStat.setTimestamp(6, sub.getEndDate());
				prepStat.executeUpdate();
				ResultSet rs = prepStat.getGeneratedKeys();
				rs.first();
				for (int i = 0; i < orderContent.size(); i++) {
					for (int j = 0; j < orderContent.get(i).countDishFish(orderContent.get(i).getDishes()).length; j++) {
						prepStat1.setInt(1, rs.getInt(1));
						prepStat1.setInt(2, sub.getContent().get(i).getDishes().get(j).getID());
						prepStat1.setTimestamp(3, sub.getContent().get(i).getDeliveryDate());
						prepStat1.setString(4, sub.getContent().get(i).getDeliveryDay());
						prepStat1.setInt(5, Integer.parseInt(orderContent.get(i).countDishFish(orderContent.get(i).getDishes())[j][1]));
						prepStat1.setDouble(6, Double.parseDouble(orderContent.get(i).countDishFish(orderContent.get(i).getDishes())[j][0]));
						prepStat1.addBatch();
					}
				}
				prepStat1.executeBatch();
				con.commit();
				con.setAutoCommit(true);
				return true;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				con.rollback();
				return false;
			}
		} catch (SQLException er) {
			System.out.println("connection failed: " + er);
		}
		return false;

		//TODO: MAKE method .. Ted
	}

    /**
     * Gets the customer with the given customer ID
     * @param cid The ID number of the desired customer
     * @return the customer object
     */
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
	 * @param input The desired employee object
	 * @param pw    The password that have been chosen
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
	 * getEmployee creates a new Employee object by using the employee id to identify the employee in the database.
	 *
	 * @param id The ID of the desired employee
	 * @return Employee object if correct employee id have been given, else it will return null.
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

    /**
     * Updates the information of an employee in the database
     * @param input The employee object with the updated information
     * @return true if successful, false if not
     */
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
	 * Checks if an employee are already in the database. The method will check for first name, last name, and phone number of an Employee object.
	 *
	 * @param employee Employee Object
	 * @return false if the employee does not exist and true if employee exist or the statement is null
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
	 * @param input String with the new password
	 * @param id    The ID of the employee
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

    /**
     * Updates the information of a customer in the database
     * @param input The customer object with the updated information
     * @param cid   The ID of the customer
     * @return true if the update is successful, false if not
     */
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

    /**
     * Gets the password from the employee with the given ID number
     * @param id The ID of the employee
     * @return a String with the password
     */
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

    /**
     * Gets the orders that a customer, with the given customer ID, have orderd
     * @param cid The desired customer in form of the customer ID
     * @return a DefaultListModel with all the orders
     */
	public DefaultListModel<Order> getOrder(int cid) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM orders LEFT JOIN customer ON customer.customer_id = orders.customer_id WHERE orders.customer_id = ?")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, cid);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<Order> output = new DefaultListModel<>();
				while (rs.next()) {
					output.addElement(new Order(rs.getInt("customer_id"), rs.getInt("employee_id"), rs.getString("status"), rs.getTimestamp("time_of_order")));
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
     * Adds dishes to the order content list and sets the time of the delivery
     * @param orderId   The order ID that the dishes are to be added to
     * @param delivery  The delivery date and time in form of a timestamp
     * @return a DefaultListModel with the dishlist
     */
	private DefaultListModel<procatering.Dish> createDishList(int orderId, Timestamp delivery) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM order_dish JOIN dish  ON (order_dish.dish_id = dish.dish_id AND order_dish.delivery = ? AND order_dish.order_id = ?)")) {
				con.setAutoCommit(false);

				prepStat.setTimestamp(1, delivery);
				prepStat.setInt(2, orderId);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<procatering.Dish> dishList = new DefaultListModel<>();
				while (rs.next()) {
					for (int i = 0; i < rs.getInt("order_dish.quantity"); i++) {
						dishList.addElement(
								new Dish(rs.getString("dish.dishname"), rs.getDouble("dish.price"), rs.getDouble("dish.cost"), rs.getInt("dish.dish_id"))
						);
					}
				}
				return dishList;
			} catch (SQLException ePrepState) {
				System.out.println("catch 1");
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				con.rollback();
				return null;
			}
		} catch (SQLException eCon) {
			System.out.println("catch 2 HERRE SJER DU ITJ");
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

    /**
     * Connects the order content with an order
     * @param orderId The ID of the order the order content is to be added to
     * @return a DefaultListModel with the order content
     */
	private DefaultListModel<procatering.OrderContent> createContentList(int orderId) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT DISTINCT order_dish.delivery FROM order_dish LEFT OUTER JOIN orders ON (order_dish.order_id = ? AND orders.order_id = ?)JOIN customer ON(customer.customer_id = orders.customer_id) WHERE days IS NULL ORDER BY order_dish.delivery ASC")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, orderId);
				prepStat.setInt(2, orderId);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<OrderContent> contentList = new DefaultListModel<>();
				while (rs.next()) {
					contentList.addElement(
							new OrderContent(rs.getTimestamp("order_dish.delivery"), createDishList(orderId, rs.getTimestamp("order_dish.delivery")))
					);
				}
				return contentList;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				con.rollback();
				return null;
			}
		} catch (SQLException eCon) {
			System.out.println("catch 2 HERRE SJER DU ITJ");
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

    /**
     * Adds dishes to the order content list and sets the day of the delivery. This is for subscriptions
     * @param orderId   The order ID that the dishes are to be added to
     * @param day       The day(s)of the week the subscription is to be delivered
     * @return a DefaultListModel with the dish list
     */
	private DefaultListModel<procatering.Dish> createDishList(int orderId, String day) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM order_dish JOIN dish  ON (order_dish.dish_id = dish.dish_id AND order_dish.days = ? AND order_dish.order_id = ?)")) {
				con.setAutoCommit(false);

				prepStat.setString(1, day);
				prepStat.setInt(2, orderId);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<procatering.Dish> dishList = new DefaultListModel<>();
				while (rs.next()) {
					for (int i = 0; i < rs.getInt("order_dish.quantity"); i++) {
						dishList.addElement(
								new Dish(rs.getString("dish.dishname"), rs.getDouble("dish.price"), rs.getDouble("dish.cost"), rs.getInt("dish.dish_id"))
						);
					}
				}
				return dishList;
			} catch (SQLException ePrepState) {
				System.out.println("catch 1");
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				con.rollback();
				return null;
			}
		} catch (SQLException eCon) {
			System.out.println("catch 2 HERRE SJER DU ITJ");
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

    /**
     * Connects the order content with an subscription order
     * @param orderId The ID of the subscription order the order content is to be added to
     * @return a DefaultListModel with the order content
     */
	private DefaultListModel<procatering.OrderContent> createSubscriptionContentList(int orderId) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT DISTINCT order_dish.days, order_dish.delivery FROM order_dish LEFT OUTER JOIN orders ON (order_dish.order_id = ? AND orders.order_id = ?)JOIN customer ON(customer.customer_id = orders.customer_id) WHERE days IS NOT NULL ORDER BY order_dish.order_id ASC")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, orderId);
				prepStat.setInt(2, orderId);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<OrderContent> contentList = new DefaultListModel<>();
				while (rs.next()) {
					contentList.addElement(
							new OrderContent(rs.getString("order_dish.days"), rs.getTimestamp("order_dish.delivery"), createDishList(orderId, rs.getString("order_dish.days")))
					);
				}
				return contentList;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				con.rollback();
				return null;
			}
		} catch (SQLException eCon) {
			System.out.println("catch 2 HERRE SJER DU ITJ");
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

    /**
     * Gets all the subscriptions from the database
     * @return a DefaultListModel with all the subscriptions
     */
	public DefaultListModel<Subscription> getAllSubscriptions() {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT DISTINCT orders.order_id, orders.customer_id, orders.employee_id, orders.status, orders.time_of_order, orders.order_id  FROM orders LEFT OUTER JOIN order_dish ON (order_dish.order_id = orders.order_id) WHERE days IS NOT NULL")) {
				con.setAutoCommit(false);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<Subscription> orderList = new DefaultListModel<>();
				while (rs.next()) {
					orderList.addElement(
							new Subscription(rs.getInt("orders.order_id"), rs.getInt("orders.employee_id"), rs.getInt("orders.customer_id"), rs.getTimestamp("orders.time_of_order"), createSubscriptionContentList(rs.getInt("orders.order_id")), rs.getString("orders.status"))
					);
				}
				return orderList;
			} catch (SQLException ePrepState) {
				System.out.println("catch 1");
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				con.rollback();
				return null;
			}
		} catch (SQLException eCon) {
			System.out.println("catch 2 HERRE SJER DU ITJ");
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

    /**
     * Gives out all the orders, sorted by most recently added first
     * @return a DefaultListModel with all the orders
     */
	public DefaultListModel<Order> getAllOrders2() {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT DISTINCT orders.order_id, orders.customer_id, orders.employee_id, orders.status, orders.time_of_order, orders.order_id  FROM orders LEFT OUTER JOIN order_dish ON (order_dish.order_id = orders.order_id) WHERE days IS NULL ORDER BY orders.time_of_order DESC")) {
				con.setAutoCommit(false);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<Order> orderList = new DefaultListModel<>();
				while (rs.next()) {
					orderList.addElement(
							new Order(rs.getInt("orders.order_id"), rs.getInt("orders.customer_id"), rs.getInt("orders.employee_id"), rs.getString("orders.status"), rs.getTimestamp("orders.time_of_order"), createContentList(rs.getInt("orders.order_id")))
					);
				}
				return orderList;
			} catch (SQLException ePrepState) {
				System.out.println("catch 1");
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				con.rollback();
				return null;
			}
		} catch (SQLException eCon) {
			System.out.println("catch 2 HERRE SJER DU ITJ");
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

    /**
     * Adds an order to a customer in the database
     * @param order The order object with all the needed atributes
     * @return true if adding of the order was successful, false if not
     */
	public boolean addOrder(Order order) {
		if (order == null) {
			return false;
		}
		DefaultListModel<OrderContent> orderContent = order.getOrderContent();
		String sql = "INSERT INTO order_dish (order_id, dish_id, delivery, days, quantity, amount) VALUES (?,?,?,?,?,?)";
		String sql1 = "INSERT INTO orders (employee_id, customer_id, time_of_order, status) VALUES (?,?,?,?)";
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS);
				 PreparedStatement prepStat1 = con.prepareStatement(sql)) {
				con.setAutoCommit(false);
				prepStat.setInt(1, order.getEmployeeId());
				prepStat.setInt(2, order.getCustomerId());
				prepStat.setTimestamp(3, order.getCreationTime());
				prepStat.setString(4, order.getStatus());
				prepStat.executeUpdate();
				ResultSet rs = prepStat.getGeneratedKeys();
				rs.first();


				for (int i = 0; i < orderContent.size(); i++) {
					for (int j = 0; j < orderContent.get(i).countDishFish(orderContent.get(i).getDishes()).length; j++) {
						prepStat1.setInt(1, rs.getInt(1));
						prepStat1.setInt(2, order.getOrderContent().get(i).getDishes().get(j).getID());
						prepStat1.setTimestamp(3, order.getOrderContent().get(i).getDeliveryDate());
						prepStat1.setString(4, order.getOrderContent().get(i).getDeliveryDay());
						prepStat1.setInt(5, Integer.parseInt(orderContent.get(i).countDishFish(orderContent.get(i).getDishes())[j][1]));
						prepStat1.setDouble(6, Double.parseDouble(orderContent.get(i).countDishFish(orderContent.get(i).getDishes())[j][0]));
						prepStat1.addBatch();
					}
				}
				prepStat1.executeBatch();

				con.commit();
				con.setAutoCommit(true);
				return true;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				con.rollback();
				return false;
			}
		} catch (SQLException er) {
			System.out.println("connection failed: " + er);
		}
		return false;
	}

    /**
     * Finds all the orders a spesific employee have added
     * @param e_id The id of the employee
     * @return a DefaultListModel with all the orders
     */
	public DefaultListModel<procatering.Order> getAllOrders(int e_id) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM order_dish LEFT OUTER JOIN orders ON (order_dish.order_id = orders.order_id)JOIN customer ON(customer.customer_id = orders.customer_id AND order_dish.days IS NULL) JOIN dish ON(dish.dish_id = order_dish.dish_id) ORDER BY order_dish.order_id ASC")) {
				con.setAutoCommit(false);
				prepStat.setInt(1, e_id);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<procatering.Order> orderList = new DefaultListModel<>();
				DefaultListModel<procatering.OrderContent> contentList = new DefaultListModel<>();
				DefaultListModel<procatering.Dish> dishList = new DefaultListModel<>();
				int activeRow = 1;
				int lastOrderId = 0;
				String checkbreak = "break ble ikke kjørt";
				rs.last();
				int length = rs.getRow();
				rs.first();
				System.out.println("radlengde: " + length);
				while (activeRow <= length) { // ADD ORDERS TO DLM
					System.out.println("1");
					Timestamp lastTimestamp = rs.getTimestamp("order_dish.delivery");
					contentList = new DefaultListModel<>();
					dishList = new DefaultListModel<>();
					rs.absolute(activeRow);

					if (rs.getInt("orders.order_id") == lastOrderId) {
						System.out.println("2");//ADD CONTENT TO DLM
						System.out.println("order_id: " + rs.getInt("orders.order_id"));
						int test = 1;
						while (rs.getTimestamp("order_dish.delivery").equals(lastTimestamp) && rs.getInt("order_dish.order_id") == lastOrderId) {
							//ADD DISHES TO DLM
							System.out.println("3");
							dishList.addElement(new Dish(rs.getString("dish.dishname"), rs.getDouble("dish.price"), rs.getDouble("dish.cost")));
							if (rs.getRow() < length) {
								rs.next();
							} else {
								checkbreak = "break ble kjørt";
								break;
							}

							System.out.println("whileloop 3: round #" + test);
							test++;
						}
						System.out.println(checkbreak);
						rs.absolute(activeRow);
						System.out.println("4");
						System.out.println(dishList.get(0).getName());
						contentList.addElement((new OrderContent(rs.getTimestamp("order_dish.delivery"), copyDishList(dishList))));
					} else {
						System.out.println("5");
						contentList.addElement((new OrderContent(rs.getTimestamp("order_dish.delivery"), copyDishList(dishList))));
						lastOrderId = rs.getInt("orders.order_id");
						activeRow--;
					}
					System.out.println("6");
					System.out.println("contentlistcheck: " + contentList.get(0).getDeliveryDay());
					orderList.addElement(new Order(rs.getInt("orders.order_id"), rs.getInt("orders.customer_id"), e_id, rs.getString("status"), rs.getTimestamp("time_of_order"), copyContentList(contentList)));
					activeRow++;
					System.out.println("activerow: " + activeRow);
					//output.addElement(new procatering.Order(rs.getInt("customer_id"), rs.getInt("employee_id"), rs.getString("status"), rs.getTimestamp("time_of_order")));
				}
				System.out.println("1337");
				return orderList;
			} catch (SQLException ePrepState) {
				System.out.println("catch 1");
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				con.rollback();
				return null;
			}
		} catch (SQLException eCon) {
			System.out.println("catch 2 HERRE SJER DU ITJ");
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}

    /**
     * Copies the order content list. If you want a order content list with a different memory location
     * @param input The desired order content list
     * @return a DefaultListModel with the order content
     */
	private DefaultListModel<OrderContent> copyContentList(DefaultListModel<OrderContent> input) {
		DefaultListModel<OrderContent> output = new DefaultListModel<>();
		for (int i = 0; i < input.size(); i++) {
			output.addElement(input.get(i));
		}
		return output;
	}

    /**
     * Copies the dish list. If you want a dish list with a different memory location
     * @param input The desired dish list
     * @return a DefaultListModel with the dish list
     */
	private DefaultListModel<Dish> copyDishList(DefaultListModel<Dish> input) {
		DefaultListModel<Dish> output = new DefaultListModel<>();
		for (int i = 0; i < input.size(); i++) {
			output.addElement(input.get(i));
		}
		return output;
	}

	/**
	 * The method check the dishName length, it has to be less than 255 signs.
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
						con.rollback();
						return null;
					}

				} catch (SQLException ePrepState) {
					gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
					con.rollback();
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
	 * edit an dish in the database. Require that the dish object obtain dish_id. The mothod only change price and cost of the dish.
	 *
	 * @param dish Dish object
	 * @return true if successfully updated even if non of the value are different from what is already in the database, else it will return false.
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
	 * Search method for dishes. uses a string value and include every dish with that value inside the dishname.
	 *
	 * @param value the value to be serached for.
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
	 * Changes the status attribute value in the database from 1 to 0 for the given dishname.
	 *
	 * @param name String object
	 * @return true if successfully updated; return false if nothing have been updated or if something went wrong.
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
	 * @param dishId the dish ID
	 * @return a integer of the number of dishes if it exists, 0 if it don't.
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
	 * @return strings in a DefaultListModel.
	 */
	public DefaultListModel<String> topDishesFromTo(Timestamp from, Timestamp to) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT time_of_order, order_dish.order_id, dish.dish_id, order_dish.dish_id, dishname, price, cost, SUM(amount) AS amountsum , SUM(order_dish.dish_id) AS 'in orders' FROM dish LEFT OUTER JOIN order_dish ON order_dish.dish_id = dish.dish_id JOIN orders WHERE time_of_order > ? AND time_of_order < ? GROUP BY dish.dish_id ORDER BY amountsum DESC LIMIT 10;")) {
				con.setAutoCommit(false);
				prepStat.setTimestamp(1, from);
				prepStat.setTimestamp(2, to);
				ResultSet rs = prepStat.executeQuery();
				DefaultListModel<String> output = new DefaultListModel<>();
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
	 * @return Strings in a DefaultListModel.
	 */
	public DefaultListModel<String[]> topDishes() {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT order_dish.order_id, dish.dish_id, dishname, price, cost, SUM(quantity) AS amountsum , COUNT(order_dish.dish_id) AS 'in orders', SUM(quantity)*(price-cost) AS income FROM dish LEFT OUTER JOIN order_dish ON order_dish.dish_id = dish.dish_id GROUP BY dish.dish_id ORDER BY amountsum DESC")) {
				con.setAutoCommit(false);
				ResultSet rs = prepStat.executeQuery();
				DefaultListModel<String[]> output = new DefaultListModel<>();
				while (rs.next()) {
					if (rs.getRow() < 6) {
						output.addElement(new String[]{rs.getString("dishname"), rs.getDouble("price") + "", rs.getDouble("cost") + "", rs.getInt("amountsum") + "", rs.getInt("in orders") + "", rs.getDouble("income") + ""});
					}
				}
				con.commit();
				con.setAutoCommit(true);
				return output;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				con.rollback();
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
	 * @return strings in a DefaultListModel.
	 */
	public DefaultListModel<String> topProfit() {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT time_of_order, order_dish.order_id, dish.dish_id, order_dish.dish_id, dishname, price, cost, SUM(price - cost) AS profit , SUM(order_dish.dish_id) AS 'in orders' FROM dish LEFT OUTER JOIN order_dish ON order_dish.dish_id = dish.dish_id JOIN orders GROUP BY dish.dish_id ORDER BY profit DESC LIMIT 10;")) {
				con.setAutoCommit(false);
				ResultSet rs = prepStat.executeQuery();
				DefaultListModel<String> output = new DefaultListModel<>();
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
	 * @return strings in a DefaultListModel.
	 */
	public DefaultListModel<String> getOrdersFromTo(Timestamp from, Timestamp to) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT order_dish.order_id, customer.lastname, customer.firstname, employee.employee_id, time_of_order, COUNT(orders.order_id) \"nu\", SUM(amount*quantity) \"sum\" FROM orders LEFT OUTER JOIN order_dish ON order_dish.order_id = orders.order_id JOIN employee ON employee.employee_id = orders.employee_id JOIN customer ON customer.customer_id = orders.customer_id JOIN dish ON order_dish.dish_id = dish.dish_id WHERE time_of_order > ? AND time_of_order < ? GROUP BY order_dish.order_id ORDER BY orders.time_of_order DESC")) {
				con.setAutoCommit(false);
				prepStat.setTimestamp(1, from);
				prepStat.setTimestamp(2, to);
				ResultSet rs = prepStat.executeQuery();
				DefaultListModel<String> output = new DefaultListModel<>();
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
	 * @return strings in a DefaultListModel.
	 */
	public DefaultListModel<String[]> bigSpender() {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT customer.customer_id, customer.lastname, customer.firstname, COUNT(customer.customer_id) 'nu', SUM(amount*quantity) 'sum' FROM orders LEFT OUTER JOIN order_dish ON order_dish.order_id = orders.order_id JOIN customer ON customer.customer_id = orders.customer_id GROUP BY customer.customer_id ORDER BY sum DESC LIMIT 10")) {
				con.setAutoCommit(false);
				ResultSet rs = prepStat.executeQuery();
				DefaultListModel<String[]> output = new DefaultListModel<>();
				while (rs.next()) {
					output.addElement(new String[]{rs.getString("firstname"), rs.getString("lastname"), rs.getDouble("nu") + "", rs.getString("sum")});
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
	 * Adds a new category into the database. This method only uses the name of the category to create one.
	 *
	 * @param name String object
	 * @return true if the category is successfully added. False is it fails.
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

    /**
     * Finds the postal name by postal code
     * @param postInt Represents the postal code
     * @return a String with the postal name
     */
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
	 * @return true if successfully added, else it will return false.
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
     * Gets all the ingredients from the database ordered by ingredient name
     * @return a DefaultListModel with the ingredients
     */
	public DefaultListModel<Ingredient> getIngredients() {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM ingredient ORDER BY ingredientname")) {
				con.setAutoCommit(false);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<Ingredient> output = new DefaultListModel<>();
				while (rs.next()) {
					Ingredient ing = new Ingredient(rs.getInt("ingredient_id"), rs.getString("ingredientname"));
					output.addElement(ing);
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
	 * @param name    the current name of the category.
	 * @param newname the new name of the category.
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
	 * @return true f successfully removed, else it will return false.
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

    /**
     * Checks if a customer exists by comparing first name and phone number
     * @param customer The object of an customer
     * @return true if customer exists, false if not
     */
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
	 * @param dishname       String object of the dish the indredient shall be putt in to
	 * @param ingredientname String object of the ingredient name
	 * @return true if successfully inserted, else false
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
	 * Insert cat_id(category id) and dish_id into cat_dish tabel
	 *
	 * @param dishname String object
	 * @param catname  String object
	 * @return true if successfully added, else false.
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
	 * @param name String object of the given category name
	 * @return true if category exist or if an exception have occurred, else it will return false.
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
                    
                    /*returns the List of customer objects with a match to the search phrase*/

			}
		}

		return false;
	}

	/**
	 * Checks if an ingredient exist in the database (BE AWARE OF THE RETURN VALUE!)
	 *
	 * @param name String object of the given ingredient
	 * @return true if category exist or if an exception have occurred, else it will return false.
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
	 * Search the database for an existing dish. (BE AWARE OF THE RETURN VALUES)
	 *
	 * @param name String object of the given dish
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
	 * Find dishes who belongs to one specific category id and are activated (not hided).
	 *
	 * @param id Integer Category id
	 * @return a DefaultListModel<Dish> with Dish objects that are active, else it will return null
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

	/**
	 * Delete a employee from the database using employee id.
	 *
	 * @param id Integer (Employee ID)
	 * @return true if successfully removed, else it will return false.
	 */
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


	/**
	 * Make a dish visible by changing dish status in the database to 1.
	 *
	 * @param name String object (Dish name)
	 * @return true if successfully hides the dish, else it will return false
	 */
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


	/**
	 * Find all dishes in one category, even if the dish are not active. (hided)
	 *
	 * @param id Integer( Category ID)
	 * @return return a DefaultListModel<Dish> with all the dish objects, else it will return null
	 */
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

    /**
     * Gives today's subscriptions. The subscriptions that is going to be delivered today
     * @param today a timestamp with the date that is today
     * @return a ArrayList with the subscriptions
     */
	public ArrayList<String[]> getTodaySubscription(String today) {
		ArrayList<String[]> output = new ArrayList<>();
		String sql = "SELECT order_dish.quantity, dish.dishname, order_dish.days, customer.firstname, customer.lastname, orders.starts, orders.ends FROM order_dish JOIN orders ON (orders.order_id = order_dish.order_id) JOIN dish ON (order_dish.dish_id = dish.dish_id) JOIN customer ON (orders.customer_id = customer.customer_id) WHERE orders.starts <= ? AND orders.ends >= ?";
		if (today == null)
			return null;
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement(sql)) {
				con.setAutoCommit(false);
				prepStat.setString(1, today);
				prepStat.setString(2, today);
				ResultSet rs = prepStat.executeQuery();
				while (rs.next()) {
					output.add(new String[]{rs.getInt(1) + "", rs.getString(2) + "", rs.getString(3), rs.getString(4), rs.getString(5)});
				}

				con.commit();
				con.setAutoCommit(true);
				return output;
			} catch (SQLException e) {
				System.out.println(e);
				con.rollback();
				return null;
			}
		} catch (SQLException er) {
			System.out.println(er);
			return null;
		}
	}

    /**
     * Gives today's orders. The orders that is going to be delivered today
     * @param today a timestamp with the date that is today
     * @return a ArrayList with the subscriptions
     */
	public ArrayList<String[]> getTodayOrder(String today) {
		ArrayList<String[]> output = new ArrayList<>();
		String sql = "SELECT order_dish.quantity, order_dish.delivery, dish.dishname, customer.firstname, customer.lastname FROM order_dish JOIN orders ON (orders.order_id = order_dish.order_id) JOIN dish ON (order_dish.dish_id = dish.dish_id) JOIN customer ON (orders.customer_id = customer.customer_id) WHERE order_dish.delivery LIKE ?";
		today = today + "%";
		if (today == null)
			return null;
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement(sql)) {
				con.setAutoCommit(false);
				prepStat.setString(1, today);
				ResultSet rs = prepStat.executeQuery();
				while (rs.next()) {
					output.add(new String[]{rs.getInt(1) + "", rs.getTimestamp(2) + "", rs.getString(3), rs.getString(4), rs.getString(5)});
				}

				con.commit();
				con.setAutoCommit(true);
				return output;
			} catch (SQLException e) {
				System.out.println(e);
				con.rollback();
				return null;
			}
		} catch (SQLException er) {
			System.out.println(er);
			return null;
		}
	}

	/**
	 * Find order by searching for firstname, lastname, phonenumber or postalcode. The name and order id length is cut because if order id are over 10 signs or the name are over 42 signs the string will be ugly.
	 *
	 * @param input String object
	 * @return DefaultListModel<String> with String object, else it will return null;
	 */
	public DefaultListModel<String> findOrder(String input) {
			/*Adds wildcard on both sides of the search phrase*/
		input = "%" + input + "%";

            /*tries to setup a connection to the database*/
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
				/*tries to create a prepared statement.*/
			try (
					PreparedStatement prepStat = con.prepareStatement("SELECT * FROM customer LEFT JOIN orders " +
							"ON customer.customer_id = orders.customer_id " +
							"WHERE clean_fn LIKE ? OR clean_ln LIKE ? OR phonenumber LIKE ?" +
							"OR postalcode LIKE ? ORDER BY order_id DESC")
			) {
					/*Inserts the input search string to the SQL in the prepared statement*/
				con.setAutoCommit(false);
				prepStat.setString(1, input);       //clean firstname
				prepStat.setString(2, input);       //clean lastname
				if (input.length() < 5) {
					prepStat.setString(3, "00000000");       //not a phonenumber
				} else {
					prepStat.setString(3, input);       //phone number
				}
				prepStat.setString(4, input);       //postal code
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);

                    /* Declares and initializes the return DefaultListModel*/
				DefaultListModel<String> output = new DefaultListModel<>();
                    
                    /*creates the objects that has matching attributes to the search phrase*/
				String inputObject = "Order ID:  Customer: Lastname, Firstname   Order Time:";
				output.addElement(inputObject);
				while (rs.next()) {
					//Break the loop if order_id = 0
					if (rs.getInt("order_id") <= 0) {
						break;
					}
					//Select the input and creates a string that is 64 signs long or more.
					inputObject = rs.getInt("order_id") + "";
					int space1 = 12;
					int space2 = 44;
					if (inputObject.length() + 2 < space1) {
						space1 = space1 - inputObject.length();
					} else {
						space1 = 2;
					}
					for (int i = 1; i < space1; i++) {
						inputObject += " ";
					}
					inputObject += rs.getString("lastname") + ", " + rs.getString("firstname");

					if (inputObject.length() + 2 < space2) {
						space2 = space2 - inputObject.length();
					} else {
						space2 = 2;
					}
					for (int i = 1; i < space2; i++) {
						inputObject += " ";
					}
					inputObject += rs.getTimestamp("time_of_order");
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

	/**
	 * @return a DefaultListModel of all the Employees in the database. Returns null if it fails somehow.
	 */
	public DefaultListModel<Employee> getEmployees() {
		String sql = "SELECT * FROM employee";
		DefaultListModel<Employee> emp = new DefaultListModel<>();
		try (Connection con = DriverManager.getConnection(URL, username, password);
			 PreparedStatement p = con.prepareStatement(sql);
			 ResultSet rs = p.executeQuery()) {

			while (rs.next()) {
				emp.addElement(new Employee(rs.getInt("employee_id"), rs.getString("firstname"), rs.getString("lastname"), rs.getString("phonenumber"), rs.getInt("postalcode"), rs.getString("dob"), rs.getString("email")));
			}
			return emp;

		} catch (SQLException e) {
			System.out.println(e);
			return null;
		}
	}

    /**
     * It's a income report that finds all the dishes that is sold and calculates the profit of the dishes
     * @param from  The start date
     * @param to    The end date
     * @return a ArrayList with all the dishes
     */
	public ArrayList<String[]> getInComeReport(String from, String to) {
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (PreparedStatement prepStat = con.prepareStatement("SELECT dish.dish_id, order_dish.amount, dish.cost, dish.dishname, SUM(quantity) AS 'sum', (order_dish.amount * SUM(quantity)) AS 'sum price', (dish.cost * SUM(quantity)) AS 'sum cost', ((order_dish.amount * SUM(quantity)) - (dish.cost * SUM(quantity))) AS 'profit' FROM dish LEFT JOIN order_dish ON(order_dish.dish_id = dish.dish_id) WHERE order_dish.delivery BETWEEN ? AND ? GROUP BY dish.dishname;")) {
				con.setAutoCommit(false);
				prepStat.setString(1, from);
				prepStat.setString(2, to);
				ResultSet rs = prepStat.executeQuery();
				ArrayList<String[]> output = new ArrayList<>();
				while (rs.next()) {
					output.add(new String[]{rs.getString("dishname") + "", rs.getInt("sum") + "", rs.getDouble("sum price") + "", rs.getDouble("sum cost") + "", rs.getDouble("profit") + ""});
				}
				con.commit();
				con.setAutoCommit(true);
				return output;
			} catch (SQLException ePrepState) {
				gui.Gui.showErrorMessage(DATABASE_NUMBER, 1, ePrepState);
				con.rollback();
				return null;
			}
		} catch (SQLException eCon) {
			gui.Gui.showErrorMessage(DATABASE_NUMBER, 2, eCon);
			return null;
		}
	}
}
