//Allie LaCompte
//Version 1

import java.util.concurrent.Semaphore;
import java.util.Random;

public class Supermarket extends Thread {

    // The length of time to sleep each day
    private int sleepTime;
    // The number of days to simulate
    private int days;
    // The names of the ingredients
    private String[] ingredients = {"meat", "rice", "vegetables"};
    // Keep track of the number of times each chef cooks
    private int[] stats = {0, 0, 0};

    // Semaphores to signal the two ingredients currently on sale
    Semaphore[] ingredient1 = {new Semaphore(0), new Semaphore(0), new Semaphore(0)};
    Semaphore[] ingredient2 = {new Semaphore(0), new Semaphore(0), new Semaphore(0)};

    // Semaphore to to signal a new day
    private Semaphore newDay = new Semaphore(0);

    Supermarket(int days, int sleepTime) {
        this.days = days;
        this.sleepTime = sleepTime;
    }

    public void run() {

        int ing;
        Random rand = new Random();

        for(int day = 1; day <= days; day++) {

            System.out.println("\nDay: " + day);

            // choose one of 3 pairs of ingredients to put on sale
            ing = rand.nextInt(3);

            System.out.println("The supermarket puts " + ingredients[ing] 
                + " and " + ingredients[(ing + 1) % 3] + " on sale!");
            ingredient1[ing].release();
            ingredient2[(ing+1) % 3].release();

            // Sleep to simulate a day's duration
            try {
                sleep(sleepTime);
            } catch (Exception e) { }

            // Wait for signal to start a new day
            // (i.e. a chef has cooked)
            try{
                newDay.acquire();
            }
            catch(InterruptedException e) {break;}
        }
        System.out.println("\nNumber of times each chef cooked: ");
        System.out.println("Chef1: " + stats[0] + ", Chef2: " + stats[1] + ", Chef3: " + stats[2]);
    }

    void cook(int id) {
        stats[id-1]++;
        System.out.println("Chef" + id+ " (the chef with unlimited " + ingredients[id-1] + ") is cooking!");

        // Signal a new day can begin
        newDay.release();
    }

    public static void main(String args[]) {
        int numDays = 10;
        int sleepTime = 3000;

        if (args.length > 0) {
            try {
                numDays = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) { }
        }
        System.out.println(numDays + " days will be simulated.");

        if (args.length > 1) {
            try {
                sleepTime = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) { }
        }

        Supermarket market = new Supermarket(numDays, sleepTime);

        Chef[] chefs = new Chef[3];
        for (int i = 0; i < chefs.length; i++) {
            chefs[i] = new Chef(i+1, market);
        }

        // Start the Supermarket thread
        market.start();

        // Start the Chef threads
        for (Chef chef : chefs) {
            chef.start();
        }

        try {
            market.join();
        } catch(InterruptedException e) { }
        System.out.println("\nSupermarket stopped.");

        // Stop the Chef threads
        for (Chef chef : chefs) {
            chef.interrupt();
        }
    }
}

// the Class for the Chef thread
class Chef extends Thread {

    // The chef's number: 1, 2, or 3
    private int id;
    private Supermarket market;
    private String[] ingredients = {"meat", "rice", "vegetables"};

    Chef(int id, Supermarket market) {
        this.market = market;
        this.id = id;
    }

    public void run() {

        while (true) {

            // Try to acquire first ingredient
            try {
                market.ingredient1[id % 3].acquire();
            }
            catch(InterruptedException e) {break;}
            System.out.println("Chef" + id + " gets the " + ingredients[id % 3] + "!");

            // Try to acquire second ingredient
            try {
                market.ingredient2[(id + 1) % 3].acquire();
            }
            catch(InterruptedException e) {break;}
            System.out.println("Chef" + id + " gets the " + ingredients[(id + 1) % 3] + "!");
            System.out.println("Chef" + id + " now has all 3 ingredients.");

            // Chef has all 3 ingredients, simulate cooking
            market.cook(id);

            // Terminate
            if(isInterrupted()) {
                break;
            }
        }
        System.out.println("Chef " + id + " terminated.");
    }
}