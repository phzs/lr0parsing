package visualization;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.*;

public class StepController {

    private static StepController INSTANCE;

    private Thread threadInstance;
    private MainThread mainThread;

    private MainController mainController;
    private Object mutex;

    private SimpleIntegerProperty delay;
    private SimpleBooleanProperty running;
    private boolean hasStarted;
    private TreeMap<String, Step> steps;

    public boolean isRunning() {
        return running.get();
    }

    public SimpleBooleanProperty runningProperty() {
        return running;
    }

    public void clearSteps() {
        steps.clear();
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
        running = new SimpleBooleanProperty(false);
        steps = new TreeMap<>();
        hasStarted = false;
        mutex = new Object();
    }

    public Object getMutex() {
        return this.mutex;
    }

    public void setMainThread(MainThread mainThread) {
        this.mainThread = mainThread;
        this.hasStarted = false;
    }

    public void start() {
        if(!hasStarted) {
            startMainThread();
        }
        else {
            synchronized (mutex) {
                mutex.notify();
            }
        }
    }

    private void startMainThread() {
        threadInstance = new Thread(mainThread);
        threadInstance.setDaemon(true);
        threadInstance.start();
        hasStarted = true;
    }

    public void killMainThread() {
        threadInstance.interrupt();
        mainThread.cancel();
        threadInstance.stop();
        mutex = new Object();
        hasStarted = false;
    }

    public void stop() {
        running.set(false);
        mainController.setFocusToContinue();
    }

    public void nextStep() {
        synchronized (mutex) {
            mutex.notify();
        }
    }

    public void previousStep() {
        //TODO
    }

    public void registerStep(String id, String description, boolean breakpoint) {
        if(breakpoint)
            running.set(false);
        registerStep(id, description);
    }

    public void registerStep(String id, String description) {

        Step currentStep = steps.get(id);
        if(currentStep == null) {
            currentStep = new Step(id, description);
            steps.put(id, currentStep);
        } else {
            currentStep.repetition++;
        }

        mainController.displayStep(id, description, currentStep.repetition);
        System.out.println("[StepController] " + id + " ("+currentStep.repetition+") | " + description + "; Thread: " + Thread.currentThread());

        try {
            if(running.get()) {
                if(currentStep.mayRun) {
                    Thread.sleep(delay.getValue());
                } else {
                    running.set(false);
                }
            } else {
                synchronized (mutex) {
                    getMutex().wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
