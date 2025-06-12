package com.bbs.bbsapi.services

import com.bbs.bbsapi.entities.PrivilegeDTO
import com.bbs.bbsapi.models.Privilege
import com.bbs.bbsapi.repositories.PrivilegeRepository
import org.springframework.stereotype.Service

@Service
class PrivilegeService(private val privilegeRepository: PrivilegeRepository) {

    fun getAllPrivileges(): List<Privilege> {
        return privilegeRepository.findAll()
    }

    fun getPrivilegeById(privilegeId: Long): Privilege? {
        return privilegeRepository.findById(privilegeId).orElse(null)
    }

    fun createPrivilege(privilegeDTO: PrivilegeDTO): Privilege {
        println("gets in here...>>>>>>>>> >>>>>>>C   >>> > >")
        return privilegeRepository.save(Privilege(name = privilegeDTO.name))
    }

    fun deletePrivilege(privilegeId: Long) {
        privilegeRepository.deleteById(privilegeId)
    }
}
