package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Entity.Report;
import com.example.carnest.Enum.ReportReason;
import com.example.carnest.Enum.ReportStatus;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Repository.ReportRepository;
import com.example.carnest.Repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Report", description = "Báo cáo vi phạm")
public class ReportController {

    @Autowired private ReportRepository reportRepository;
    @Autowired private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Báo cáo vi phạm")
    public ResponseEntity<AuthDTO.MessageResponse> create(
            @AuthenticationPrincipal CustomUserDetails u, @RequestBody Map<String, Object> body) {
        Report r = new Report();
        r.setReporter(userRepository.findById(u.getUserId()).orElseThrow());
        r.setTargetType((String) body.get("targetType"));
        r.setTargetId(Long.parseLong(body.get("targetId").toString()));
        r.setReason(ReportReason.valueOf((String) body.get("reason")));
        r.setDescription((String) body.get("description"));
        r.setStatus(ReportStatus.PENDING);
        r = reportRepository.save(r);

        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId()); m.put("status", r.getStatus().name());
        m.put("message", "Báo cáo đã được ghi nhận, chúng tôi sẽ xem xét trong 24h");
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthDTO.MessageResponse.builder()
                .status(201).message("Báo cáo thành công").data(m).build());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Danh sách report (Admin)")
    public ResponseEntity<AuthDTO.MessageResponse> adminList(
            @RequestParam(required = false) String status) {
        List<Report> reports;
        if (status != null) {
            reports = reportRepository.findByStatus(ReportStatus.valueOf(status));
        } else {
            reports = reportRepository.findAll();
        }
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(reports.stream().map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", r.getId()); m.put("targetType", r.getTargetType());
                    m.put("targetId", r.getTargetId()); m.put("reason", r.getReason().name());
                    m.put("description", r.getDescription()); m.put("status", r.getStatus().name());
                    m.put("reporterUsername", r.getReporter().getUsername());
                    m.put("createdAt", r.getCreatedAt());
                    return m;
                }).collect(Collectors.toList())).build());
    }

    @PutMapping("/admin/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xử lý report (Admin)")
    public ResponseEntity<AuthDTO.MessageResponse> resolve(
            @AuthenticationPrincipal CustomUserDetails u, @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Report r = reportRepository.findById(id).orElseThrow();
        r.setStatus(ReportStatus.RESOLVED);
        r.setResolution(body.get("resolution"));
        r.setHandledBy(userRepository.findById(u.getUserId()).orElseThrow());
        r.setResolvedAt(java.time.LocalDateTime.now());
        reportRepository.save(r);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Đã xử lý report").build());
    }
}