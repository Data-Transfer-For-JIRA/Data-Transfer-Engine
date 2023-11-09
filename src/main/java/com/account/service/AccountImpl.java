package com.account.service;

import com.account.dao.TB_ADMIN_JpaRepository;
import com.account.dto.AdminInfoDTO;
import com.account.dto.UserInfoDTO;
import com.account.entity.TB_ADMIN_Entity;
import com.utils.ProjectConfig;
import com.utils.WebClientUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Service("AdminInfo")
public class AccountImpl implements Account {

    @Autowired
    private TB_ADMIN_JpaRepository TB_ADMIN_JpaRepository;

    @Autowired
    private ProjectConfig projectConfig;


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


    @Override
    public Map<String,String> getCollectUserInfo() {

        System.out.println(projectConfig.users);

        List<String> markany = projectConfig.users;

        AdminInfoDTO info = getAdminInfo(1);

        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint = "https://markany.atlassian.net/rest/api/3/users/search?startAt=0&maxResults=10";

        List<UserInfoDTO> response = WebClientUtils.get(webClient, endpoint, new ParameterizedTypeReference<List<UserInfoDTO>>() {}).block();


        System.out.println(response);

        return null;
    }
}
