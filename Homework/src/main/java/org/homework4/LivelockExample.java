package org.homework4;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public final class LivelockExample {
    private final ReentrantLock firstLock = new ReentrantLock();
    private final ReentrantLock secondLock = new ReentrantLock();

    public static void main(final String[] args) {
        new LivelockExample().demonstrate();
    }

    public void demonstrate() {
        final Runnable taskOne = () -> attemptLocks(firstLock, secondLock, "Thread-1");
        final Runnable taskTwo = () -> attemptLocks(secondLock, firstLock, "Thread-2");

        new Thread(taskOne).start();
        new Thread(taskTwo).start();
    }

    private void attemptLocks(
            final ReentrantLock primary,
            final ReentrantLock secondary,
            final String threadName
    ) {
        while (!Thread.currentThread().isInterrupted()) {
            if (primary.tryLock()) {
                System.out.println(threadName + " acquired primary lock.");
                try {
                    sleepBriefly();
                    if (secondary.tryLock()) {
                        try {
                            System.out.println(threadName + " acquired both locks!");
                            return;
                        } finally {
                            secondary.unlock();
                        }
                    } else {
                        System.out.println(threadName + " failed secondary lock, retrying...");
                    }
                } finally {
                    primary.unlock();
                }
            }
        }
    }

    private void sleepBriefly() {
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}