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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerImp implements Scheduler {
	private ExecutorFactory executorFactory;

	public static SchedulerImp usingExecutorFactory(ExecutorFactory executorFactory) {
		return new SchedulerImp(executorFactory);
	}

	private SchedulerImp(ExecutorFactory executorFactory) {
		this.executorFactory = executorFactory;
	}

	@Override
	public void scheduleMethodWithDelayInMillis(Runnable task, long delayInMillis) {
		WeakReference<Runnable> weakReferenceTask = executorFactory
				.createWeakReferenceFromRunnable(task);
		ExecutorService virtualThreadExecutor = executorFactory
				.createVirtualThreadPerTaskExecutor();

		try {
			virtualThreadExecutor.submit(
					() -> scheduleTaskUsingSingleThreadSchedule(weakReferenceTask, delayInMillis));
		} finally {
			virtualThreadExecutor.shutdown();
			// virtualThreadExecutor.close();
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