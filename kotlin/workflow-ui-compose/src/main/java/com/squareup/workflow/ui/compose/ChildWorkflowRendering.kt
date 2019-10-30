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
package com.squareup.workflow.ui.compose

import androidx.compose.Composable
import androidx.compose.ambient
import androidx.compose.unaryPlus
import androidx.ui.core.ContextAmbient
import com.squareup.workflow.ui.ViewRegistry

/**
 * Displays a rendering using the current [ViewRegistry][com.squareup.workflow.ui.ViewRegistry] to
 * generate the view.
 *
 * ## Example
 *
 * ```
 * data class FramedRendering(
 *   val borderColor: Color,
 *   val child: Any
 * )
 *
 * val FramedContainerBinding = bindCompose { rendering: FramedRendering ->
 *   Surface(border = Border(rendering.borderColor, 8.dp)) {
 *     ChildWorkflowRendering(rendering.child)
 *   }
 * }
 * ```
 *
 * @throws IllegalStateException If not called from a [bindCompose] context.
 */
@Composable
@Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER")
fun <RenderingT : Any> ChildWorkflowRendering(rendering: RenderingT) {
  val context = +ambient(ContextAmbient)
  val containerHints = +ambient(ContainerHintsAmbient)
  val registry = containerHints[ViewRegistry]
//  val viewStub = WorkflowViewStub(context)
//  viewStub.update(rendering, containerHints, registry)
  TODO("figure out how to actually drop this view into the composition")
}
