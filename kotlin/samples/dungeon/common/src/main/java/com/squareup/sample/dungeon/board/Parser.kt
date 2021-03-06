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
package com.squareup.sample.dungeon.board

import com.squareup.sample.dungeon.board.BoardCell.Companion.EMPTY_FLOOR
import okio.Source
import okio.buffer

fun Source.parseBoard(): Board {
  var lines = buffer().let { generateSequence { it.readUtf8Line() }.toList() }

  // Trim leading and trailing empty lines.
  lines = lines.dropWhile { it.isBlank() }
      .dropLastWhile { it.isBlank() }

  var rows = lines.map { it.asBoardCells() }
  val height = rows.size
  val width = rows.asSequence()
      .map { it.size }
      .max()!!

  // Pad short rows.
  rows = rows.map { row ->
    if (row.size == width) row
    else {
      row + List(width - row.size) { EMPTY_FLOOR }
    }
  }

  if (height < width) {
    // Too short, pad top and bottom.
    val verticalPadding = (width - height) / 2
    val paddingRow = List(width) { EMPTY_FLOOR }
    val topPads = List(verticalPadding) { paddingRow }
    val bottomPads = List(width - (height + verticalPadding)) { paddingRow }
    rows = topPads + rows + bottomPads
  } else if (width < height) {
    // Too narrow, pad all rows.
    val leftPadding = (height - width) / 2
    val rightPadding = (height - (width + leftPadding))
    val leftPad = List(leftPadding) { EMPTY_FLOOR }
    val rightPad = List(rightPadding) { EMPTY_FLOOR }
    rows = rows.map { row -> leftPad + row + rightPad }
  }

  // Concatenate rows to one giant list.
  return Board.fromRows(rows)
}
