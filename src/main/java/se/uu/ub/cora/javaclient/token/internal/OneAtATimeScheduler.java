/*
 * Copyright 2025 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.javaclient.token.internal;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * OneAtATimeScheduler only schedules one task during the same time. When the next task is scheduled
 * is the first task canceled if it has not already run, before the next one is scheduled.
 */
public class OneAtATimeScheduler implements Scheduler {
	private ExecutorFactory executorFactory;
	private Future<?> future;

	public static OneAtATimeScheduler usingExecutorFactory(ExecutorFactory executorFactory) {
		return new OneAtATimeScheduler(executorFactory);
	}

	private OneAtATimeScheduler(ExecutorFactory executorFactory) {
		this.executorFactory = executorFactory;
	}

	@Override
	public void scheduleTaskWithDelayInMillis(Runnable task, long delayInMillis) {
		WeakReference<Runnable> weakReferenceTask = executorFactory
				.createWeakReferenceFromRunnable(task);
		ExecutorService virtualThreadExecutor = executorFactory
				.createVirtualThreadPerTaskExecutor();
		if (null != future) {
			future.cancel(false);
		}
		try {
			future = virtualThreadExecutor.submit(
					() -> scheduleTaskUsingSingleThreadSchedule(weakReferenceTask, delayInMillis));
		} finally {
			virtualThreadExecutor.shutdown();
		}
	}

	private void scheduleTaskUsingSingleThreadSchedule(WeakReference<Runnable> task, long delay) {
		ScheduledExecutorService singleThreadScheduler = executorFactory
				.createSingleThreadScheduledExecutor();
		try {
			singleThreadScheduler.schedule(getRunnableFromWeakReference(task), delay,
					TimeUnit.MILLISECONDS);
		} finally {
			singleThreadScheduler.shutdown();
		}
	}

	private Runnable getRunnableFromWeakReference(WeakReference<Runnable> methodAsWeakReference) {
		return () -> {
			Runnable methodAsRunnable = methodAsWeakReference.get();
			methodAsRunnable.run();
		};
	}

	public ExecutorFactory onlyForTestGetExecutorFactory() {
		return executorFactory;
	}
}