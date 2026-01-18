package com.fransua.burger_order_api.order.dto.response;

import lombok.Data;

@Data
public class UploadStatsResponse {

  private int successfulCount = 0;
  private int failedCount = 0;
  private int totalRecords = 0;

  public void incrementSuccessfulCount() {
    ++successfulCount;
    ++totalRecords;
  }

  public void incrementFailedCount() {
    ++failedCount;
    ++totalRecords;
  }
}
