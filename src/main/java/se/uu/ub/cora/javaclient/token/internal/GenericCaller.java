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
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

//GenericCaller class that calls a given Runnable after a delay
public class GenericCaller extends Thread {
	private final WeakReference<Runnable> taskReference;
	private final long delayMillis;

	public GenericCaller(Runnable task, long delayMillis) {
		this.taskReference = new WeakReference<>(task);
		this.delayMillis = delayMillis;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(delayMillis); // Wait for the specified delay
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // Handle interruption gracefully
			return;
		}

		Runnable task = taskReference.get();
		if (task != null) {
			task.run(); // Execute the task if it hasn't been garbage collected
		} else {
			System.out.println("Task owner is no longer referenced. Thread exiting.");
		}
	}

	// SPIKE
	static Future<?> schedule(Runnable task, int delay, TemporalUnit unit,
			ExecutorService executorService) {
		return executorService.submit(() -> {
			try {
				Thread.sleep(Duration.of(delay, unit));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			task.run();
		});
	}
}
