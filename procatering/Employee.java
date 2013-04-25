package procatering;

import database.Database;
import javax.swing.*;
import java.sql.Timestamp;

/**
 * Class Employee
 * The active class of proCatering.
 * @author Team17
 */
public class Employee extends Person {
	private int employeeId = -1;
	private final String dob;
	private Database db;
	private Order order;
	private Subscription subscription;

    /**
     * Constructor, creates an object of Employee
     * Adds the parameters to attributes
     * Creates an object of Database
     * @param empId int employee id
     * @param fn String first name
     * @param ln String last name
     * @param phone String phone number
     * @param pCode int postCode (4digits)
     * @param dob String Date of Birth ( dd/mm/yyyy ) TODO make sure the format is correct.
     * @param mail String email
     */
    public Employee(int empId, String fn, String ln, String phone, int pCode, String dob, String mail) {
		super(fn, ln, phone, mail, pCode);
		this.dob = dob;
		this.employeeId = empId;
		db = new Database();
	}

    /**
     * Constructor, creates an object of Employee without an employee id. (needs to be inserted to DB for an id.)
     * Adds the parameters to attributes
     * Creates an object of Database
     * @param fn String first name
     * @param ln String last name
     * @param phone String phone number
     * @param pCode int postCode (4digits)
     * @param dob String Date of Birth ( dd/mm/yyyy ) TODO make sure the format is correct.
     * @param mail String email
     */
	public Employee(String fn, String ln, String phone, int pCode, String dob, String mail) {
		super(fn, ln, phone, mail, pCode);
		this.dob = dob;
		db = new Database();
	}

    /** simple copy-constructor*/
	public Employee(Employee e) {
		super(e.getFirstName(), e.getLastName(), e.getPhoneNumber(), e.getEmail(), e.getPostalCode());
		dob = e.getDob();
	}

	/**
	 * Method getEmployee
     * Creates a new employee object with data found in the database on a given <i>employeeId</i>
	 * @param employeeId an integer object that contains the employee id.
	 * @return an employee object with data from the database. If no information found, the method returns <i>null</i>.
	 */
	public static Employee getEmployee(Integer employeeId) {
		Database db = new Database();
		if (employeeId == null || employeeId <= 0) {
			return null;
		}
		return new Employee(db.getEmployee(employeeId));
	}

	/**
     * Method addEmployee
	 * Adds an employee to the database
	 *
	 * @param fn    String object first name
	 * @param ln    String object last name
	 * @param phone String object phone number
	 * @param pCode Integer post code
	 * @param dob   String object Date of Birth
	 * @param mail  String object eMail
	 * @param pw    String object password
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
     * Method editDish
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
     * Method updateEmployee
	 * Edit a Employee by sending inn a new Employee object. The employee object requires an employee ID
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
     * Method toString
	 * toString are of the object String
	 *
	 * @return class information and Person class toString: <br>
	 *         returns: Person toString
	 */
	@Override
	public String toString() {
		return "Employee{" + super.toString() +  '}';
	}

    /**
     * Method createOrder
     * Creates an order without any orderContent objects
     * Adds the employee id to the order, and sets the status to "Active"
     * @param c_id  customer id
     * @return boolean, returns true if executed successfully
     */
	public boolean createOrder(int c_id) {
		if (c_id > 0) {
			order = new Order(c_id, this.getEmployeeId(), "Active");
			return true;
		}
		return false;
	}

    /**
     * Method addOrderContent
     * Adds an orderContent object to the Order object's orderContent List.
     * @param deliverDate Timestamp containing the date and time the order is to be delivered.
     * @return boolean, returns true if executed successfully
     */
	public boolean addOrderContent(Timestamp deliverDate) {
		if (deliverDate.after(order.getCurrentDate())) {
			if (order.addOrderContent(deliverDate)) {
				return true;
			}
			return false;
		}
		return false;
	}

    /**
     * Method addOrderDishes
     * Adds a dish within: The Order object's orderContentLists' specified object.
     * @param dish Dish object that represents the dish that is to be added
     * @param qty The number of the given dish that is to be added
     * @param index Which OrderContent object within the Order objects content list that is affected.
     * @return boolean, returns true if executed successfully
     */
	public boolean addOrderDishes(Dish dish, int qty, int index) {
		if (dish != null) {
			order.addDish(dish, qty, index);
			return true;
		}
		return false;
	}

    /**
     * Method addOrder
     * Checks if the order contains the needed attributes
     * Adds the order to the database.
     * if successfully added, sets the order object to null.
     * @return boolean, returns true if executed successfully
     */
    public boolean addOrder() {     //TODO Check if we need to control other contents
        DefaultListModel<OrderContent> check = order.getOrderContent();
        if (check != null && check.size() > 0 ) {
            if (db.addOrder(new Order(order))) {
                order = null;
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Method createSubscription
     * initiates the Subscription object
     * @param c_id customer id the Subscription is ordered to.
     * @return boolean, returns true if executed successfully
     */
	public boolean createSubscription(int c_id) {
		if (c_id > 0) {
			subscription = new Subscription(c_id, this.getEmployeeId());
			return true;
		}
		return false;
	}

    /**
     * Method addSubscriptionStartDate
     * Adds the date of activation of the subscription.
     * @param startDate Timestamp of the date of activation.
     * @return boolean, returns true if executed successfully
     */
	public boolean addSubscriptionStartDate(Timestamp startDate) {
		if (startDate != null && startDate.after(subscription.getOrderDate())) {
			subscription.addStartDate(startDate);
			return true;
		}
		return false;
	}

    /**
     * Method addSubscriptionEndDate
     * Adds a date for termination of the subscription
     * @param endDate Timestamp of the date of termination
     * @return boolean, returns true if executed successfully
     */
	public boolean addSubscriptionEndDate(Timestamp endDate) {
		if (endDate != null && endDate.after(subscription.getStartDate())) {
			subscription.addEndDate(endDate);
			return true;
		}
		return false;
	}

    /**
     * Method addOrderContent
     * Adds an empty (without dishes) OrderContent object to the Subscription object's orderContent List.
     * @param weekday Which weekday the order is to be delivered repeatedly.
     * @param deliveryTime Timestamp containing the time the order is to be delivered repeatedly.
     * @return boolean, returns true if executed successfully
     */
	public boolean addSubscriptionContent(String weekday, Timestamp deliveryTime) {
		if (weekday != null && deliveryTime.after(subscription.getOrderDate())) {
			subscription.addOrderContent(weekday, deliveryTime);
			return true;
		}
		return false;
	}

    /**
     * Method addSubscription
     * Checks if the subscription contains the needed attributes
     * Adds the subscription to the database.
     * @return boolean, returns true if executed successfully
     */
    public boolean addSubscription() {     //TODO Check if we need to control other contents
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

	//TODO SKRIV FERDIG METODEN!!!
    public boolean addNewDish(Dish dish, DefaultListModel catNames, DefaultListModel ingredient) {
        return false;
    }
}