package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.dtos.ProductResponseDTO
import com.bbs.bbsapi.entities.ProductDTO
import com.bbs.bbsapi.services.ProductService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createProduct(
        @RequestPart("data") productDTO: ProductDTO,
        @RequestPart("files") files: List<MultipartFile>
    ): ResponseEntity<ProductResponseDTO> {
        try {
            val product = productService.createProduct(productDTO, files)
            return ResponseEntity.ok(product)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(null)
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(null)
        }
    }

    @GetMapping
    fun getAllProducts(): ResponseEntity<List<ProductResponseDTO>> {
        val products = productService.getAllProducts()
        return ResponseEntity.ok(products)
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): ResponseEntity<ProductResponseDTO> {
        return try {
            val product = productService.getProductById(id)
            ResponseEntity.ok(product)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateProduct(
        @PathVariable id: Long,
        @RequestPart("data") productDTO: ProductDTO,
        @RequestPart("file", required = false) file: MultipartFile?
    ): ResponseEntity<ProductResponseDTO> {
        return try {
            val updatedProduct = productService.updateProduct(id, productDTO, file)
            ResponseEntity.ok(updatedProduct)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(null)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(null)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            productService.deleteProduct(id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(500).build()
        }
    }
}