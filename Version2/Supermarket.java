//Allie LaCompte
//Version 2

import java.util.concurrent.Semaphore;
import java.util.Random;

public class Supermarket extends Thread {

    // The length of time to sleep each day
    private int sleepTime;
    // The number of days to simulate
    private int days;
    // The current day
    private int day;
    // The names of the ingredients
    private String[] ingredients = {"meat", "rice", "vegetables"};
    // Keep track of the number of times each chef cooks
    private int[] stats = {0, 0, 0};

    // Semaphores for each ingredient
    Semaphore[] itemSemaphores = {new Semaphore(0), new Semaphore(0), new Semaphore(0)};
    // Semaphore to to signal a new day
    private Semaphore newDay = new Semaphore(0);

    Supermarket(int days, int sleepTime) {
        this.days = days;
        this.sleepTime = sleepTime;
    }

    public void run() {

        int ing1;
        int ing2;
        Random rand = new Random();

        for(day = 1; day <= days; day++) {

            System.out.println("\nDay: " + day);

            // choose two ingredients at random to put on sale
            ing1 = rand.nextInt(3);
            ing2 = rand.nextInt(3);
            while(ing1 == ing2) {
                ing2 = rand.nextInt(3);
            }

            System.out.println("The supermarket put " + ingredients[ing1] + " and " + ingredients[ing2] + " on sale!");
            itemSemaphores[ing1].release();
            itemSemaphores[ing2].release();

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

    // Return the current day
    int getDay() {
        return day;
    }

    void cook(int id) {
        stats[id-1]++;
        System.out.println("Chef" + id + " (the chef with unlimited " + ingredients[id-1] + ") is cooking!");

        // Signal a new day can begin
        newDay.release();
    }

    public static void main(String args[]) {
        int numDays = 10;
        int sleepTime = 3000;

        if (args.length > 0) {
            try {
                numDays = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {}
        }
        System.out.println(numDays + " days will be simulated.");

        if (args.length > 1) {
            try {
                sleepTime = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {}
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
        } catch(InterruptedException e) {};
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

    public Chef(int id, Supermarket market) {
        this.market = market;
        this.id = id;
    }

    public void run() {

        int day;

        while (true) {

            // Try to acquire the first ingredient
            try {
                market.itemSemaphores[id % 3].acquire();
            }
            catch(InterruptedException e) {break;}

            // get the current day
            day = market.getDay();

            // Release the first acquired ingredient
            market.itemSemaphores[id % 3].release();

            // Try to acquire the second ingredient
            try {
                market.itemSemaphores[(id + 1) % 3].acquire();
            }
            catch(InterruptedException e) {break;}

            // If the ingredients were acquired on the same day,
            // re-acquire the ingredient that was released, which
            // could not have been acquired by another Chef due to
            // the unique ordering in which each chef acquires
            // ingredients, then cook
            if(day == market.getDay()) {
                try {
                    market.itemSemaphores[id % 3].acquire();
                }
                catch(InterruptedException e) {break;}
                market.cook(id);
            }

            // If the ingredients were not acquired on the same day,
            // release the second ingredient
            else {
                market.itemSemaphores[(id + 1) % 3].release();
            }

            // Terminate
            if(isInterrupted()) {
                break;
            }
        }
        System.out.println("Chef " + id + " terminated.");
    }
}