import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class Main {
    static final int philosopherCount = 5; // determines number of philosophers
    static final int runTime =  25;  //running 25 senerios 20 times aka ~500
    static ArrayList<Chopstick> chopsticks = new ArrayList<Chopstick>();
    static ArrayList<Philosopher> philosophers = new ArrayList<Philosopher>();
 
    public static void main(String[] args) {
        for (int i = 0 ; i < philosopherCount ; i++) chopsticks.add(new Chopstick());
        for (int i = 0 ; i < philosopherCount ; i++)
            philosophers.add(new Philosopher());
        for (Philosopher p : philosophers) new Thread(p).start();
        long endTime = System.currentTimeMillis() + (runTime * 1000); 
 
        do {                                                    
            StringBuilder sb = new StringBuilder("Status:");  //Start printing status of the 5 philosophers
            for (Philosopher p : philosophers) {
                sb.append(p.state.toString());
                sb.append(" ");           					  //Each second it prints a senerio that is happening
            }                             				      //Looks nicer than all 500!
             sb.append("     Chopstick Holders:");  		  //Printing Same Status with the chopstick holders
            for (Chopstick c : chopsticks) {   
                int holder = c.holder.get();
                sb.append(holder==-1?"   ":String.format("P%02d",holder));
                sb.append(" ");
            }
             System.out.println(sb.toString());
            try {Thread.sleep(1000);} catch (Exception ex) {}
        } while (System.currentTimeMillis() < endTime);
        for (Philosopher p : philosophers) p.end.set(true);
        for (Philosopher p : philosophers)
            System.out.printf("Philosopher%2d: EatCount %,d times \n",
                p.philosipherNumber, p.timesEaten, p.timesEaten/runTime);
}  }
enum PhilosopherActions { Hungry, Eating, Ponder }

class Chopstick {
    public static final int CURRENT_TABLE = -1; //nothing on the table 
    static int CurrentCstks = 0;  
    public int id;
    public AtomicInteger holder = new AtomicInteger(CURRENT_TABLE);
    Chopstick() { id = CurrentCstks++; }
}
class Philosopher implements Runnable {
    static final int waiting = 100;                          
    static AtomicInteger token = new AtomicInteger(0);
    static int currentPhil = 0;
    static Random rand = new Random();
    AtomicBoolean end = new AtomicBoolean(false);
    int philosipherNumber;                                    
    PhilosopherActions state = PhilosopherActions.Hungry;
    Chopstick left;
    Chopstick right;
    int timesEaten = 0; 
    Philosopher() {
        philosipherNumber = currentPhil++;
        left = Main.chopsticks.get(philosipherNumber);
        right = Main.chopsticks.get((philosipherNumber+1)%Main.philosopherCount);
    }
    void sleep() { try { Thread.sleep(rand.nextInt(waiting)); }
        catch (InterruptedException ex) {} }
 
    void waitingChopstick(Chopstick stick) {
        do {
            if (stick.holder.get() == Chopstick.CURRENT_TABLE) {
                stick.holder.set(philosipherNumber);                
                return;
            } else {                                 
                sleep();                            
            }
        } while (true);
    }
     public void run() {
        do {
            if (state == PhilosopherActions.Ponder) {    
                state = PhilosopherActions.Hungry;         
            } else {                                  
                if (token.get() == philosipherNumber) {             
                    waitingChopstick(left);
                    waitingChopstick(right);               
                    token.set((philosipherNumber+2)% Main.philosopherCount);
                    state = PhilosopherActions.Eating;
                    timesEaten++;  
                    sleep();                          
                    left.holder.set(Chopstick.CURRENT_TABLE);
                    right.holder.set(Chopstick.CURRENT_TABLE);
                    state = PhilosopherActions.Ponder;     
                    sleep();
                } else {                    
                    sleep();
                }            }
        } while (!end.get());
} 	}