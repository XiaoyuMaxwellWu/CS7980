package com.eventualconsistency.demo.vo;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZRankEntry {
  private String csKey;
  private String csValue;
  private long insertTime;
  private int hitCount;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ZRankEntry that = (ZRankEntry) o;
    return insertTime == that.insertTime &&
        hitCount == that.hitCount &&
        Objects.equals(csKey, that.csKey) &&
        Objects.equals(csValue, that.csValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(csKey, csValue, insertTime, hitCount);
  }
}
