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

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//SPIKE
public class Scheduler {

	public void scheduleMethodWithDelay(Callable<?> method, int delay)
			throws InterruptedException, ExecutionException {
		ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

		try (virtualThreadExecutor) {
			Future<?> methodResult = scheduleMethodUsingVirtualTread(method, delay,
					TimeUnit.SECONDS, virtualThreadExecutor);
			handleAnswerFromScheduledMethod(methodResult);
		}
	}

	private static Future<?> scheduleMethodUsingVirtualTread(Callable<?> task, int delay,
			TimeUnit unit, ExecutorService executorService) {
		return executorService.submit(() -> {
			ScheduledExecutorService singleThreadScheduler = Executors
					.newSingleThreadScheduledExecutor();

			try (singleThreadScheduler) {
				CompletableFuture<Object> result = new CompletableFuture<>();

				singleThreadScheduler.schedule(() -> {
					try {
						result.complete(task.call());
					} catch (Exception e) {
						result.completeExceptionally(e);
					}
				}, delay, unit);

				return result.get();
			}
		});
	}

	private void handleAnswerFromScheduledMethod(Future<?> methodResult)
			throws InterruptedException, ExecutionException {

		// taskResult.get() waits until scheduledTask is run
		System.out.println(methodResult.get());
		System.out.println(methodResult.isDone());
	}
}
