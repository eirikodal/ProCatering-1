package procatering;

/**
 *
 * @author TEAM 17
 */
import java.sql.Timestamp;
import javax.swing.DefaultListModel;

public class Order {

    private int customerId;
    private int employeeId;
    private Timestamp date;
    private String status;
    private DefaultListModel<OrderContent> ordercontent;

    /* Constructor */
    public Order(int c_id, int e_id) {
        customerId = c_id;
        employeeId = e_id;
//        this.date = date;
//        this.status = status;
//        ordercontent = new DefaultListModel();
    }

    public DefaultListModel getOrderContent() {
        return ordercontent;
    }
    
    public String getStatus(){
        return status;
    }
    
    public String setStatus(String stat){
        return status = stat;
    }

    /**
     * Adds the date when the order is made
     * @param date
     */
    public void addDate(Timestamp date) {
        this.date = date;
    }

    /**
     * @param orderObj This ordercontent object will be added to the order list.
     */
    public void addOrderContent(OrderContent orderObj) {
        for (int i = 0; i < ordercontent.getSize(); i++) {
            ordercontent.addElement(orderObj);
        }
    }

    public void addDB(){
       database.Database db = new database.Database();
    }
}
