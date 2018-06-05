package visualization;

import javafx.beans.property.SimpleIntegerProperty;

import java.util.*;
import java.util.concurrent.Semaphore;

public class StepController {

    private static StepController INSTANCE;

    private MainThread mainThread;
    private Object semaphore;

    private SimpleIntegerProperty delay;
    private boolean running;
    private boolean hasStarted;
    private TreeMap<String, Step> steps;

    public boolean isRunning() {
        return running;
    }

    private static class Step {
        protected String id;
        protected String descripton;
        protected boolean mayRun;
        protected int repetition;

        public Step(String id, String descripton) {
            this.id = id;
            this.descripton = descripton;
            this.mayRun = true;
            this.repetition = 0;

        }
    }

    public static StepController getInstance() {
        if(INSTANCE == null)
            INSTANCE = new StepController();
        return INSTANCE;
    }

    private StepController() {
        delay = new SimpleIntegerProperty(1);
        running = true;
        steps = new TreeMap<>();
        hasStarted = false;
        semaphore = new Object();
    }

    public Object getSemaphore() {
        return this.semaphore;
    }

    public void setMainThread(MainThread mainThread) {
        this.mainThread = mainThread;
    }

    public void start() {
        Thread th = new Thread(mainThread);
        running = true;
        if(!hasStarted) {
            System.out.println("StepController: Starting mainThread for the first time");
            th.setDaemon(true);
            th.start();
            hasStarted = true;
        }
        else {
            synchronized (semaphore) {
                semaphore.notify();
            }
        }
    }

    public void stop() {
        running = false;
    }

    public void nextStep() {
        //TODO
    }

    public void previousStep() {
        //TODO
    }

    public void registerStep(String id, String description) {
        steps.put(id, new Step(id, description));
    }
}
