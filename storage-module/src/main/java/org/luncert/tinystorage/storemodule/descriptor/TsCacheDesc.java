package org.luncert.tinystorage.storemodule.descriptor;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class TsCacheDesc extends Descriptor {

  private Map<String, TsBucketDesc> buckets;
}
