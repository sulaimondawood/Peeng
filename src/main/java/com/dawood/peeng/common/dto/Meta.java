package com.dawood.peeng.common.dto;

import lombok.*;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Meta {

  private Integer pageNumber;

  private Integer pageSize;

  private Long totalElements;

  private Integer totalPages;

  private boolean last;
}
