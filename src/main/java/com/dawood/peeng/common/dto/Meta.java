package com.dawood.peeng.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Meta {

  private Integer page;

  private Integer size;

  private Long totalElements;

  private Integer totalPages;
}
