package com.github.flandre923.berrypouch.helper;

import java.util.List;

public final class TransferPlanner {
    private TransferPlanner() {
    }

    @FunctionalInterface
    public interface Target<T> {
        int move(T payload, int remaining);
    }

    public static <T> int transfer(T payload, int amount, List<Target<T>> targets) {
        int remaining = Math.max(0, amount);
        for (Target<T> target : targets) {
            if (remaining <= 0) {
                break;
            }
            int moved = Math.max(0, target.move(payload, remaining));
            remaining -= Math.min(remaining, moved);
        }
        return remaining;
    }
}
