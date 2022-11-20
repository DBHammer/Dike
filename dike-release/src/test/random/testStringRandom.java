package random;

import org.junit.Test;

import edu.ecnu.dike.random.BasicRandom;

public class testStringRandom {
    
    private int rndRounds = 10;

    @Test
    public void testCLast() {
        String rndValue;
        BasicRandom rnd = new BasicRandom();
        System.out.println("\ntest clast\n");
        for (int i = 0; i < rndRounds; i++) {
            rndValue = rnd.getCLast();
            System.out.println(rndValue);
            rndValue = rnd.getCLast(i);
            System.out.println(rndValue);
        }
    }

    @Test
    public void testAString() {
        String rndValue;
        BasicRandom rnd = new BasicRandom();
        System.out.println("\ntest astring\n");
        for (int i = 0; i < rndRounds; i++) {
            rndValue = rnd.getAString(i , i);
            System.out.println(rndValue);
        }
    }

    @Test
    public void testNString() {
        String rndValue;
        BasicRandom rnd = new BasicRandom();
        System.out.println("\ntest nstring\n");
        for (int i = 0; i < rndRounds; i++) {
            rndValue = rnd.getAString(i , i);
            System.out.println(rndValue);
        }
    }

    @Test
    public void testCPhone() {
        String rndValue;
        BasicRandom rnd = new BasicRandom();
        System.out.println("\ntest cphone\n");
        for (int i = 0; i < rndRounds; i++) {
            rndValue = rnd.getCPhone(i , i, i);
            System.out.println(rndValue);
        }
    }

    @Test
    public void testState() {
        String rndValue;
        BasicRandom rnd = new BasicRandom();
        System.out.println("\ntest state\n");
        for (int i = 0; i < rndRounds; i++) {
            rndValue = rnd.getState();
            System.out.println(rndValue);
        }
    }
}
