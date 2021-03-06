package procatering;

/**
 * The Dish class have everything to do with all dish metodes. And are mainly used to help Employe and Database class.
 * <dl>
 * <dt>Constructors:</dt>
 * <dd>- String name, double price, double cost</dd>
 * <dd>- String name, double price, double cost, int id</dd>
 * <dd>- Dish d</dd>
 * </dl>
 *
 * @author Team 17
 */
public class Dish {
	private String name;
	private double price;
	private double cost;
	private int id = -1;

	/**
	 * This constructor should be used when inserting a dish in the database.
	 *
	 * @param name  The dish name
	 * @param price The dish price
	 * @param cost  The dish cost
	 */
	public Dish(String name, double price, double cost) {
		this.name = name;
		this.price = price;
		this.cost = cost;
	}

	/**
	 * This constructor should be used when extracting a dish from the database.
	 *
	 * @param name  The dish name
	 * @param price The dish price
	 * @param cost  The dish cost
	 * @param id    The dish id in the database
	 */
	public Dish(String name, double price, double cost, int id) {
		this.name = name;
		this.price = price;
		this.cost = cost;
		this.id = id;
	}

	/**
	 * Standard copy constructor. Should be used when it is needed a new reference to the object in memory.
	 *
	 * @param dish The dish object to be copied.
	 */
	public Dish(Dish dish) {
		this(dish.getName(), dish.getPrice(), dish.getCost());
	}

	/**
	 * Checks if the object o is the same as the current dish object.
	 *
	 * @param o The dish object to be checked.
	 * @return true if the object is not the same as the current dish object.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Dish dish = (Dish) o;

		if (!name.equals(dish.name)) return false;

		return true;
	}

	/**
	 * @return name to hasCode
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Regular get method for name
	 *
	 * @return a string value of the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Regular get method for price
	 *
	 * @return a double value of the price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Set the price for a dish
	 *
	 * @param price The price of the given dish
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	/**
	 * Regular get method for cost
	 *
	 * @return a double value of the cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * Set the cost for a dish
	 *
	 * @param cost The cost of the given dish
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * Regular get method for ID
	 *
	 * @return a int value of the ID
	 */
	public int getID() {
		return id;
	}

	/**
	 * toString returns a string representation of the dish object
	 *
	 * @return the name and price of the dish
	 */
	@Override
	public String toString() {
		return name + ", " + price;
	}
}
