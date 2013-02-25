package org.fest.assertions.generator.data;

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