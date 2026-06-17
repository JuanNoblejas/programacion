package di.uniba.map.b.adventure.threads;

import javax.swing.SwingUtilities;

public class OxygenTimer extends Thread {
    private int oxigenoRestante;
    private volatile boolean activo;
    private volatile boolean pausado;
    private final Object lockPausa = new Object();
    private OxygenListener listener;

    public interface OxygenListener {
        void onOxygenUpdate(int oxigeno);
        void onGameOver();
    }

    public OxygenTimer(int oxigenoInicial, OxygenListener listener) {
        this.oxigenoRestante = oxigenoInicial;
        this.activo = true;
        this.pausado = false;
        this.listener = listener;
    }

    @Override
    public void run() {
        while (activo && oxigenoRestante > 0) {
            try {
                // Si esta pausado, esperamos hasta que se reanude
                synchronized (lockPausa) {
                    while (pausado && activo) {
                        lockPausa.wait();
                    }
                }

                if (!activo) break;

                Thread.sleep(1000);
                oxigenoRestante--;
                
                if (listener != null) {
                    SwingUtilities.invokeLater(() -> listener.onOxygenUpdate(oxigenoRestante));
                }
                
            } catch (InterruptedException e) {
                System.out.println("Temporizador interrumpido.");
                activo = false;
            }
        }
        
        if (oxigenoRestante <= 0 && activo && listener != null) {
            SwingUtilities.invokeLater(() -> listener.onGameOver());
        }
    }

    /**
     * Pausa el temporizador. El oxigeno deja de decrementarse.
     */
    public void pausar() {
        pausado = true;
    }

    /**
     * Reanuda el temporizador despues de una pausa.
     */
    public void reanudar() {
        synchronized (lockPausa) {
            pausado = false;
            lockPausa.notifyAll();
        }
    }

    /**
     * Devuelve true si el timer esta pausado.
     */
    public boolean isPausado() {
        return pausado;
    }

    public void detener() {
        this.activo = false;
        // Desbloquear si esta en pausa para que el hilo termine
        synchronized (lockPausa) {
            pausado = false;
            lockPausa.notifyAll();
        }
    }

    public void addOxigeno(int cantidad) {
        this.oxigenoRestante += cantidad;
    }

    public int getOxigenoRestante() {
        return oxigenoRestante;
    }
}
