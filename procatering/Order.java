
package procatering;

/**
 * Class Order
 *
 * @author TEAM 17
 */

import database.Database;
import java.sql.Timestamp;
import javax.swing.*;

public class Order {
    
    private int customerId;
    private int employeeId;
    private String status;      // Active, Completed or Cancelled
    private Timestamp orderDate;
    private DefaultListModel<OrderContent> ordercontent;
    
    /* Constructor */
    public Order(int c_id, int e_id, String status) {
        customerId = c_id;
        employeeId = e_id;
        this.status = status;
        this.orderDate = orderDate;
        ordercontent = new DefaultListModel();
	}
    
     /** Standard copy constructor */ 
    public Order(Order o){
        this(o.getCustomerId(),o.getEmployeeId(),o.getStatus());
        this.orderDate = o.getOrderDate();
        this.ordercontent = o.getOrderContent();
    }
	
    public int getEmployeeId(){
        return employeeId;
    }
    
    public int getCustomerId(){
        return customerId;
    }

    /**
     * Method addDish
     * Adds a dish to the order content list within the Order
     * @param dishName Dish object that are to be added
     * @param quantity The amount of dishes that is to be added
     * @param index which OrderContent object in the DefaultListModel that the Dish object is inserted into.
     * @return boolean, true for successful insertion.
     */
    public boolean addDish(Dish dishName, int quantity, int index){
        if(ordercontent.getElementAt(index).addDish(dishName, quantity)){
            return true;
        }
        return false;
    }
     /**
     * Method getCurrentDate
     * @return a current Timestamp
     */
    public Timestamp getCurrentDate(){
        java.util.Date time= new java.util.Date();
	Timestamp date = new Timestamp(time.getTime());
        return date;
    }

    public Timestamp getOrderDate(){
        return orderDate;
    }
    public String getStatus(){
        return status;
    }

    /**
     * Method setStatus
     * Sets the status of an order.
     * @param status the status for the order. Available statuses for Order: Active, Completed or Cancelled
     */
    public void setStatus(String status){
        this.status = status;
    }

    /**
     * Method getOrderContent
     * @return a DefaultListModel<OrderContent> with the orderContent of the order ( Dishes, deliveryDate and time )
     */
    public DefaultListModel<OrderContent> getOrderContent() {
        return ordercontent;
    }
    
    
    /**
     * Method addOrderContent
     * Creates an OrderContent object for adding of dishes, and adds it to the DefaultListModel<orderContent>
     * @param delivery Timestamp with the date and time for delivery.
     */
    public boolean addOrderContent(Timestamp delivery) {
        if(delivery != null){    
            ordercontent.addElement(new OrderContent(delivery));
            return true;
        }
        return false;
    }
    //TODO: ADDISH
    
    public void addDB(){
        database.Database db = new database.Database();
    }

    /**
     * Method toString
     * @return object shows as String with the order created date/time and the content of the order sorted.
     */
    @Override
    public String toString() {
        String output ="Order created: "+orderDate+"\n\n";
        for (int i = 0; i < ordercontent.size(); i++) {
            output+= ordercontent.get(i)+"\n"; 
        }
        return output;
    }
}

