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

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class OneAtATimeSchedulerTest {
	private static final long DELAY_10MS = 10;
	ExecutorFactorySpy executorFactorySpy;
	private Scheduler scheduler;
	private RunnableTaskSpy task1;
	private RunnableTaskSpy task2;

	@BeforeMethod
	private void beforeMethod() {
		task1 = new RunnableTaskSpy();
		task2 = new RunnableTaskSpy();
		executorFactorySpy = new ExecutorFactorySpy();
		scheduler = OneAtATimeScheduler.usingExecutorFactory(executorFactorySpy);
	}

	@Test
	public void testSchedulerImpIsScheduler() {
		assertTrue(scheduler instanceof Scheduler);
	}

	@Test
	public void testOnlyForTestGetExecutorFactory() {
		OneAtATimeScheduler schedulerImp = OneAtATimeScheduler
				.usingExecutorFactory(executorFactorySpy);

		ExecutorFactory factory = schedulerImp.onlyForTestGetExecutorFactory();

		assertSame(factory, executorFactorySpy);
	}

	@Test
	public void testVirtualThreadExecutorCreatedAndSubmitCalledWithRunnable() {
		scheduler.scheduleTaskWithDelayInMillis(task1, DELAY_10MS);

		var scheduleCode = getSchedulerCodeFromScheduler();

		assertTrue(scheduleCode instanceof Runnable);
	}

	private Runnable getSchedulerCodeFromScheduler() {
		ExecutorServiceSpy virtualExecutorSpy = (ExecutorServiceSpy) executorFactorySpy.MCR
				.assertCalledParametersReturn("createVirtualThreadPerTaskExecutor");
		return (Runnable) virtualExecutorSpy.MCR
				.getParameterForMethodAndCallNumberAndParameter("submit", 0, "task");
	}

	@Test
	public void testVirtualThreadExecutorShutdownIsCalled() {
		scheduler.scheduleTaskWithDelayInMillis(task1, DELAY_10MS);

		ExecutorServiceSpy virtualExecutorSpy = (ExecutorServiceSpy) executorFactorySpy.MCR
				.assertCalledParametersReturn("createVirtualThreadPerTaskExecutor");
		virtualExecutorSpy.MCR.assertMethodWasCalled("shutdown");
	}

	@Test
	public void testVirtualThreadExecutorShutdownOnFinally() {
		ExecutorServiceSpy executorServiceSpy = new ExecutorServiceSpy();
		executorFactorySpy.MRV.setDefaultReturnValuesSupplier("createVirtualThreadPerTaskExecutor",
				() -> executorServiceSpy);
		executorServiceSpy.MRV.setAlwaysThrowException("submit", new RuntimeException());

		try {

			scheduler.scheduleTaskWithDelayInMillis(task1, DELAY_10MS);
		} catch (Exception e) {
			// TODO: handle exception
		}

		ExecutorServiceSpy virtualExecutorSpy = (ExecutorServiceSpy) executorFactorySpy.MCR
				.assertCalledParametersReturn("createVirtualThreadPerTaskExecutor");
		virtualExecutorSpy.MCR.assertMethodWasCalled("shutdown");
	}

	@Test
	public void testSingleScheduleExecutor() {
		scheduler.scheduleTaskWithDelayInMillis(task1, DELAY_10MS);
		var scheduleCode = getSchedulerCodeFromScheduler();
		scheduleCode.run();

		ScheduledExecutorServiceSpy singleScheduleExecutorSpy = (ScheduledExecutorServiceSpy) executorFactorySpy.MCR
				.assertCalledParametersReturn("createSingleThreadScheduledExecutor");
		singleScheduleExecutorSpy.MCR.assertParameters("schedule", 0);
		singleScheduleExecutorSpy.MCR.assertParameter("schedule", 0, "delay", DELAY_10MS);
		singleScheduleExecutorSpy.MCR.assertParameter("schedule", 0, "unit", TimeUnit.MILLISECONDS);

	}

	@Test
	public void testSingleScheduleExecutorShutdownIsCalled() {
		scheduler.scheduleTaskWithDelayInMillis(task1, DELAY_10MS);
		var scheduleCode = getSchedulerCodeFromScheduler();
		scheduleCode.run();

		ScheduledExecutorServiceSpy singleScheduleExecutorSpy = (ScheduledExecutorServiceSpy) executorFactorySpy.MCR
				.assertCalledParametersReturn("createSingleThreadScheduledExecutor");
		singleScheduleExecutorSpy.MCR.assertMethodWasCalled("shutdown");
	}

	@Test
	public void testSingleScheduleExecutorShutdownIsCalledOnFinally() {
		ScheduledExecutorServiceSpy executorServiceSpy = new ScheduledExecutorServiceSpy();
		executorServiceSpy.MRV.setAlwaysThrowException("schedule", new RuntimeException());
		executorFactorySpy.MRV.setDefaultReturnValuesSupplier("createSingleThreadScheduledExecutor",
				() -> executorServiceSpy);

		scheduler.scheduleTaskWithDelayInMillis(task1, DELAY_10MS);
		var scheduleCode = getSchedulerCodeFromScheduler();
		try {
			scheduleCode.run();
		} catch (Exception e) {
			// TODO: handle exception
		}

		ScheduledExecutorServiceSpy singleScheduleExecutorSpy = (ScheduledExecutorServiceSpy) executorFactorySpy.MCR
				.assertCalledParametersReturn("createSingleThreadScheduledExecutor");
		singleScheduleExecutorSpy.MCR.assertMethodWasCalled("shutdown");
	}

	@Test
	public void testRunWeakReferenceTask() {
		scheduler.scheduleTaskWithDelayInMillis(task1, DELAY_10MS);
		var scheduleCode = getSchedulerCodeFromScheduler();
		scheduleCode.run();

		var runnableSurroundingWeakReferenceForTask = getRunnableSurroundingWeakReference();

		WeakReferenceSpy<?> weakReferenceSpy = (WeakReferenceSpy<?>) executorFactorySpy.MCR
				.assertCalledParametersReturn("createWeakReferenceFromRunnable", task1);

		runnableSurroundingWeakReferenceForTask.run();

		RunnableTaskSpy taskSpy = (RunnableTaskSpy) weakReferenceSpy.MCR
				.assertCalledParametersReturn("get");
		taskSpy.MCR.assertMethodWasCalled("run");
	}

	private Runnable getRunnableSurroundingWeakReference() {
		ScheduledExecutorServiceSpy singleScheduleExecutorSpy = (ScheduledExecutorServiceSpy) executorFactorySpy.MCR
				.assertCalledParametersReturn("createSingleThreadScheduledExecutor");
		return (Runnable) singleScheduleExecutorSpy.MCR
				.getParameterForMethodAndCallNumberAndParameter("schedule", 0, "command");
	}

	@Test
	public void testOnlyOneSchedulerAtATime_OneTask() {
		scheduler.scheduleTaskWithDelayInMillis(task1, DELAY_10MS);

		ExecutorServiceSpy virtualExecutorSpy = (ExecutorServiceSpy) executorFactorySpy.MCR
				.assertCalledParametersReturn("createVirtualThreadPerTaskExecutor");
		FutureSpy<?> future = (FutureSpy<?>) virtualExecutorSpy.MCR.getReturnValue("submit", 0);
		future.MCR.assertMethodNotCalled("cancel");
	}

	@Test
	public void testOnlyOneSchedulerAtATime_twoTasks() {
		scheduler.scheduleTaskWithDelayInMillis(task1, DELAY_10MS);
		scheduler.scheduleTaskWithDelayInMillis(task2, DELAY_10MS);

		ExecutorServiceSpy virtualExecutorSpy = (ExecutorServiceSpy) executorFactorySpy.MCR
				.assertCalledParametersReturn("createVirtualThreadPerTaskExecutor");
		FutureSpy<?> future = (FutureSpy<?>) virtualExecutorSpy.MCR.getReturnValue("submit", 0);
		future.MCR.assertMethodWasCalled("cancel");
	}

	@Test(enabled = false)
	public void testCallSchedule() throws Exception {
		int delayInMillis = 40;
		TestClass testClass = new TestClass();

		scheduler.scheduleTaskWithDelayInMillis(() -> testClass.methodToBeScheduled(),
				delayInMillis);

		testClass.MCR.assertMethodNotCalled("methodToBeScheduled");
		Thread.sleep(80);
		testClass.MCR.assertMethodWasCalled("methodToBeScheduled");
	}

	class TestClass {

		public MethodCallRecorder MCR = new MethodCallRecorder();
		public MethodReturnValues MRV = new MethodReturnValues();

		public TestClass() {
			MCR.useMRV(MRV);
		}

		private void methodToBeScheduled() {
			MCR.addCall();
		}
	}

	@Test(enabled = false)
	// @Test
	public void realTestToScheduler() throws Exception {
		// ExecutorFactory executorFactory = new ExecutorFactoryImp();
		// Scheduler scheduler = SchedulerImp.usingExecutorFactory(executorFactory);

		TestClass2 testClass = new TestClass2();

		// int twoSecondsDelay = 2000;
		testClass.runSchedule();
		testClass = null;
		// System.gc();
		// scheduler.scheduleMethodWithDelay(() -> methodToBeScheduled2(), twoSecondsDelay);
		System.out.println("1. after schedule");
		// scheduler.shutdown();
		// scheduler = null;

		System.out.println("2. before sleep");
		Thread.sleep(4000);
		System.out.println("4. after sleep");
	}

	public String methodToBeScheduled() {
		System.out.println("Running on a scheduled virtual thread!");
		return "Completed";
	}

	class TestClass2 {
		int twoSecondsDelay = 2000;
		ExecutorFactory executorFactory = new ExecutorFactoryImp();
		Scheduler scheduler = OneAtATimeScheduler.usingExecutorFactory(executorFactory);

		public void runSchedule() {
			scheduler.scheduleTaskWithDelayInMillis(() -> methodToBeScheduled(), twoSecondsDelay);
			// scheduler.strongScheduleMethodWithDelay(() -> methodToBeScheduledStrong(),
			// twoSecondsDelay);
		}

		private String methodToBeScheduled() {
			System.out.println("3. Running on a weak scheduled virtual thread!");
			return "Completed";
		}

		private String methodToBeScheduledStrong() {
			System.out.println("Running on a strong scheduled virtual thread!");
			return "Completed";
		}

	}

}
