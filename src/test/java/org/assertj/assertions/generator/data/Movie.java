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
 * Copyright 2012-2021 the original author or authors.
 */
package org.assertj.assertions.generator.data;

import org.assertj.assertions.generator.data.art.ArtWork;

import java.util.Date;

@SuppressWarnings("unused")
public class Movie extends ArtWork {

  private final Date releaseDate;
  private PublicCategory publicCategory;
  public String producer;
  public double rating;
  public boolean xrated;

  public Movie(String title, Date releaseDate) {
    super(title);
    this.releaseDate = releaseDate;
  }

  public Date getReleaseDate() {
    return releaseDate;
  }
  
  public PublicCategory getPublicCategory() {
    return publicCategory;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((releaseDate == null) ? 0 : releaseDate.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    Movie other = (Movie) obj;
    if (releaseDate == null) {
      if (other.releaseDate != null)
        return false;
    } else if (!releaseDate.equals(other.releaseDate))
      return false;
    return true;
  }

  public static class PublicCategory {
    private String name;

    public PublicCategory(String name) {
      super();
      this.name = name;
    }

    public String getName() {
      return name;
    }

  }

  static class InternalCategory {
    private String name;

    public InternalCategory(String name) {
      super();
      this.name = name;
    }

    public String getName() {
      return name;
    }

  }
}
