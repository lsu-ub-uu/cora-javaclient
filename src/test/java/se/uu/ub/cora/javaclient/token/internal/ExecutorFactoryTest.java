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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExecutorFactoryTest {
	private ExecutorFactory executorFactory;
	private Runnable runnable;

	@BeforeMethod
	public void setUp() {
		executorFactory = new ExecutorFactoryImp();
		runnable = () -> {
		};
	}

	@Test
	public void testCreateVirtualThreadPerTaskExecutor() throws Exception {
		ExecutorService executor = executorFactory.createVirtualThreadPerTaskExecutor();

		assertEquals(executor.getClass().getCanonicalName(),
				"java.util.concurrent.ThreadPerTaskExecutor");
		assertStartedThreadsAreVirtual(executor);
	}

	private void assertStartedThreadsAreVirtual(ExecutorService executor)
			throws InterruptedException, ExecutionException {
		Callable<Boolean> checkVirtual = createCallableThatReturnsTrueIfRunOnAVirtualThread();
		Future<Boolean> isVirtual = executor.submit(checkVirtual);

		assertTrue(isVirtual.get());
	}

	private Callable<Boolean> createCallableThatReturnsTrueIfRunOnAVirtualThread() {
		return () -> {
			return Thread.currentThread().isVirtual();
		};
	}

	@Test
	public void testCreateSingleThreadScheduleExecutor() {
		ScheduledExecutorService executor = executorFactory.createSingleThreadScheduledExecutor();

		assertEquals(executor.getClass().getCanonicalName(),
				"java.util.concurrent.Executors.DelegatedScheduledExecutorService");
		// ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) executor;
		// assertEquals(poolExecutor.getMaximumPoolSize(), 1);
	}

	@Test
	public void testCreateWeakRefernce() {
		WeakReference<Runnable> weakReference = executorFactory
				.createWeakReferenceFromRunnable(runnable);

		assertTrue(weakReference.refersTo(runnable));
	}
}
