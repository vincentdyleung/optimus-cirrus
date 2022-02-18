/*
 * Morgan Stanley makes this available to you under the Apache License, Version 2.0 (the "License").
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package optimus.utils

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor

import org.slf4j.LoggerFactory

abstract class ErrorIgnoringFileVisitor extends SimpleFileVisitor[Path] {

  /**
   * There is basically nothing we can do about files disappearing while we are scanning, and given that they are no
   * longer there we probably don't need to do anything about them
   */
  override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = {
    ErrorIgnoringFileVisitor.log.debug(s"Unable to visit path: $file (probably it was deleted while we were scanning)")
    FileVisitResult.CONTINUE
  }
}

object ErrorIgnoringFileVisitor {
  private val log = LoggerFactory.getLogger(getClass)
}