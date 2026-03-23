package com.edu.platform.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RejectProblemRequest {

    @NotBlank(message = "반려 사유를 입력해주세요.")
    private String rejectReason;
}
