package org.homework4;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class AlternatingPrinter {
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private boolean isThreadOneTurn = true;

    public static void main(final String[] args) {
        new AlternatingPrinter().start();
    }

    public void start() {
        final Thread threadOne = new Thread(() -> printRepeatedly("1", true));
        final Thread threadTwo = new Thread(() -> printRepeatedly("2", false));

        threadOne.start();
        threadTwo.start();
    }

    private void printRepeatedly(final String value, final boolean isFirstThread) {
        while (!Thread.currentThread().isInterrupted()) {
            lock.lock();
            try {
                while (isThreadOneTurn != isFirstThread) {
                    condition.await();
                }
                System.out.println(value);
                isThreadOneTurn = !isThreadOneTurn;
                condition.signal();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                lock.unlock();
            }
        }
    }
}