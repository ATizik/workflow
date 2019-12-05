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
package com.squareup.workflow

import com.squareup.workflow.internal.WorkflowNode
import com.squareup.workflow.internal.id
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Mode.AverageTime
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import java.util.concurrent.TimeUnit.MILLISECONDS

@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(AverageTime)
@OutputTimeUnit(MILLISECONDS)
open class WorkflowNodeBenchmark {

  private val deepProps = CompletableDeferred<Int>()
  private lateinit var renderings: Flow<Int>
  private lateinit var dispatcher: ExecutorCoroutineDispatcher

  @Setup open fun setUp() {

  }

  @Benchmark open fun deepRender() {
    val workflow = FractalWorkflow(childCount = 1, depth = 100)
    val node = WorkflowNode(
        workflow.id(),
        workflow,
        initialProps = Unit,
        snapshot = null,
        baseContext = TODO()
    )

    TODO()
  }

  @Benchmark open fun bushyRender() {
    TODO()
  }

  @Benchmark open fun deepBushyRender() {

  }
}

private class FractalWorkflow(
  private val childCount: Int,
  private val depth: Int
) : StatefulWorkflow<Unit, Unit, Nothing, Unit>() {

  override fun initialState(
    props: Unit,
    snapshot: Snapshot?
  ) {
    TODO("not implemented")
  }

  override fun render(
    props: Unit,
    state: Unit,
    context: RenderContext<Unit, Nothing>
  ) {
    TODO("not implemented")
  }

  override fun snapshotState(state: Unit): Snapshot = throw NotImplementedError()
}
