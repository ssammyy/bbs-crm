package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.PortfolioItemDTO
import com.bbs.bbsapi.entities.PortfolioItemResponseDTO
import com.bbs.bbsapi.models.PortfolioFileType
import com.bbs.bbsapi.models.PortfolioItem
import com.bbs.bbsapi.repositories.PortfolioItemRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class PortfolioItemService(
    private val portfolioItemRepository: PortfolioItemRepository,
    private val digitalOceanService: DigitalOceanService
) {
    fun createPortfolioItem(dto: PortfolioItemDTO, file: MultipartFile): PortfolioItemResponseDTO {
        // Validate DTO and file
        if (dto.title.isBlank() || dto.client.isBlank() || dto.typeOfContract.isBlank() ||
            dto.description.isBlank() || dto.location.isBlank()) {
            throw IllegalArgumentException("All fields are required")
        }
        val allowedTypes = listOf("image/png", "image/jpeg", "image/jpg", "video/mp4", "video/avi", "video/mpeg")
        if (file.contentType !in allowedTypes) {
            throw IllegalArgumentException("Unsupported file type")
        }

        // Upload file
        val (objectKey, fileUrl) = digitalOceanService.uploadFile(file)

        // Create and save PortfolioItem
        val portfolioItem = PortfolioItem(
            title = dto.title,
            client = dto.client,
            typeOfContract = dto.typeOfContract,
            description = dto.description,
            location = dto.location,
            fileType = if (file.contentType?.startsWith("image") == true) PortfolioFileType.IMAGE else PortfolioFileType.VIDEO,
            fileName = file.originalFilename ?: "unknown",
            objectKey = objectKey
        )
        val savedItem = portfolioItemRepository.save(portfolioItem)

        // Return response DTO with fileUrl
        return PortfolioItemResponseDTO(
            id = savedItem.id,
            title = savedItem.title,
            client = savedItem.client,
            typeOfContract = savedItem.typeOfContract,
            description = savedItem.description,
            location = savedItem.location,
            fileType = savedItem.fileType,
            fileName = savedItem.fileName,
            objectKey = savedItem.objectKey,
            fileUrl = fileUrl,
            createdAt = savedItem.createdAt
        )
    }

    fun getAllPortfolioItems(): List<PortfolioItemResponseDTO> {
        return portfolioItemRepository.findAll().map { item ->
            PortfolioItemResponseDTO(
                id = item.id,
                title = item.title,
                client = item.client,
                typeOfContract = item.typeOfContract,
                description = item.description,
                location = item.location,
                fileType = item.fileType,
                fileName = item.fileName,
                objectKey = item.objectKey,
                fileUrl = digitalOceanService.getPublicFileUrl(item.objectKey.toString()),
                createdAt = item.createdAt
            )
        }
    }

    fun getPortfolioItemById(id: Long): PortfolioItemResponseDTO {
        val item = portfolioItemRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Portfolio item not found") }
        return PortfolioItemResponseDTO(
            id = item.id,
            title = item.title,
            client = item.client,
            typeOfContract = item.typeOfContract,
            description = item.description,
            location = item.location,
            fileType = item.fileType,
            fileName = item.fileName,
            objectKey = item.objectKey,
            fileUrl = digitalOceanService.getPublicFileUrl(item.objectKey.toString()),
            createdAt = item.createdAt
        )
    }

    fun updatePortfolioItem(id: Long, dto: PortfolioItemDTO, file: MultipartFile?): PortfolioItemResponseDTO {
        val item = portfolioItemRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Portfolio item not found") }

        // Validate DTO
        if (dto.title.isBlank() || dto.client.isBlank() || dto.typeOfContract.isBlank() ||
            dto.description.isBlank() || dto.location.isBlank()) {
            throw IllegalArgumentException("All fields are required")
        }

        // Update fields
        item.title = dto.title
        item.client = dto.client
        item.typeOfContract = dto.typeOfContract
        item.description = dto.description
        item.location = dto.location

        // Update file if provided
        if (file != null) {
            val allowedTypes = listOf("image/png", "image/jpeg", "image/jpg", "video/mp4", "video/avi", "video/mpeg")
            if (file.contentType !in allowedTypes) {
                throw IllegalArgumentException("Unsupported file type")
            }
            val (objectKey, fileUrl) = digitalOceanService.uploadFile(file)
            item.fileType = if (file.contentType?.startsWith("image") == true) PortfolioFileType.IMAGE else PortfolioFileType.VIDEO
            item.fileName = file.originalFilename ?: "unknown"
            item.objectKey = objectKey
            portfolioItemRepository.save(item)
            return PortfolioItemResponseDTO(
                id = item.id,
                title = item.title,
                client = item.client,
                typeOfContract = item.typeOfContract,
                description = item.description,
                location = item.location,
                fileType = item.fileType,
                fileName = item.fileName,
                objectKey = item.objectKey,
                fileUrl = fileUrl,
                createdAt = item.createdAt
            )
        }

        val savedItem = portfolioItemRepository.save(item)
        return PortfolioItemResponseDTO(
            id = savedItem.id,
            title = savedItem.title,
            client = savedItem.client,
            typeOfContract = savedItem.typeOfContract,
            description = savedItem.description,
            location = savedItem.location,
            fileType = savedItem.fileType,
            fileName = savedItem.fileName,
            objectKey = savedItem.objectKey,
            fileUrl = digitalOceanService.getPublicFileUrl(savedItem.objectKey.toString()),
            createdAt = savedItem.createdAt
        )
    }

    fun deletePortfolioItem(id: Long) {
        val item = portfolioItemRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Portfolio item not found") }
        digitalOceanService.deleteFile(item.objectKey.toString())
        portfolioItemRepository.delete(item)
    }
}