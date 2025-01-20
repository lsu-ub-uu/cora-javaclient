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
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.Assert;
import org.testng.annotations.Test;

public class GenericCallerTest {

	@Test
	public void test_executor() throws Exception {
		ExecutorService threadPerTaskExecutor = Executors.newVirtualThreadPerTaskExecutor();
		// threadPerTaskExecutor.submit(null);
	}

	@Test
	public void testThreadExecutesTaskAfterDelay() throws InterruptedException {
		AtomicBoolean taskExecuted = new AtomicBoolean(false);

		// Create GenericCaller with a task that sets a flag and delay of 500ms
		GenericCaller genericCaller = new GenericCaller(() -> taskExecuted.set(true), 500);
		genericCaller.start();

		// Wait for the task to execute
		genericCaller.join();

		// Verify that the task was executed
		Assert.assertTrue(taskExecuted.get(), "Task should execute after the specified delay.");
	}

	@Test
	public void testThreadDoesNotExecuteIfTaskOwnerIsGarbageCollected()
			throws InterruptedException {
		AtomicBoolean taskExecuted = new AtomicBoolean(false);

		// Create a Runnable that sets the flag
		Runnable task = () -> taskExecuted.set(true);
		WeakReference<Runnable> taskReference = new WeakReference<>(task);

		// Create and start GenericCaller
		GenericCaller genericCaller = new GenericCaller(task, 1000);
		genericCaller.start();

		// Nullify the strong reference to the task
		task = null;

		// Suggest garbage collection
		System.gc();
		Thread.sleep(500); // Give the GC time to act

		// Verify that the taskReference has been cleared
		Assert.assertNull(taskReference.get(), "Task should be garbage collected.");
		genericCaller.join(); // Ensure the thread has stopped

		// Verify that the task was not executed
		Assert.assertFalse(taskExecuted.get(),
				"Task should not execute after being garbage collected.");
	}

	@Test
	public void testThreadHandlesInterruptGracefully() throws InterruptedException {
		AtomicBoolean taskExecuted = new AtomicBoolean(false);

		// Create GenericCaller with a task
		GenericCaller genericCaller = new GenericCaller(() -> taskExecuted.set(true), 1000);
		genericCaller.start();

		// Interrupt the thread
		genericCaller.interrupt();
		genericCaller.join(); // Ensure the thread has stopped

		// Verify the task was not executed
		Assert.assertFalse(taskExecuted.get(),
				"Task should not execute if the thread is interrupted.");
	}
}
