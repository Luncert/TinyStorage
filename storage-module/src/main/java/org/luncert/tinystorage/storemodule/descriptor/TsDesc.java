package org.luncert.tinystorage.storemodule.descriptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TsDesc extends Descriptor {

  private TsRuntimeDesc runtime;

  private TsCacheDesc cache;
}
