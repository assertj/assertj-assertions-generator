package org.fest.assertions.generator;

import java.util.Date;

public class Movie extends ArtWork {

  private final Date releaseDate;

  public Movie(String title, Date releaseDate) {
    super(title);
    this.releaseDate = releaseDate;
  }

  public Date getReleaseDate() {
    return releaseDate;
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
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    Movie other = (Movie) obj;
    if (releaseDate == null) {
      if (other.releaseDate != null) return false;
    } else if (!releaseDate.equals(other.releaseDate)) return false;
    return true;
  }

  
}
