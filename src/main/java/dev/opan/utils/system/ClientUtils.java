package dev.opan.utils.system;



import dev.opan.utils.IMinecraft;

import java.util.ArrayList;
import java.util.List;

public class ClientUtils implements IMinecraft {
    private ClientUtils() {

    }
    public static ClientUtils instance = new ClientUtils();
    private List<Runnable> runnables = new ArrayList<Runnable>();
    private boolean updateC03Packets = false;
    private int skips = 0;

    public static void skipTicks(int i) {
        skipTicks(i, false);
    }
    public static void skipTicks(int i,boolean updateC03Packets) {



        instance.skips = i;
        instance.updateC03Packets = updateC03Packets;
    }
    public static void addRunnable(Runnable runnable) {
        instance.runnables.add(runnable);
    }


    public void onSkip() {
        skips--;
        runnables.forEach(Runnable::run);
        if (updateC03Packets) {
           mc.getInstance().player.updatePrevAngles();
        }
        if (skips == 0) {
            reset();
        }
    }

    public int getSkips() {
        return skips;
    }

    private void reset(){
        runnables.clear();
        updateC03Packets = false;
    }
}
