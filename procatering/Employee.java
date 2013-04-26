package procatering;

import database.Database;

import javax.swing.*;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Class Employee
 * The active class of proCatering.
 * @author Team17
 */
public class Employee extends Person {
	private int employeeId;
	private String dob;
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
                employeeId = -1;
	}

    /** simple copy-constructor*/
	public Employee(Employee e) {
		super(e.getFirstName(), e.getLastName(), e.getPhoneNumber(), e.getEmail(), e.getPostalCode());
		dob = e.getDob();
		db = new Database();
                if(e.getEmployeeId()>-1){
                    employeeId = e.getEmployeeId();
                }else{
                employeeId = -1;
                }
	}

	/**
	 * Method getEmployee
     * Creates a new employee object with data found in the database on a given <i>employeeId</i>
	 * @param employeeId an integer object that contains the employee id.
	 * @return an employee object with data from the database. If no information found, the method returns <i>null</i>.
	 */
	public Employee getEmployee(Integer employeeId) {
		
		if (employeeId == null || employeeId < 0) {
			return null;
		}
                if(db.getEmployee(employeeId)!=null){
                    return new Employee(db.getEmployee(employeeId));
                }
		return null;
	}


	public Order getOrder() {
		return order;
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
                if(db.employeeExist(new Employee(fn, ln, phone, pCode, dob, mail))){
                    return false;
                }
		return db.addEmployee(new Employee(fn, ln, phone, pCode, dob, mail), pw);
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
	public boolean editDish(Dish dish, double newPrice, double newCost) {
                dish.setPrice(newPrice);
                dish.setCost(newCost);
                if(db.editDish(dish)){
                    return true;
                }
		return false;
	}
        
        public Dish getDish(String name){
            return db.getDish(name);
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
	 * @return //TODO fix docuemtation
	 */
	public boolean updateEmployee(Employee input) {
		if(db.updateEmployee(input)){
                    return true;
                }
                return false;
        }

	public boolean changeEmployeePassword(String input, int id) {
		if(input == null || id > -1){
                    if(db.getEmployee(id)==null){
                        return false;
                    }
                   
                    else if (db.changeEmployeePassword(input, id)) {
				return true;
			}
                }
		return false;
	}
        //TODO DOKUMENTASJON
        public DefaultListModel getDishes(int id){
                return db.getDishes(id);
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
			return order.addOrderContent(deliverDate);
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
         * Adds a new dish to the database, it will also add dish and category names that are not in the database.
         * @param dish Dish object
         * @param catNames DefaultListModel<String>
         * @param ingredient DefaultListModel<String>
         * @return true if sucsessfully added, else it will return false.
         */
        public boolean addNewDish(Dish dish, DefaultListModel<String> catNames, DefaultListModel<String> ingredient) {
            if(dish == null || catNames.isEmpty() || ingredient.isEmpty()){
                return false;
            }
            int en = 0;
            int to = 0;
            try{
                if(!db.dishExist(Helper.capitalFirst(dish.getName()))) {
                    for (int i = 0; i < catNames.getSize(); i++) {
                        if(!db.cateogryExist(Helper.capitalFirst(catNames.get(i)))){
                            db.addCategory(Helper.capitalFirst(catNames.get(i)));
                        }
                    }
                    for (int i = 0; i < ingredient.getSize(); i++) {
                        if(!db.ingredientExist(Helper.capitalFirst(ingredient.get(i)))){
                            db.addIngredient(Helper.capitalFirst(ingredient.get(i)));
                        }
                    }
                    
                    if(db.addDish(dish)){
                        for (int i = 0; i < catNames.getSize(); i++) {
                            db.insertDishCat(Helper.capitalFirst(dish.getName()), Helper.capitalFirst(catNames.get(i)));
                            en++;
                            
                        }
                        for (int i = 0; i < ingredient.getSize(); i++) {
                            db.insertDishIngredient(Helper.capitalFirst(dish.getName()), Helper.capitalFirst(ingredient.get(i)));
                            to++;
                        }
						return en == catNames.getSize() && to == ingredient.getSize();
					}
                }
            }catch (SQLException ePrepState) {
//                gui.Gui.showErrorMessage(Helper.DATABASE_NUMBER, 1, ePrepState);
                return false;
                
                
                
            }
            return false;
        }

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
    public boolean removeDish(String name){
        if(db.hideDish(Helper.capitalFirst(name))){
            return true;
        }
        return false;
    }
}
