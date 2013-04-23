
package database;

import gui.Gui;
import procatering.Employee;
import procatering.Dish;

import javax.swing.*;
import java.sql.*;
import procatering.Helper;

import static procatering.Helper.DATABASE_NUMBER;


/**
 * @author TEAM 17
 */
public class Database {
	private final String username = "q20";
	private final String password = "mFW6fL3";
	private final String URL = "jdbc:mysql://mysql.stud.aitel.hist.no:3306/q20";
	private final String dbDriver = "com.mysql.jdbc.Driver";
//	private Connection connection;
//	private PreparedStatement preparedStatement;
	private final DBClean cleanup = new DBClean();

	//TODO create documentation for all classes
	public Database() {
		try {
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
	public boolean addCustomer(procatering.Customer input) {
		if (input == null) {
			return false;
		}
		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (
					PreparedStatement prepStat = con.prepareStatement("INSERT INTO customer"
							+ "(firstname, lastname, clean_fn, clean_ln, phonenumber, email, address, postalcode)"
							+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?)")
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
         * @param input Takes letters or numbers and searches the customer table for matches within first name, last name, phone number, postal code, and corporate name and number
         * @return DefaultListModel containing customer objects
         * @author Tedjk
         */
	public DefaultListModel<procatering.Customer> findCustomer(String input) {
            /*Adds wildcard on both sides of the search phrase*/
            input = "%"+input+"%";
            
            /*tries to setup a connection to the database*/
            try (Connection con = DriverManager.getConnection(URL, username, password)) {
                /*tries to create a prepared statement.*/
                try (
                    PreparedStatement prepStat = con.prepareStatement("SELECT * FROM customer LEFT JOIN corporate_register" +
                        "ON customer.corporatenumber = corporate_register.corporatenumber " +
                        "WHERE clean_fn LIKE ? OR clean_ln LIKE ? OR phonenumber LIKE ?" +
                        "OR postalcode LIKE ? OR corporatename LIKE ? OR customer.corporatenumber LIKE ?");
                ){
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
                    DefaultListModel<procatering.Customer> output = new DefaultListModel<>();
                    
                    /*creates the objects that has matching attributes to the search phrase*/
                    while (rs.next()) {
                            procatering.Customer inputObject = new procatering.Customer(rs.getString("address"), rs.getString("clean_fn"), rs.getString("clean_ln"), rs.getString("phonenumber"), rs.getString("email"), rs.getInt("postalcode"), rs.getInt("customer_id"));
                            /*if the customer object has a corporate connection, add to attributes*/
                                if(rs.getString("corporate_register.corporatename") != null){
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
	

	public procatering.Customer getCustomer(int cid) {
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
                        return new procatering.Customer(rs.getString("address"), rs.getString("clean_fn"), rs.getString("clean_ln"), rs.getString("phonenumber"), rs.getString("email"), rs.getInt("postalcode"), rs.getInt("customer_id"));
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

        //TODO:
    public boolean updateCustomer(){return false;}
    

    /**
     * Uses a Employee object and a string with password to add a new employee into the database
     * @param input
     * @param pw
     * @return true if sucsessfully added, else it will return false.
     */
    public boolean addEmployee(Employee input, String pw) {
        if(input == null){
            return false;
        }
        /*creating strings for every attribute in the customer object:*/
        //            String fnClean = input.getFirstName().toUpperCase();
        //            String lnClean = input.getLastName().toUpperCase();
        try(Connection con = DriverManager.getConnection(URL,username,password);){
            try(
                PreparedStatement prepStat = con.prepareStatement
				("INSERT INTO employee (type_id, firstname, lastname, clean_fn, clean_ln, password, phonenumber, postalcode, dob, email) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")){
                con.setAutoCommit(false);
                    prepStat.setString(1, input.getType());
                    prepStat.setString(2, input.getFirstName());
                    prepStat.setString(3, input.getLastName());
                    prepStat.setString(4, input.getFirstName().toUpperCase());
                                    prepStat.setString(6, SecurityChecker.MD5(pw));
                    prepStat.setString(5, input.getLastName().toUpperCase());
                    prepStat.setString(7, input.getPhoneNumber());
                    prepStat.setInt(8, input.getPostalCode());
                    prepStat.setString(9, input.getDob());
                    prepStat.setString(10, input.getEmail());
                    prepStat.executeUpdate();
                    con.commit();
                con.setAutoCommit(true);
                return true;
            }catch(Exception ePrepState){
                gui.Gui.showErrorMessage(DATABASE_NUMBER,1, ePrepState);
                cleanup.dbRollback(con);
                return false;
            }
        }catch(SQLException eCon){
            gui.Gui.showErrorMessage(DATABASE_NUMBER,2,eCon);
            return false;
        }
    }
    
    /**
     * getEmploye creates a new Employee object by using the employee id to identify the employe in the database.
     * @param id
     * @return Employe if correct employee id have been given, else it will return null.
     */
    public Employee getEmployee(int id) {
        if(id <1){
            return null;
        }
        /*creating strings for every attribute in the customer object:*/
        try(Connection con = DriverManager.getConnection(URL,username,password);){
            try(
                    PreparedStatement prepStat = con.prepareStatement("SELECT * FROM employee WHERE employee_id = ?")
			){
                        prepStat.setInt(1, id);
                try( ResultSet rs = prepStat.executeQuery();) {
                    
                    while(rs.next()){
                        int employeeId = rs.getInt("employee_id");
                        String type = rs.getString("type_id");
                        String fn = rs.getString("firstname");
                        String ln = rs.getString("lastname");
                        String dob = rs.getString("dob");
                        String phone = rs.getString("phonenumber");
                        String mail = rs.getString("mail");
                        int pCode = rs.getInt("postalcode");
                        
                        if(type!=null && fn != null && ln != null && dob != null
                                && phone != null && mail != null && pCode > 999 && pCode < 10000){
                            return new Employee(employeeId, type, fn, ln, phone, pCode, dob, mail);
                        }else {
                            return null;
                        }
                    } //String type, String fn, String ln, String phone, int pCode,String dob, String mail
                    
                }catch(SQLException ePrepState){
                    gui.Gui.showErrorMessage(DATABASE_NUMBER,1, ePrepState);
                    cleanup.dbRollback(con);
                    return null;
                }
                
            }catch(SQLException ePrepState){
                gui.Gui.showErrorMessage(DATABASE_NUMBER,1, ePrepState);
                cleanup.dbRollback(con);
                return null;
            }
        }catch(SQLException eCon){
            gui.Gui.showErrorMessage(DATABASE_NUMBER,2,eCon);
            return null;
        }
        return null;   /// WHY NOT UNREACHABLE!?
    }
    /**
     * Update an employee require an Employee object and an employee id. 
     * The employee object are supposed to be the updated version of an employee.
     * This mean that this methode only update an existing employee by identify the row in the database
     * with employee id and update each attribute with the Employee object.
     * @param input
     * @param id
     * @return true if sucsessfully updated, else it will return null.
     */
    public boolean updateEmployee(procatering.Employee input, int id) {
            if(input == null){
                return false;
            }
            /*creating strings for every attribute in the customer object:*/ 
//            String fnClean = input.getFirstName().toUpperCase();
//            String lnClean = input.getLastName().toUpperCase();
            try(Connection con = DriverManager.getConnection(URL,username,password);){
                try(
                    PreparedStatement prepStat = con.prepareStatement("Update customer "
                    + "SET type_id = ?, firstname = ?, lastname = ?, clean_fn = ?, clean_ln = ?, password = ?, dob = ?, email = ?"
                    +" WHERE employee_id = ?;");
                    ){
                        con.setAutoCommit(false);
                            prepStat.setString(1, input.getType());
                            prepStat.setString(2, input.getFirstName());
                            prepStat.setString(3, input.getLastName());
                            prepStat.setString(4, input.getFirstName().toUpperCase());
                            prepStat.setString(5, input.getLastName().toUpperCase());
                            prepStat.setString(6, input.getPhoneNumber());
                            prepStat.setInt(7, input.getPostalCode());
                            prepStat.setString(8, input.getDob());
                            prepStat.setString(9, input.getEmail());
                            prepStat.setInt(10, id);
                            prepStat.executeUpdate();
                        con.commit();
                        con.setAutoCommit(true);
                        return true;
                    }catch(SQLException ePrepState){
                        gui.Gui.showErrorMessage(DATABASE_NUMBER,1, ePrepState);    
                        cleanup.dbRollback(con);
                        return false;
                    }
                }catch(SQLException eCon){
                    gui.Gui.showErrorMessage(DATABASE_NUMBER,2,eCon);
                    return false;                    
                }
        } 
      /**
       * Uses the employee id to identify a row in the database and updatet the password with the given string.
       * @param input
       * @param id
       * @return return true if sucsessfully updatet, else it will return fasle.
       */
    public boolean changeEmployeePassword(String input, int id){
                  if(input == null){
                return false;
            }
            try(Connection con = DriverManager.getConnection(URL,username,password)){
                try(
                    PreparedStatement prepStat = con.prepareStatement("Update customer "
                    + "SET password = ?"
                    +" WHERE employee_id = ?")
                    ){
                        con.setAutoCommit(false);
                            prepStat.setString(1, input);
                            prepStat.setInt(2, id);
                            prepStat.executeUpdate();
                        con.commit();
                        con.setAutoCommit(true);
                        return true;
                    }catch(SQLException ePrepState){
                        gui.Gui.showErrorMessage(DATABASE_NUMBER,1, ePrepState);    
                        cleanup.dbRollback(con);
                        return false;
                    }
                }catch(SQLException eCon){
                    gui.Gui.showErrorMessage(DATABASE_NUMBER,2,eCon);
                    return false;                    
                }  
        }     
    
	public boolean updateCustomer(procatering.Customer input, int cid) {
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
        
        
    public boolean addOrder(procatering.Order input) {
        if (input == null) {
            return false;
        }

        try (Connection con = DriverManager.getConnection(URL, username, password)) {
            try(//TODO add a for-loop to add ordercontent/dishes
                PreparedStatement prepStat = con.prepareStatement("INSERT INTO orders (employee_id, customer_id, time_of_order, status) VALUES (?,?,?,?)")){
                con.setAutoCommit(false);
                    prepStat.setInt(1, input.getEmployeeId());
                    prepStat.setInt(2, input.getCustomerId());
                    prepStat.setTimestamp(3, input.getDate());
                    prepStat.setString(3, input.getStatus());
                return true;
				//TODO SET AUTOCOMMIT == TRUE;
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
    
    public DefaultListModel<procatering.Order> findOrder(String value) {
            /*creating strings for every attribute in the customer object:*/
//            String fnClean = input.getFirstName().toUpperCase();
//            String lnClean = input.getLastName().toUpperCase();

		try (Connection con = DriverManager.getConnection(URL, username, password)) {
			try (
					PreparedStatement prepStat = con.prepareStatement("SELECT * FROM order "
							+ "WHERE employee_id LIKE '%?%' OR customer_id LIKE '%?%' OR order_id LIKE '%?%'")
			) {
				con.setAutoCommit(false);
				prepStat.setString(1, value);
				prepStat.setString(2, value);
				prepStat.setString(3, value);
				ResultSet rs = prepStat.executeQuery();
				con.commit();
				con.setAutoCommit(true);
				DefaultListModel<procatering.Order> output = new DefaultListModel<>();
				while (!rs.next()) {
					output.addElement(new procatering.Order(rs.getInt("customer_id"), rs.getInt("employee_id"), rs.getString("status")));
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
    
    
	public String getPasswordFromDatabase(int id) {
		String query = "SELECT password FROM employee WHERE employee_id = ?";
		try (Connection con = DriverManager.getConnection(URL, username, password);
			 PreparedStatement prep = con.prepareStatement(query)) {
			prep.setInt(id, id);
			ResultSet ans = prep.executeQuery();
			ans.first();
			return ans.getString(1);
		} catch (SQLException e) {
			Gui.showErrorMessage(DATABASE_NUMBER,100,e);
			return null;
		}
	}
        /**
         * Adds a new dish into the database. The metode uses another metode to check the length of the dishname.
         * These means that the dish name need less than 255 signs.
         * @param name
         * @param price
         * @param cost
         * @return true if sucsessfully added, else it will retun null.
		 * TODO FIX THE DOCUMENTATION
         */
        public boolean addDish (Dish dish) {
            if(Helper.stringChecker(dish.getName())) {
                try (Connection con = DriverManager.getConnection(URL, username, password)) {
                    try ( PreparedStatement prepStat = con.prepareStatement("INSERT INTO dish (dishname, price, cost) VALUES (?, ?, ?)")){
                        con.setAutoCommit(false);
                        prepStat.setString(1, dish.getName());
                        prepStat.setDouble(2, dish.getPrice());
                        prepStat.setDouble(3, dish.getCost());
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
            return false;
        }
        /**
         * The methode check the dishName length, it has to be less than 255 signs.
         * @param dishName
         * @return Dish object, or null if dishName is not found in the database.
         */
        public Dish getDish(String dishName) {
            if(Helper.stringChecker(dishName)){
                try(Connection con = DriverManager.getConnection(URL,username,password);){
                    try(
                            PreparedStatement prepStat = con.prepareStatement("SELECT * FROM dish WHERE dishname = ?;")
                            ){
                        prepStat.setString(1, dishName);
                        try( ResultSet rs = prepStat.executeQuery();) {
                            
                            while(!rs.next()){
                                String name = rs.getString("dishname");
                                double price = rs.getDouble("price");
                                double cost = rs.getDouble("cost");
                                
                                if(name!=null){
                                    return new Dish(name, price, cost);
                                }else {
                                    return null;
                                }
                            }
                            
                        }catch(SQLException ePrepState){
                            gui.Gui.showErrorMessage(DATABASE_NUMBER,1, ePrepState);
                            cleanup.dbRollback(con);
                            return null;
                        }
                        
                    }catch(SQLException ePrepState){
                        gui.Gui.showErrorMessage(DATABASE_NUMBER,1, ePrepState);
                        cleanup.dbRollback(con);
                        return null;
                    }
                }catch(SQLException eCon){
                    gui.Gui.showErrorMessage(DATABASE_NUMBER,2,eCon);
                    return null;
                }
                
            }
            return null;   /// WHY NOT UNREACHABLE!?
        }
        /**
         * editDish require an allready updated Dish object and the old dishname
         * to identify the existing row in the database to be updated.
         * @param dish
         * @return true if sucsessfully updated, else it will retun false.
         */
        
        public boolean editDish (Dish dish, String name) {
            if(Helper.stringChecker(dish.getName()) && Helper.stringChecker(name)){
                
                try (Connection con = DriverManager.getConnection(URL, username, password)) {
                    try ( PreparedStatement prepStat = con.prepareStatement("UPDATE dish SET dishname = ?, price = ?, cost = ? WHERE dishname = ?")){
                        con.setAutoCommit(false);
                        prepStat.setString(1, dish.getName());
                        prepStat.setDouble(2, dish.getPrice());
                        prepStat.setDouble(3, dish.getCost());
                        prepStat.setString(4, name);
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
            return false;
        }
        /**
         * Search metode for dishes. uses a string value and include every dish with that value inside the dishname.
         * @param value
         * @return a DefaultListModel with dishes that include the string value in dishname, or null if something fails.
         */
        public DefaultListModel<procatering.Dish> findDishes(String value) {
            try (Connection con = DriverManager.getConnection(URL, username, password)) {
                try (PreparedStatement prepStat = con.prepareStatement("SELECT * FROM dish "
                        + "WHERE dishname LIKE '%?%'")
                        ) {
                    con.setAutoCommit(false);
                    prepStat.setString(1, value);
                    ResultSet rs = prepStat.executeQuery();
                    con.commit();
                    con.setAutoCommit(true);
                    DefaultListModel<procatering.Dish> output = new DefaultListModel<>();
                    while (!rs.next()) {
                        output.addElement(new procatering.Dish(rs.getString("dishname"), rs.getDouble("price"), rs.getDouble("cost")));
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
         * Remove a dish from the database with the given name. The name will be checked if it is less than 255 sign
         * before it tries to connect to the database.
         * @param name
         * @return true if sucsessfully deleted, else it will return false.
         */
        public boolean removeDish (String name){
            if(Helper.stringChecker(name)){
                try (Connection con = DriverManager.getConnection(URL, username, password)) {
                    try ( PreparedStatement prepStat = con.prepareStatement("DELETE FROM dish WHERE dishname = ?")){
                        con.setAutoCommit(false);
                        prepStat.setString(1, name);
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
            return false;
        }
        /**
         * Adds a new category into the database. This methode only uses the name of the category to create one.
         * @param name
         * @return 
         */
        public boolean addCategory(String name) {
            if(Helper.stringChecker(name)){
                try (Connection con = DriverManager.getConnection(URL, username, password)) {
                    try ( PreparedStatement prepStat = con.prepareStatement("INSERT INTO category (catname) VALUES (?);")
                            ) {
                        con.setAutoCommit(false);
                        prepStat.setString(1, name);
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
            return false;
        }
}
