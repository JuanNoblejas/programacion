package di.uniba.map.b.adventure.threads;

import javax.swing.SwingUtilities;

public class OxygenTimer extends Thread {
    private int oxigenoRestante;
    private boolean activo;
    private OxygenListener listener;

    public interface OxygenListener {
        void onOxygenUpdate(int oxigeno);
        void onGameOver();
    }

    public OxygenTimer(int oxigenoInicial, OxygenListener listener) {
        this.oxigenoRestante = oxigenoInicial;
        this.activo = true;
        this.listener = listener;
    }

    @Override
    public void run() {
        while (activo && oxigenoRestante > 0) {
            try {
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

    public void detener() {
        this.activo = false;
    }

    public void addOxigeno(int cantidad) {
        this.oxigenoRestante += cantidad;
    }

    public int getOxigenoRestante() {
        return oxigenoRestante;
    }
}
