package di.uniba.map.b.adventure.threads;

import javax.swing.SwingUtilities;

public class OxygenTimer extends Thread {
    private int oxygenRemaining;
    private volatile boolean active;
    private volatile boolean paused;
    private final Object pauseLock = new Object();
    private OxygenListener listener;

    public interface OxygenListener {
        void onOxygenUpdate(int oxygen);
        void onGameOver();
    }

    public OxygenTimer(int initialOxygen, OxygenListener listener) {
        this.oxygenRemaining = initialOxygen;
        this.active = true;
        this.paused = false;
        this.listener = listener;
    }

    @Override
    public void run() {
        while (active && oxygenRemaining > 0) {
            try {
                // If paused, wait until resumed
                synchronized (pauseLock) {
                    while (paused && active) {
                        pauseLock.wait();
                    }
                }

                if (!active) break;

                Thread.sleep(1000);
                oxygenRemaining--;
                
                if (listener != null) {
                    SwingUtilities.invokeLater(() -> listener.onOxygenUpdate(oxygenRemaining));
                }
                
            } catch (InterruptedException e) {
                System.out.println("Timer interrupted.");
                active = false;
            }
        }
        
        if (oxygenRemaining <= 0 && active && listener != null) {
            SwingUtilities.invokeLater(() -> listener.onGameOver());
        }
    }

    /**
     * Pauses the timer. Oxygen stops decreasing.
     */
    public void pausar() {
        paused = true;
    }

    /**
     * Resumes the timer after a pause.
     */
    public void reanudar() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    /**
     * Returns true if the timer is paused.
     */
    public boolean isPausado() {
        return paused;
    }

    public void detener() {
        this.active = false;
        // Unblock if paused so the thread can terminate
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public void addOxigeno(int amount) {
        this.oxygenRemaining += amount;
    }

    public int getOxigenoRestante() {
        return oxygenRemaining;
    }
}
