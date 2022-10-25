/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.aladdinconnect.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@link ThreadManager} is
 *
 * @author matt - Initial contribution
 */
public final class ThreadManager {

    private static final ThreadGroup threadGroup = new ThreadGroup("CAMERA");

    private static final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(4,
            defaultThreadFactory("Scheduled"));

    private static final ExecutorService general = Executors.newCachedThreadPool(defaultThreadFactory("General"));

    // /////////////////////////////////////////

    public static void stop() {
        scheduled.shutdownNow();
        general.shutdownNow();
    }

    public static ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit unit) {
        return scheduled.schedule(task, delay, unit);
    }

    public static ScheduledFuture<?> scheduleFixedRateTask(Runnable task, long initialDelay, long period,
            TimeUnit unit) {
        return scheduled.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public static ScheduledFuture<?> scheduleFixedDelayTask(Runnable task, long initialDelay, long delay,
            TimeUnit unit) {
        return scheduled.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    // /////////////////////////////////////////

    public static void execute(Runnable command) {
        general.execute(command);
    }

    public static Future<?> submit(Runnable command) {
        return general.submit(command);
    }

    // /////////////////////////////////////////

    public static Thread newThread(Runnable target) {
        return new Thread(threadGroup, target);
    }

    public static Thread newThread(Runnable target, String name) {
        return new Thread(threadGroup, target, name);
    }

    public static ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    // /////////////////////////////////////////

    private static ThreadFactory defaultThreadFactory(String threadPoolName) {
        return new DefaultThreadFactory(threadPoolName);
    }

    private static class DefaultThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final String threadNamePrefix;

        public DefaultThreadFactory(String threadPoolName) {
            threadNamePrefix = threadPoolName + "-";
        }

        @Override
        public Thread newThread(Runnable runnable) {

            Thread thread = new Thread(threadGroup, runnable, threadNamePrefix + threadNumber.getAndIncrement());

            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY);

            return thread;
        }
    }
}
