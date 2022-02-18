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

object PropertyUtils {

  // Look first for -Dfoo.bar.baz and then for OPTIMUS_DIST_FOO_BAR_BAZ.
  // While this could be a generic utility, its real purpose is to lever
  // the auto-distribution of OPTIMUS_DIST-prefixed environment variables.
  val Prefix = "optimus.dist"
  val empty: Map[String, String] = Map()
  def flag(k: String) = get(k, false)
  def get(k: String, default: => Boolean): Boolean = get(k).map(parseBoolean(_)).getOrElse(default)
  def get(k: String, default: => Int): Int = get(k).map(_.toInt).getOrElse(default)
  def get(k: String, default: => String): String = get(k).getOrElse(default)
  def get(k: String, overrides: Map[String, String] = empty): Option[String] = {
    overrides.get(k) orElse {
      Option(System.getProperty(k))
    } orElse {
      val kv = (if (k.startsWith(Prefix)) k else s"$Prefix.$k").toUpperCase.replaceAll("\\.", "_")
      Option(System.getenv(kv))
    }
  }

  private def parseBoolean(value: String): Boolean =
    if (value.length == 0) true
    else if ("1" == value || "true" == value) true
    else if ("0" == value || "false" == value) false
    else throw new IllegalArgumentException(s"Can't parse >>$value<< to boolean.")

}