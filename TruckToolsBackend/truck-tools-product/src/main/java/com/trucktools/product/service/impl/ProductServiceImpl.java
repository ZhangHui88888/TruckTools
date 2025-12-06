package com.trucktools.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trucktools.common.core.domain.PageResult;
import com.trucktools.common.exception.BusinessException;
import com.trucktools.product.dto.*;
import com.trucktools.product.entity.Product;
import com.trucktools.product.mapper.ProductMapper;
import com.trucktools.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 产品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Value("${app.upload.base-url:http://localhost:8080}")
    private String uploadBaseUrl;

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public PageResult<ProductVO> getPage(ProductQueryParam param) {
        Page<Product> page = new Page<>(param.getPage(), param.getPageSize());
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, param.getUserId())
                .like(StrUtil.isNotBlank(param.getKeyword()), Product::getOeNo, param.getKeyword())
                .or(StrUtil.isNotBlank(param.getKeyword()))
                .like(StrUtil.isNotBlank(param.getKeyword()), Product::getXkNo, param.getKeyword())
                .eq(StrUtil.isNotBlank(param.getBrandCode()), Product::getBrandCode, param.getBrandCode())
                .eq(StrUtil.isNotBlank(param.getOeNo()), Product::getOeNo, param.getOeNo());
        
        // 排序（默认按XK NO.升序）
        if (StrUtil.isNotBlank(param.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(param.getSortOrder());
            switch (param.getSortField()) {
                case "brandCode" -> wrapper.orderBy(true, isAsc, Product::getBrandCode);
                case "xkNo" -> wrapper.orderBy(true, isAsc, Product::getXkNo);
                case "oeNo" -> wrapper.orderBy(true, isAsc, Product::getOeNo);
                case "priceAvg" -> wrapper.orderBy(true, isAsc, Product::getPriceAvg);
                case "createdAt" -> wrapper.orderBy(true, isAsc, Product::getCreatedAt);
                default -> wrapper.orderByAsc(Product::getXkNo);
            }
        } else {
            wrapper.orderByAsc(Product::getXkNo);
        }

        Page<Product> result = productMapper.selectPage(page, wrapper);
        
        List<ProductVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), param.getPage(), param.getPageSize());
    }

    @Override
    public ProductVO getDetail(Long userId, Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || !product.getUserId().equals(userId)) {
            throw new BusinessException("产品不存在");
        }
        return toVO(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, ProductRequest request) {
        Product product = new Product();
        BeanUtil.copyProperties(request, product);
        product.setUserId(userId);
        productMapper.insert(product);
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long id, ProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null || !product.getUserId().equals(userId)) {
            throw new BusinessException("产品不存在");
        }
        BeanUtil.copyProperties(request, product, "id", "userId", "createdAt");
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || !product.getUserId().equals(userId)) {
            throw new BusinessException("产品不存在");
        }
        productMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, userId)
                .in(Product::getId, ids);
        productMapper.delete(wrapper);
    }

    @Override
    public List<BrandVO> getBrandList(Long userId) {
        return productMapper.selectBrandList(userId);
    }

    @Override
    public List<ProductVO> searchByOeNo(Long userId, String oeNos) {
        if (StrUtil.isBlank(oeNos)) {
            return new ArrayList<>();
        }
        
        // 支持逗号、换行分隔的多个OE号
        List<String> oeNoList = Arrays.stream(oeNos.split("[,;\n\r]+"))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        
        if (oeNoList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Product> products = productMapper.selectByOeNos(userId, oeNoList);
        return products.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private ProductVO toVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtil.copyProperties(product, vo);
        vo.setId(String.valueOf(product.getId()));
        
        // 生成图片URL（使用相对路径，让前端通过代理访问）
        if (StrUtil.isNotBlank(product.getImagePath())) {
            // imagePath 已经是 /uploads/products/... 格式，直接使用
            vo.setImageUrl(product.getImagePath());
        }
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadImage(Long userId, Long productId, MultipartFile file) {
        // 验证产品归属
        Product product = productMapper.selectById(productId);
        if (product == null || !product.getUserId().equals(userId)) {
            throw new BusinessException("产品不存在");
        }

        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException("文件名不能为空");
        }

        // 验证文件类型
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!".jpg".equals(extension) && !".jpeg".equals(extension) && 
            !".png".equals(extension) && !".gif".equals(extension) && 
            !".webp".equals(extension) && !".tiff".equals(extension)) {
            throw new BusinessException("只支持 jpg/jpeg/png/gif/webp/tiff 格式的图片");
        }

        try {
            // 生成文件路径: uploads/products/{date}/{uuid}.{ext}
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String newFileName = UUID.randomUUID().toString().replace("-", "") + extension;
            String relativePath = "/uploads/products/" + dateDir + "/" + newFileName;
            
            // 创建目录
            Path targetDir = Paths.get(uploadPath, "products", dateDir);
            Files.createDirectories(targetDir);
            
            // 保存文件
            Path targetPath = targetDir.resolve(newFileName);
            file.transferTo(targetPath.toFile());
            
            // 删除旧图片（如果存在）
            if (StrUtil.isNotBlank(product.getImagePath())) {
                try {
                    String oldPath = product.getImagePath().replace("/uploads/", "");
                    Path oldFile = Paths.get(uploadPath, oldPath);
                    Files.deleteIfExists(oldFile);
                } catch (Exception e) {
                    log.warn("删除旧图片失败: {}", product.getImagePath(), e);
                }
            }
            
            // 更新数据库
            product.setImagePath(relativePath);
            productMapper.updateById(product);
            
            log.info("产品图片上传成功: productId={}, path={}", productId, relativePath);
            return relativePath;
            
        } catch (IOException e) {
            log.error("图片上传失败", e);
            throw new BusinessException("图片上传失败: " + e.getMessage());
        }
    }
}

