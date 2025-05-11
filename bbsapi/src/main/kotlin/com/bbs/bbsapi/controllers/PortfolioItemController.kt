package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.entities.PortfolioItemDTO
import com.bbs.bbsapi.entities.PortfolioItemResponseDTO
import com.bbs.bbsapi.models.PortfolioItem
import com.bbs.bbsapi.services.PortfolioItemService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/portfolio-items")
class PortfolioItemController(
    private val portfolioItemService: PortfolioItemService
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createPortfolioItem(
        @RequestPart("data") portfolioItemDTO: PortfolioItemDTO,
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<PortfolioItemResponseDTO> {
        try {
            val portfolioItem = portfolioItemService.createPortfolioItem(portfolioItemDTO, file)
            return ResponseEntity.ok(portfolioItem)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(null)
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(null)
        }
    }

    @GetMapping
    fun getAllPortfolioItems(): ResponseEntity<List<PortfolioItemResponseDTO>> {
        val portfolioItems = portfolioItemService.getAllPortfolioItems()
        return ResponseEntity.ok(portfolioItems)
    }

    @GetMapping("/{id}")
    fun getPortfolioItemById(@PathVariable id: Long): ResponseEntity<PortfolioItemResponseDTO> {
        return try {
            val portfolioItem = portfolioItemService.getPortfolioItemById(id)
            ResponseEntity.ok(portfolioItem)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updatePortfolioItem(
        @PathVariable id: Long,
        @RequestPart("data") portfolioItemDTO: PortfolioItemDTO,
        @RequestPart(name = "file", required = false) file: MultipartFile?
    ): ResponseEntity<PortfolioItemResponseDTO> {
        return try {
            val updatedItem = portfolioItemService.updatePortfolioItem(id, portfolioItemDTO, file)
            ResponseEntity.ok(updatedItem)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(null)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(null)
        }
    }

    @DeleteMapping("/{id}")
    fun deletePortfolioItem(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            portfolioItemService.deletePortfolioItem(id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}