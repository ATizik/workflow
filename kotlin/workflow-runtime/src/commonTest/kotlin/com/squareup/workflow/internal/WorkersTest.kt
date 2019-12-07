/*
 * Copyright 2019 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.squareup.workflow.internal

import com.squareup.workflow.Worker
import com.squareup.workflow.asWorker
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WorkersTest {

  @Test fun `propagates backpressure`() {
    val channel = Channel<String>()
    val worker = channel.asWorker()
    // Used to assert ordering.
    val counter = AtomicInteger(0)

    runBlocking {
      val workerOutputs = launchWorker(
          worker,
          workerDiagnosticId = 0,
          workflowDiagnosticId = 0,
          diagnosticListener = null
      )

      launch(start = UNDISPATCHED) {
        assertEquals(0, counter.getAndIncrement())
        channel.send("a")
        assertEquals(2, counter.getAndIncrement())
        channel.send("b")
        assertEquals(4, counter.getAndIncrement())
        channel.close()
        assertEquals(5, counter.getAndIncrement())
      }
      yield()
      assertEquals(1, counter.getAndIncrement())

      assertEquals("a", workerOutputs.poll()!!.value)
      yield()
      assertEquals(3, counter.getAndIncrement())

      assertEquals("b", workerOutputs.poll()!!.value)
      yield()
      assertEquals(6, counter.getAndIncrement())

      // Cancel the worker so we can exit this loop.
      workerOutputs.cancel()
    }
  }

  @Test fun `emits diagnostic events`() {
    val channel = Channel<String>()
    val worker = Worker.create<String> { emitAll(channel) }
    val workerId = 0L
    val workflowId = 1L
    val listener = RecordingDiagnosticListener()

    runBlocking {
      val outputs = launchWorker(worker, workerId, workflowId, listener)

      // Start event is sent by WorkflowNode.
      yield()
      assertTrue(listener.consumeEvents().isEmpty())

      channel.send("foo")
      outputs.receive()

      assertEquals("onWorkerOutput(0, 1, foo)", listener.consumeNextEvent())

      channel.close()
      yield()

      assertEquals("onWorkerStopped(0, 1)", listener.consumeNextEvent())
      // Read the last event so the scope can complete.
      assertTrue(outputs.receive().isDone)
    }
  }

  @Test fun `emits done when complete immediately`() {
    val channel = Channel<String>(capacity = 1)

    runBlocking {
      val workerOutputs = launchWorker(channel.asWorker(), 0, 0, null)
      assertTrue(workerOutputs.isEmpty)

      channel.close()
      assertTrue(workerOutputs.receive().isDone)
    }
  }

  @Test fun `emits done when complete after sending`() {
    val channel = Channel<String>(capacity = 1)

    runBlocking {
      val workerOutputs = launchWorker(channel.asWorker(), 0, 0, null)
      assertTrue(workerOutputs.isEmpty)

      channel.send("foo")
      assertEquals("foo", workerOutputs.receive().value)

      channel.close()
      assertTrue(workerOutputs.receive().isDone)
    }
  }

  @Test fun `does not emit done when failed`() {
    val channel = Channel<String>(capacity = 1)

    runBlocking {
      // Needed so that cancelling the channel doesn't cancel our job, which means receive will
      // throw the JobCancellationException instead of the actual channel failure.
      supervisorScope {
        val workerOutputs = launchWorker(channel.asWorker(), 0, 0, null)
        assertTrue(workerOutputs.isEmpty)

        channel.close(ExpectedException())
        assertFailsWith<ExpectedException> { workerOutputs.receive() }
      }
    }
  }

  @Test fun `completes after emitting done`() {
    val channel = Channel<String>(capacity = 1)

    runBlocking {
      val workerOutputs = launchWorker(channel.asWorker(), 0, 0, null)
      channel.close()
      assertTrue(workerOutputs.receive().isDone)

      assertTrue(channel.isClosedForReceive)
    }
  }

  private class ExpectedException : RuntimeException()
}
