/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2025 the original author or authors.
 */
package org.assertj.assertions.generator.data;

import java.util.Collection;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public enum TreeEnum {

  PARENT, CHILD1(PARENT), CHILD2(PARENT);

  TreeEnum parent;

  private TreeEnum() {
    // nothing
  }

  private TreeEnum(TreeEnum parent) {
    this.parent = parent;
  }

  public Collection<TreeEnum> getChildren() {
    return Collections2.filter(Lists.newArrayList(values()), new Predicate<TreeEnum>() {
      @Override
      public boolean apply(TreeEnum input) {
        return this.equals(input.parent);
      }
    });
  }

}