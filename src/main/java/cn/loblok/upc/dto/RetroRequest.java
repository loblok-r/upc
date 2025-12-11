package cn.loblok.upc.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.time.LocalDate;

@Data
public class RetroRequest {

    @NotNull(message = "补签日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate retroDate;
}