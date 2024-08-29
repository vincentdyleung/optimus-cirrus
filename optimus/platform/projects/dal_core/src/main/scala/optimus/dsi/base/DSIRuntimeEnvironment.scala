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
package optimus.dsi.base

import optimus.platform._
import optimus.config._
import optimus.platform.dal.EntityResolver

private class DSIRuntimeEnvironment(config: RuntimeComponents, entityResolver: EntityResolver)
    extends RuntimeEnvironment(config, entityResolver)

object DSIUntweakedScenarioState {
  def apply(config: RuntimeComponents = null, entityResolver: EntityResolver = null): UntweakedScenarioState =
    UntweakedScenarioState(new DSIRuntimeEnvironment(config, entityResolver))
}