
package database;

import gui.Gui;
import procatering.Employee;

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
	private final String dbDriver = "com.mysql.jdbc.Driver";
	private Connection connection;
	private PreparedStatement preparedStatement;
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
							+ " VALUES('?', '?', '?', '?', '?', '?', '?', '?')")
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
                        "WHERE clean_fn LIKE '?' OR clean_ln LIKE '?' OR phonenumber LIKE '?'" +
                        "OR postalcode LIKE '?' OR corporatename LIKE '?' OR customer.corporatenumber LIKE '?'");
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

    private boolean createConnection(){
        try{
            connection = DriverManager.getConnection(URL,username,password);
            return true;
        }catch(SQLException ex){
            return false;//"Error 011: "+ex;
        }
    }

    public boolean addEmployee(Employee input, String pw) {
        if(input == null){
            return false;
        }
        /*creating strings for every attribute in the customer object:*/
        //            String fnClean = input.getFirstName().toUpperCase();
        //            String lnClean = input.getLastName().toUpperCase();
        try(Connection con = DriverManager.getConnection(URL,username,password);){
            try(
                    PreparedStatement prepStat = con.prepareStatement("INSERT INTO customer"
                            + "(type_id, firstname, lastname, clean_fn, clean_ln, password, phonenumber,"
                            + "postalcode, dob, email)"
                            +" VALUES('?', '?', '?', '?', '?', '?', '?', '?', '?', '?')");
                    ){
                con.setAutoCommit(false);
                prepStat.setString(1, input.getType());
                prepStat.setString(2, input.getFirstName());
                prepStat.setString(3, input.getLastName());
                prepStat.setString(4, input.getFirstName().toUpperCase());
                prepStat.setString(5, input.getLastName().toUpperCase());
                prepStat.setString(6, pw);
                prepStat.setString(7, input.getPhoneNumber());
                prepStat.setInt(8, input.getPostalCode());
                prepStat.setString(9, input.getDob());;
                prepStat.setString(10, input.getEmail());
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
    
    public Employee getEmployee(int id) {
        if(id <1){
            return null;
        }
        /*creating strings for every attribute in the customer object:*/
        try(Connection con = DriverManager.getConnection(URL,username,password);){
            try(
                    PreparedStatement prepStat = con.prepareStatement("SELECT * FROM employee WHERE employee_id = ?;")
			){
                        prepStat.setInt(1, id);
                try( ResultSet rs = prepStat.executeQuery();) {
                    
                    while(!rs.next()){
                        String type = rs.getString("type_id");
                        String fn = rs.getString("firstname");
                        String ln = rs.getString("lastname");
                        String dob = rs.getString("dob");
                        String phone = rs.getString("phonenumber");
                        String mail = rs.getString("mail");
                        int pCode = rs.getInt("postalcode");
                        
                        if(type!=null && fn != null && ln != null && dob != null
                                && phone != null && mail != null && pCode > 999 && pCode < 10000){
                            return new Employee(type, fn, ln, phone, pCode, dob, mail);
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
    
    public boolean updateEmployee(procatering.Employee input, int id) {
            if(input == null){
                return false;
            }
            /*creating strings for every attribute in the customer object:*/ 
//            String fnClean = input.getFirstName().toUpperCase();
//            String lnClean = input.getLastName().toUpperCase();
            try(Connection con = DriverManager.getConnection(URL,username,password);){
                try(
                    PreparedStatement prepStat = con.prepareStatement("Update customer"
                    + "SET type_id = ?, firstname = ?, lastname = ?, clean_fn = ?, clean_ln = ?, password = ?, dob = ?, email = ?," 
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
      
    public boolean changeEmployeePassword(String input, int id){
                  if(input == null){
                return false;
            }
            /*creating strings for every attribute in the customer object:*/ 
//            String fnClean = input.getFirstName().toUpperCase();
//            String lnClean = input.getLastName().toUpperCase();
            try(Connection con = DriverManager.getConnection(URL,username,password);){
                try(
                    PreparedStatement prepStat = con.prepareStatement("Update customer"
                    + "SET password = ?,"
                    +" WHERE employee_id = ?;");
                    ){
                        con.setAutoCommit(false);
                            prepStat.setString(1, input);;
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
							+ "SET firstname = '?', lastname = '?', clean_fn = '?', clean_ln = '?', phonenumber = '?', email = '?', address = '?', postalcode = '?'"
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
}

