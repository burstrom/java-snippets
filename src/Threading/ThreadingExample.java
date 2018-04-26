package Threading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ThreadingExample {
    /**
     * Här har vi ett enkelt exempel på problem som kan uppstå med trådar som använder sig av samma data.
     * @param args not used
     */
    public static void main(String[] args){

        Television sharedTV;

        boolean runThreadSafe = false;  // true = säkrare trådning, false = ingen synkronisering

        if(runThreadSafe){
            sharedTV = new TelevisionTHREADSAFE();
        }else{
            sharedTV = new TelevisionNOTSAFE();
        }

        // Här skapar vi alla barn och lägger till dessa till allChildren
        List<Child> allChildren = new ArrayList<>(Arrays.asList(
                new Child(sharedTV,"TIM", 200)
                ,new Child(sharedTV,"JOHAN", 300)
                ,new Child(sharedTV,"JIMMIE", 200)
        ));

        // Starta alla barn-trådarna.
        allChildren.forEach(Child::start);
    }
}



class Child extends Thread{
    private static final int WAIT_TIME = 100; // Väntetid som tråden "sover" mellan varje iteration.
    private static final int POSSIBLE_CHANNELS = 10; // Möjliga kanaler som barnet kan trycka ned på sin "kontroller"
    private final Television tv; // TV-Apparaten som barnet ser på.
    private final String tvIdentity;
    private boolean wantsToWatchTV;
    private Random r; // Används för att kunna få ut slumpmässiga värden i likadana sekvenser mellan körningar.

    /**
     *
     * @param tv Den TV apparat som barnet ser på
     * @param name Barnets namn
     * @param randomSeed Random seed för att kunna få återkommande slumpvärden.
     */
    Child(Television tv, String name, long randomSeed){
        this.tv = tv;
        this.tvIdentity = this.tv.getIdentity();
        this.setName(name);
        this.r = new Random(randomSeed);
        this.wantsToWatchTV = true;
    }

    @Override
    public void run() {
        super.run();
        System.out.printf("\n%s started watching TV", this.getName());
        pauseThread(100);
        while (this.wantsToWatchTV){
            watchTV();
        }
        System.out.printf("\n%s stopped watching tv", this.getName());
    }

    private void watchTV() {
        Integer oldChannel = tv.getCurrentChannel();
        Integer newChannel = newChannel();
        long loopTime = System.currentTimeMillis() % 100000; // För att kunna visa tidstämpel lite förkortat.
        if (!oldChannel.equals(newChannel)) {
            printAction(oldChannel, loopTime, newChannel);
            this.tv.setCurrentChannel(newChannel, this);
        } else {
            printAction(oldChannel, loopTime, null);
        }
        this.hasSeenEnoughTV();
        pauseThread(r.nextInt(WAIT_TIME)+10);
    }


    private void pauseThread(int waitTime) {
        try {sleep(waitTime);}
        catch (InterruptedException e) {e.printStackTrace();}
    }
    private void printAction(Integer oldChannel, long loopTime, Integer newChannel) {
        if(newChannel == null){System.out.printf("\n%s :: %s, %d\tunchanged\t%s", tvIdentity, loopTime, oldChannel, this.getName());}
        else{System.out.printf("\n%s :: %s, %d\t->\t%d\t\t%s", tvIdentity, loopTime, oldChannel, newChannel, this.getName());}
    }
    private void hasSeenEnoughTV(){this.wantsToWatchTV = !(r.nextInt(10) < 1);}
    private Integer newChannel(){return r.nextInt(POSSIBLE_CHANNELS)+1;}
}

class TelevisionNOTSAFE extends Television{
    TelevisionNOTSAFE() {
        super();
        System.out.println("RUNNING UNSAFE MODE!");
    }
}

class TelevisionTHREADSAFE extends Television{
    TelevisionTHREADSAFE() {
        super();
        System.out.println("RUNNING SAFE MODE!");
    }

    @Override
    public synchronized Integer getCurrentChannel() {
        return super.getCurrentChannel();
    }

    @Override
    public synchronized void setCurrentChannel(Integer currentChannel) {
        synchronized (this){
            super.setCurrentChannel(currentChannel);
        }
    }
}

class Television{
    private Integer currentChannel;

    Television() {
        this.currentChannel = null;
    }

    Integer getCurrentChannel() {
        return currentChannel;
    }

    void setCurrentChannel(Integer currentChannel) {
        this.currentChannel = currentChannel;
    }

    String getIdentity() {
        return Integer.toHexString(System.identityHashCode(this));
    }
}
