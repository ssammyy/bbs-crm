package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.dtos.ContractDTO
import com.bbs.bbsapi.dtos.ContractProgressUpdateDTO
import com.bbs.bbsapi.models.ClientContract
import com.bbs.bbsapi.services.ClientContractService
import com.bbs.bbsapi.services.ClientService
import com.bbs.bbsapi.services.DigitalOceanService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/contracts")
class ClientContractController(
    private val clientContractService: ClientContractService,
    private val clientService: ClientService,
    private val digitalOceanService: DigitalOceanService
) {
    @PostMapping
    fun createContract(@RequestBody contractDTO: ContractDTO): ResponseEntity<Map<String, Any?>> {
        val clientContract = ClientContract(
            clientId = contractDTO.clientId,
            projectName = contractDTO.projectName,
            boqAmount = contractDTO.boqAmount,
            projectStartDate = contractDTO.projectStartDate,
            projectDuration = contractDTO.projectDuration,
            contractFileUrl = contractDTO.contractFileUrl
        )
        val result = clientContractService.createContract(clientContract, contractDTO)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @GetMapping("/{clientId}")
    fun getContractByClientId(@PathVariable clientId: Long): ResponseEntity<Map<String, Any?>> {
        val contract = clientContractService.getContractWithInvoicesByClientId(clientId)
        return if (contract != null) {
            ResponseEntity.ok(contract)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{contractId}")
    fun updateContract(@PathVariable contractId: Long, @RequestBody updatedContract: ClientContract): ResponseEntity<ClientContract> {
        val contract = clientContractService.updateContract(contractId, updatedContract)
        return if (contract != null) {
            ResponseEntity.ok(contract)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{contractId}/progress")
    fun updateContractProgress(
        @PathVariable contractId: Long,
        @RequestBody progressUpdate: ContractProgressUpdateDTO
    ): ResponseEntity<Map<String, Any?>> {
        val result = clientContractService.updateContractProgress(contractId, progressUpdate)
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.notFound().build()
        }
    }
} 