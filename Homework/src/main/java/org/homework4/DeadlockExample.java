package org.homework4;

public final class DeadlockExample {
    private final Object firstLock = new Object();
    private final Object secondLock = new Object();

    public static void main(final String[] args) {
        new DeadlockExample().demonstrate();
    }

    public void demonstrate() {
        final Thread threadOne = new Thread(this::accessFirstThenSecond);
        final Thread threadTwo = new Thread(this::accessSecondThenFirst);

        threadOne.start();
        threadTwo.start();
    }

    private void accessFirstThenSecond() {
        synchronized (firstLock) {
            sleepBriefly();
            synchronized (secondLock) {
                System.out.println("Thread-1 acquired both locks");
            }
        }
    }

    private void accessSecondThenFirst() {
        synchronized (secondLock) {
            sleepBriefly();
            synchronized (firstLock) {
                System.out.println("Thread-2 acquired both locks");
            }
        }
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}