package com.example.carnest.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.carnest.Exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    // Thread pool cho upload song song
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    // Upload 1 ảnh
    public String upload(MultipartFile file, String folder) {
        validateFile(file);

        try {
            Map result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "carnest/" + folder,
                    "resource_type", "image",
                    "quality", "auto",
                    "fetch_format", "auto",
                    "transformation", "c_limit,w_1200,h_1200"  // resize max 1200px → nhỏ hơn → nhanh hơn
            ));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new BadRequestException("Upload thất bại: " + e.getMessage());
        }
    }

    // Upload nhiều ảnh SONG SONG
    public List<String> uploadMultiple(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("Không có file nào");
        }
        if (files.size() > 10) {
            throw new BadRequestException("Tối đa 10 ảnh");
        }

        // Validate tất cả trước khi upload
        files.forEach(this::validateFile);

        // Upload song song — tất cả ảnh cùng lúc
        List<CompletableFuture<String>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> upload(file, folder), executor))
                .collect(Collectors.toList());

        // Chờ tất cả xong
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    // Xóa ảnh
    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        // Chạy async — không cần chờ
        CompletableFuture.runAsync(() -> {
            try {
                String publicId = extractPublicId(imageUrl);
                if (publicId != null) {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                }
            } catch (IOException e) {
                System.err.println("Xóa ảnh thất bại: " + e.getMessage());
            }
        }, executor);
    }

    // Xóa nhiều ảnh
    public void deleteMultiple(List<String> urls) {
        if (urls == null) return;
        urls.forEach(this::delete);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File rỗng");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Chỉ chấp nhận file ảnh");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Ảnh tối đa 5MB");
        }
    }

    private String extractPublicId(String url) {
        try {
            String[] parts = url.split("/upload/");
            if (parts.length < 2) return null;
            String afterUpload = parts[1];
            if (afterUpload.startsWith("v")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }
            return afterUpload.substring(0, afterUpload.lastIndexOf("."));
        } catch (Exception e) {
            return null;
        }
    }
}