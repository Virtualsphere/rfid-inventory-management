package com.virtualsphere.rfidbackend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * The frontend accumulates a reportId from every scan-log call during a
 * one-by-one scanning session, then sends the whole list here on "Export" to
 * get one combined workbook covering that session.
 */
@Data
public class SingleReportExportRequest {
    @NotEmpty
    private List<Long> reportIds;
}
