package org.luncert.tinystorage.storemodule.descriptor;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TsBucketDesc extends Descriptor {

  private String id;

  private long bucketSize;

  private long lastAccessTime;

  private List<TsFileDesc> files;
}
