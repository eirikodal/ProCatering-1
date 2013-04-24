package procatering;

import database.Database;

import javax.swing.*;
import java.sql.Timestamp;

public class Employee extends Person {
	private int employeeId = -1;
	private final String dob;
	private Database db;
	private Order order;
	private Subscription subscription;

	public Employee(int empId, String fn, String ln, String phone, int pCode, String dob, String mail) {
		super(fn, ln, phone, mail, pCode);
		this.dob = dob;
		this.employeeId = empId;
		db = new Database();
	}

	public Employee(String fn, String ln, String phone, int pCode, String dob, String mail) {
		//Person(String fn, String ln, String phone, String mail, int pCode)
		super(fn, ln, phone, mail, pCode);
		this.dob = dob;
		db = new Database();
	}

	public Employee(Employee e) {
		//Person(String fn, String ln, String phone, String mail, int pCode)
		super(e.getFirstName(), e.getLastName(), e.getPhoneNumber(), e.getEmail(), e.getPostalCode());
		dob = e.getDob();
	}


	/**
	 * Method creates a new employee object with data found in the database on given <i>employeeId</i>
	 *
	 * @param employeeId an integer object that contains the employee id.
	 * @return an employee object with data from the database. If no information found, the method returns <i>null</i>.
	 * @author Jørgen Lien Sellæg
	 */
	public static Employee getEmployee(Integer employeeId) {
		Database db = new Database();
		if (employeeId == null || employeeId <= 0) {
			return null;
		}
		return new Employee(db.getEmployee(employeeId));
	}

	/**
	 * Adds an empoyee
	 *
	 * @param fn    String object
	 * @param ln    String object
	 * @param phone String object
	 * @param pCode Integer
	 * @param dob   String object
	 * @param mail  String object
	 * @param pw    String object
	 * @return true if sucsessfully added, else it will return false
	 */
	public boolean addEmployee(String fn, String ln, String phone, int pCode, String dob, String mail, String pw) {
		fn = Helper.capitalFirst(fn);
		if (db.addEmployee(new Employee(fn, ln, phone, pCode, dob, mail), pw)) {
			return true;
		}
		return false;
	}

	/**
	 * Edits a dish to send in a new dish object and the name of the old dish
	 *
	 * @param name     String object
	 * @param newPrice Double
	 * @param cost     Double
	 * @param oldName  String object
	 * @return true if sucsessfully updated, else it will return false
	 */
	public boolean editDish(String name, double newPrice, double cost, String oldName) {
		name = Helper.capitalFirst(name);
		if (db.editDish(new Dish(name, newPrice, cost), oldName)) {
			return true;
		}
		return false;
	}

	/**
	 * type are of object type String
	 *
	 * @return type
	 */

	public String getDob() {
		return dob;
	}

	public int getEmployeeId() {
		return employeeId;
	}

	/**
	 * Edit a Employee by sending inn a new Employee object. The employee object require employee ID
	 *
	 * @param input
	 * @return
	 */
	public boolean updateEmployee(Employee input) {
		if (input.getEmployeeId() < 0) {
			return false;
		}
		if (db.updateEmployee(input)) {
			return true;
		}
		return false;
	}

	public boolean changeEmployeePassword(String input, int id) {
		if (id < 0) {
			if (db.changeEmployeePassword(input, id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * toString are of the object String
	 *
	 * @return class information and Person class toString: <br>
	 *         returns: Person toString
	 */
	@Override
	public String toString() {
		return "Employee{" + super.toString() + '}';
	}

	/* TODO:
	 * addOrder ok
	 * order.addContent ok
	 * addSubscription
	 * subscription.addContent
	 */
	public boolean createOrder(int c_id) {
		if (c_id > 0) {
			order = new Order(c_id, this.getEmployeeId(), "Active");
			return true;
		}
		return false;
	}

	public boolean addOrderContent(Timestamp deliverDate) {
		if (deliverDate.after(order.getCurrentDate())) {
			if (order.addOrderContent(deliverDate)) {
				return true;
			}
			return false;
		}
		return false;
	}

	public boolean addOrderDishes(Dish dish, int qty, int index) {
		if (dish != null) {
			order.addDish(dish, qty, index);
			return true;
		}
		return false;
	}

	public boolean createSubscription(int c_id) {
		if (c_id > 0) {
			subscription = new Subscription(c_id, this.getEmployeeId());
			return true;
		}
		return false;
	}

	public boolean addSubscriptionStartDate(Timestamp startDate) {
		if (startDate != null && startDate.after(subscription.getOrderDate())) {
			subscription.addStartDate(startDate);
			return true;
		}
		return false;
	}

	public boolean addSubscriptionEndDate(Timestamp endDate) {
		if (endDate != null && endDate.after(subscription.getStartDate())) {
			subscription.addEndDate(endDate);
			return true;
		}
		return false;
	}

	//TODO SKRIV FERDIG METODEN!!!
	public boolean addNewDish(Dish dish, DefaultListModel catNames, DefaultListModel ingredient) {
		return false;
	}

	public boolean addSubscriptionContent(String weekday, Timestamp deliveryTime) {
		if (weekday != null && deliveryTime.after(subscription.getOrderDate())) {
			subscription.addOrderContent(weekday, deliveryTime);
			return true;
		}
		return false;
	}

	public boolean addSubscription() {
		DefaultListModel<OrderContent> check1 = subscription.getContent();
		Timestamp check2 = subscription.getStartDate();
		if (check1 != null && check1.size() > 0 && check2 != null) {
			if (db.addSubscription(new Subscription(subscription))) {
				subscription = null;
				return true;
			}
			return false;
		}
		return false;
	}
}
