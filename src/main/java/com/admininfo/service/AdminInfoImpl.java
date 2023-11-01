package com.admininfo.service;

import com.admininfo.dao.TB_ADMIN_JpaRepository;
import com.admininfo.dto.AdminInfoDTO;
import com.admininfo.entity.TB_ADMIN_Entity;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service("AdminInfo")
public class AdminInfoImpl implements AdminInfo {

    @Autowired
    private TB_ADMIN_JpaRepository TB_ADMIN_JpaRepository;

    public AdminInfoDTO getAdminInfo(int personalId){

        Optional<TB_ADMIN_Entity> search_result = TB_ADMIN_JpaRepository.findById(personalId);

        if(search_result.isPresent()){
            TB_ADMIN_Entity entity = search_result.get();
            ModelMapper modelMapper = new ModelMapper();
            AdminInfoDTO admin_info = modelMapper.map(entity, AdminInfoDTO.class);

            return admin_info;
        } else {

            return null;
        }
    }

}
