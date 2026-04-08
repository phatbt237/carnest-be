package com.example.carnest.Model;

import com.example.carnest.Enum.ReportReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ReportDTO {

    public static class CreateRequest {
        @NotBlank(message = "Loại đối tượng không được để trống")
        private String targetType;
        @NotNull(message = "ID đối tượng không được để trống")
        private Long targetId;
        @NotNull(message = "Lý do không được để trống")
        private ReportReason reason;
        private String description;

        public CreateRequest() {}
        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        public Long getTargetId() { return targetId; }
        public void setTargetId(Long targetId) { this.targetId = targetId; }
        public ReportReason getReason() { return reason; }
        public void setReason(ReportReason reason) { this.reason = reason; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class ResolveRequest {
        @NotBlank(message = "Kết quả xử lý không được để trống")
        private String resolution;

        public ResolveRequest() {}
        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }
    }

    public static class ReportResponse {
        private Long id;
        private String targetType;
        private Long targetId;
        private String reason;
        private String description;
        private String status;
        private String reporterUsername;
        private String resolution;
        private LocalDateTime createdAt;
        private LocalDateTime resolvedAt;

        public ReportResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        public Long getTargetId() { return targetId; }
        public void setTargetId(Long targetId) { this.targetId = targetId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getReporterUsername() { return reporterUsername; }
        public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }
        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getResolvedAt() { return resolvedAt; }
        public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    }
}