package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.ProductDTO
import com.bbs.bbsapi.entities.ProductResponseDTO
import com.bbs.bbsapi.models.PortfolioFileType
import com.bbs.bbsapi.models.Product
import com.bbs.bbsapi.repos.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val digitalOceanService: DigitalOceanService
) {
    fun createProduct(dto: ProductDTO, file: MultipartFile): ProductResponseDTO {
        // Validate DTO and file
        if (dto.productTitle.isBlank() || dto.description.isBlank() || dto.videoUrl.isBlank()) {
            throw IllegalArgumentException("All fields are required")
        }
        val allowedTypes = listOf("image/png", "image/jpeg", "image/jpg", "video/mp4", "video/avi", "video/mpeg")
        if (file.contentType !in allowedTypes) {
            throw IllegalArgumentException("Unsupported file type")
        }

        // Upload file
        val (objectKey, fileUrl) = digitalOceanService.uploadFile(file)

        // Create and save Product
        val product = Product(
            productTitle = dto.productTitle,
            productVp = dto.productVp,
            productHook = dto.productHook,
            videoUrl = dto.videoUrl,
            description = dto.description,
            fileType = if (file.contentType?.startsWith("image") == true) PortfolioFileType.IMAGE else PortfolioFileType.VIDEO,
            fileName = file.originalFilename ?: "unknown",
            objectKey = objectKey
        )
        val savedProduct = productRepository.save(product)

        // Return response DTO with fileUrl
        return ProductResponseDTO(
            id = savedProduct.id,
            productTitle = savedProduct.productTitle,
            description = savedProduct.description,
            fileType = savedProduct.fileType,
            fileName = savedProduct.fileName,
            objectKey = savedProduct.objectKey,
            fileUrl = fileUrl,
            createdAt = savedProduct.createdAt,
            productVp = savedProduct.productVp,
            productHook = savedProduct.productHook,
            videoUrl = savedProduct.videoUrl
        )
    }

    fun getAllProducts(): List<ProductResponseDTO> {
        return productRepository.findAll().map { product ->
            ProductResponseDTO(
                id = product.id,
                productTitle = product.productTitle,
                description = product.description,
                fileType = product.fileType,
                fileName = product.fileName,
                objectKey = product.objectKey,
                fileUrl = digitalOceanService.getPublicFileUrl(product.objectKey.toString()),
                createdAt = product.createdAt,
                productVp = product.productVp,
                productHook = product.productHook,
                videoUrl = product.videoUrl
            )
        }
    }

    fun getProductById(id: Long): ProductResponseDTO {
        val product = productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product not found") }
        return ProductResponseDTO(
            id = product.id,
            productTitle = product.productTitle,
            description = product.description,
            fileType = product.fileType,
            fileName = product.fileName,
            objectKey = product.objectKey,
            fileUrl = digitalOceanService.getPublicFileUrl(product.objectKey.toString()),
            createdAt = product.createdAt,
            productVp = product.productVp,
            productHook = product.productHook,
            videoUrl = product.videoUrl
        )
    }

    fun updateProduct(id: Long, dto: ProductDTO, file: MultipartFile?): ProductResponseDTO {
        // Find existing product
        val product = productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product not found") }

        // Validate DTO
        if (dto.productTitle.isBlank() || dto.description.isBlank() || dto.videoUrl.isBlank()) {
            throw IllegalArgumentException("All fields are required")
        }

        // Update fields
        product.productTitle = dto.productTitle
        product.description = dto.description
        product.productVp = dto.productVp
        product.productHook = dto.productHook
        product.videoUrl = dto.videoUrl

        // Update file if provided
        if (file != null) {
            val allowedTypes = listOf("image/png", "image/jpeg", "image/jpg", "video/mp4", "video/avi", "video/mpeg")
            if (file.contentType !in allowedTypes) {
                throw IllegalArgumentException("Unsupported file type")
            }

            // Delete old file from DigitalOcean Spaces
            digitalOceanService.deleteFile(product.objectKey.toString())

            // Upload new file
            val (objectKey, fileUrl) = digitalOceanService.uploadFile(file)
            product.fileType = if (file.contentType?.startsWith("image") == true) PortfolioFileType.IMAGE else PortfolioFileType.VIDEO
            product.fileName = file.originalFilename ?: "unknown"
            product.objectKey = objectKey

            // Save updated product
            val savedProduct = productRepository.save(product)
            return ProductResponseDTO(
                id = savedProduct.id,
                productTitle = savedProduct.productTitle,
                description = savedProduct.description,
                fileType = savedProduct.fileType,
                fileName = savedProduct.fileName,
                objectKey = savedProduct.objectKey,
                fileUrl = fileUrl,
                createdAt = savedProduct.createdAt,
                productVp = savedProduct.productVp,
                productHook = savedProduct.productHook,
                videoUrl = savedProduct.videoUrl
            )
        }

        // Save updated product without file change
        val savedProduct = productRepository.save(product)
        return ProductResponseDTO(
            id = savedProduct.id,
            productTitle = savedProduct.productTitle,
            description = savedProduct.description,
            fileType = savedProduct.fileType,
            fileName = savedProduct.fileName,
            objectKey = savedProduct.objectKey,
            fileUrl = digitalOceanService.getPublicFileUrl(savedProduct.objectKey.toString()),
            createdAt = savedProduct.createdAt,
            productVp = savedProduct.productVp,
            productHook = savedProduct.productHook,
            videoUrl = savedProduct.videoUrl
        )
    }

    fun deleteProduct(id: Long) {
        // Find product
        val product = productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product not found") }

        // Delete file from DigitalOcean Spaces
        digitalOceanService.deleteFile(product.objectKey.toString())

        // Delete product from database
        productRepository.delete(product)
    }
}