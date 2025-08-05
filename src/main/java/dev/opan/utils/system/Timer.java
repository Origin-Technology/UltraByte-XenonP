package dev.opan.utils.system;

public class Timer {
    private long startTime;
    private final long time = -1L;
    public Timer() {
        startTime = System.currentTimeMillis();
    }

    public boolean hasTimeElapsed(Number time) {
        return System.currentTimeMillis() - startTime >= time.longValue();
    }

    public long timeElapsed() {
        return System.currentTimeMillis() - startTime;
    }

    public void reset() {
        startTime = System.currentTimeMillis();
    }

    public boolean passedMs(long ms) {
        return passedNS(convertToNS(ms));
    }
    public boolean passedNS(long ns) {
        return System.nanoTime() - time >= ns;
    }

    public long convertToNS(long time) {
        return time * 1000000L;
    }
}
