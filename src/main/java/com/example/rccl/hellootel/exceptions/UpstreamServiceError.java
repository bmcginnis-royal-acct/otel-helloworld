package com.example.rccl.hellootel.exceptions;

import com.example.rccl.hellootel.common.MiddlewareException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpstreamServiceError extends MiddlewareException {
  final String errorCode = ErrorCodes.UpstreamError.errCode;
  final Integer httpStatus = HttpStatus.INTERNAL_SERVER_ERROR.value();
  final String userMessage = "Error occurred calling IssueTrax REST API server.";

  public UpstreamServiceError() {
    super(String.format(ErrorCodes.UpstreamError.errMsg, "InventoryService"));
  }

}
