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
import java.util.concurrent.ScheduledExecutorService;

public class ExecutorFactoryImp implements ExecutorFactory {
	@Override
	public ExecutorService createVirtualThreadPerTaskExecutor() {
		return Executors.newVirtualThreadPerTaskExecutor();
		// ThreadFactory factory = Thread.ofPlatform().factory();
		// return Executors.newThreadPerTaskExecutor(factory);
	}

	@Override
	public ScheduledExecutorService createSingleThreadScheduledExecutor() {
		// Executors.newScheduledThreadPool(2);
		return Executors.newSingleThreadScheduledExecutor();
	}

	@Override
	public WeakReference<Runnable> createWeakReferenceFromRunnable(Runnable task) {
		return new WeakReference<>(task);
	}
}
