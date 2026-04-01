package com.example.carnest.Service;

import com.example.carnest.Entity.Brand;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.BrandDTO;
import com.example.carnest.Repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    @Autowired
    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Transactional
    public BrandDTO.BrandResponse create(BrandDTO.CreateRequest request) {
        if (brandRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("Tên hãng đã tồn tại");
        }
        Brand brand = new Brand();
        brand.setName(request.getName().trim());
        brand.setSlug(generateSlug(request.getName()));
        brand.setLogoUrl(request.getLogoUrl());
        brand.setCountry(request.getCountry());
        brand.setDescription(request.getDescription());
        brand.setIsActive(true);
        brand = brandRepository.save(brand);
        return toResponse(brand);
    }

    @Transactional
    public BrandDTO.BrandResponse update(Long id, BrandDTO.UpdateRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        if (request.getName() != null && !request.getName().trim().equals(brand.getName())) {
            if (brandRepository.existsByName(request.getName().trim())) {
                throw new BadRequestException("Tên hãng đã tồn tại");
            }
            brand.setName(request.getName().trim());
            brand.setSlug(generateSlug(request.getName()));
        }
        if (request.getLogoUrl() != null) brand.setLogoUrl(request.getLogoUrl());
        if (request.getCountry() != null) brand.setCountry(request.getCountry());
        if (request.getDescription() != null) brand.setDescription(request.getDescription());
        if (request.getIsActive() != null) brand.setIsActive(request.getIsActive());
        brand = brandRepository.save(brand);
        return toResponse(brand);
    }

    public List<BrandDTO.BrandResponse> getAll() {
        return brandRepository.findAllActive().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<BrandDTO.BrandResponse> search(String keyword) {
        return brandRepository.searchByName(keyword).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BrandDTO.BrandResponse getById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        return toResponse(brand);
    }

    @Transactional
    public void delete(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        brand.setIsActive(false);
        brandRepository.save(brand);
    }

    private BrandDTO.BrandResponse toResponse(Brand b) {
        BrandDTO.BrandResponse r = new BrandDTO.BrandResponse();
        r.setId(b.getId()); r.setName(b.getName()); r.setSlug(b.getSlug());
        r.setLogoUrl(b.getLogoUrl()); r.setCountry(b.getCountry());
        r.setDescription(b.getDescription()); r.setIsActive(b.getIsActive());
        return r;
    }

    private String generateSlug(String name) {
        String slug = name.toLowerCase().trim()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        String base = slug; int c = 1;
        while (brandRepository.existsBySlug(slug)) { slug = base + "-" + c++; }
        return slug;
    }
}
