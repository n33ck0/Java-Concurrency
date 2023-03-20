import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
/*
 * Project: Restaurant DeLuxe
 * Version: 1.0^n
 * Create a restaurant.  You will have customers come into the restaurant.
 * The restaurant has three waiters that wait on 5 different tables.  There is one chef.
 * The waiters seat the customer, take the order, bring the order to the chef, and then bring the food to the customer
 * once it is ready.  The customers eat and then leave the restaurant.  Use interface/abstract to design this.
 * You should use at least one generic method or class, and use multi-threading to do this.
 * Show how this runs with customers eating for random amounts of time
 * */

public class RestaurantNeue {
    public static void main(String[] args)
    {
        // initializing the restaurant
        Restaurant restaurant = new Restaurant();
        Chef chef = new Chef(restaurant);

        CustomerGenerator customerGenerator = new CustomerGenerator(restaurant);

        /*
         Instantiating the Thread Objects
         The JVM allows for multiple threads of execution running concurrently.
        */
        Thread chefThread = new Thread(chef);
        Thread customerGeneratorThread = new Thread(customerGenerator);
        customerGeneratorThread.start();
        chefThread.start();
    }
}

class Chef implements Runnable{
    Restaurant restaurant;

    public Chef(Restaurant restaurant){
        this.restaurant = restaurant;
    }
    public void run(){
        try{
            Thread.sleep(10000);
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("Chef is ready to cook");
        /*
        restaurant.chef.acquire();
        restaurant.chef.release();
        */
        while(true){
            try {
                restaurant.cookingMeals();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class Customer implements Runnable
{
    String customerName;
    Date customerArrivalTime;

    Restaurant restaurant;

    public Customer(Restaurant restaurant){
        this.restaurant = restaurant;
        this.customerArrivalTime = new Date();
    }
    // Setters and Getters for the customer name and arrival time
    public String getCustomerName(){
        return customerName;
    }
    public Date getCustomerArrivalTime(){
        return customerArrivalTime;
    }
    public void setCustomerName(String customerName){
        this.customerName = customerName;
    }
    public void setCustomerArrivalTime(Date customerArrivalTime){
        this.customerArrivalTime = customerArrivalTime;
    }
    public void run(){
        goToEat();
    }
    private synchronized void goToEat(){
        //restaurant.customerArrived(this);
        //restaurant.customerEaten(this);
        restaurant.addCustomer(this);
    }
}

class CustomerGenerator implements Runnable{
    Restaurant restaurant;
    public CustomerGenerator(Restaurant restaurant){
        this.restaurant = restaurant;
    }
    public void run(){
        while(true){
            //Customer customer = new Customer("Customer " + customerCount, restaurant);
            Customer customer = new Customer(restaurant);
            customer.setCustomerArrivalTime(new Date());
            Thread customerThread = new Thread(customer);
            customer.setCustomerName("Customer " + customerThread.getId());
            customerThread.start();
            try{
                //Thread.sleep(1000);
                TimeUnit.SECONDS.sleep((long) (Math.random() * 10));
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }

        }
    }
}

class Waiter
{
    String[] names;
    Waiter()
    {
        this.names = new String[3];
        names[0] = "Justin";
        names[1] = "Saba";
        names[2] = "Mindy";
    }

    public String getName() {
        Random r = new Random();
        return this.names[r.nextInt(0,2)];
    }

}

class Restaurant{
    int numTables;
    final List<Customer> listOfCustomers;

    public Restaurant(){
        this.numTables = 5;
        this.listOfCustomers = new LinkedList<>(); //<Customer>
    }
    public void cookingMeals() throws InterruptedException {
        Waiter waiter = new Waiter();
        Customer customer;
        synchronized (listOfCustomers){
            while(listOfCustomers.isEmpty()){
                System.out.println("Chef's waiting for diners");

                try{
                    listOfCustomers.wait();
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            //System.out.println("Chef is waiting for a diner");
            customer = (Customer) ((LinkedList<?>)listOfCustomers).poll();
        }
        //long duration = (new Date()).getTime() - customer.getCustomerArrivalTime().getTime();
        long duration;

        try{
            System.out.println("Chef is cooking for " + customer.getCustomerName());
            duration = (long) (Math.random() * 10);
            TimeUnit.SECONDS.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Chef is done cooking for " + customer.getCustomerName());
        System.out.println("Waiter: "+waiter.getName()+ " is serving the food.");
        System.out.println(customer.getCustomerName() + " is eating");
        TimeUnit.SECONDS.sleep(duration);
        System.out.println("Waiter: " +waiter.getName()+" picked up the food");
        System.out.println(customer.getCustomerName() + " is done eating, paid their bill, and has left");

    }
    public void addCustomer(Customer customer){
        System.out.println(customer.getCustomerName() + " is added to the list" + " at time : "
                + customer.getCustomerArrivalTime());
        synchronized (listOfCustomers){
            if (listOfCustomers.size() == numTables){
                System.out.println("All tables are full!");
                System.out.println(customer.getCustomerName() + " leaves the restaurant");
                return;
            }
            ((LinkedList<Customer>)listOfCustomers).offer(customer);
            System.out.println(customer.getCustomerName() + " is seated");

            if (listOfCustomers.size() == 1){
                listOfCustomers.notifyAll();
            }
            //listOfCustomers.add(customer);
            //listOfCustomers.notifyAll();
        }
        //listOfCustomers.add(customer);
        //listOfCustomers.notify();
    }
}
