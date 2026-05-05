package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "Upload", description = "Upload ảnh lên Cloudinary")
public class UploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload 1 ảnh")
    public ResponseEntity<AuthDTO.MessageResponse> uploadImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "general") String folder) {

        String url = cloudinaryService.upload(file, folder);

        Map<String, String> data = new HashMap<>();
        data.put("url", url);

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Upload thành công").data(data).build());
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload nhiều ảnh (tối đa 10)")
    public ResponseEntity<AuthDTO.MessageResponse> uploadImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "general") String folder) {

        List<String> urls = cloudinaryService.uploadMultiple(files, folder);

        Map<String, Object> data = new HashMap<>();
        data.put("urls", urls);
        data.put("count", urls.size());

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Upload " + urls.size() + " ảnh thành công").data(data).build());
    }

    @DeleteMapping("/image")
    @Operation(summary = "Xóa ảnh")
    public ResponseEntity<AuthDTO.MessageResponse> deleteImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String url) {

        cloudinaryService.delete(url);

        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Xóa ảnh thành công").build());
    }
}