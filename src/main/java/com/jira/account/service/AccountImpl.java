package com.jira.account.service;

import com.jira.account.model.TeamInfo;
import com.jira.account.model.dao.TB_JIRA_USER_JpaRepository;
import com.jira.account.model.dto.AdminInfoDTO;
import com.jira.account.model.dto.UserInfoDTO;
import com.jira.account.model.entity.TB_ADMIN_Entity;
import com.jira.account.model.entity.TB_JIRA_USER_Entity;
import com.utils.WebClientUtils;
import com.utils.마크애니직원;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service("AdminInfo")
public class AccountImpl implements Account {

    @Autowired
    private com.jira.account.model.dao.TB_ADMIN_JpaRepository TB_ADMIN_JpaRepository;

    @Autowired
    private TB_JIRA_USER_JpaRepository TB_JIRA_USER_JpaRepository;

    @Autowired
    private 마크애니직원 마크애니직원;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
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

    /*
    *  전자문서 사업부 데이터 가져오는 메서드
    *  API에서 리턴하는 데이터의 양이 많아 버퍼 사이즈 단위로 나눠 지라 서버에 요청했음
    *  기존 WebClient mono 방식에서 비동기 요청 방식인 WebFlux로 데이터를 나눠 처리하였음
    *  그증 전자문서 사업부 사람 정보만 가져오기 위해 필터 처리함.
    *  원 데이터는 현재 이름(영문이름), 이름 이런 형식으로 데이터를 제공하기 때문에 영문 이름른 제거 하고 필터하여 가져옴
    *  마크애니 전자문서 직원의 지라 아이디를 가져오기 위한 데이터로 최조에만 실행
    * */
    @Override
    @Transactional
    public  Flux<UserInfoDTO> getCollectUserInfo() {

        System.out.println(마크애니직원.getEmployee());

        List<String> markany = 마크애니직원.getEmployee();

        AdminInfoDTO info = getAdminInfo(1);

        WebClient webClient = WebClientUtils.createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        String endpoint = "https://markany.atlassian.net/rest/api/3/users/search?startAt=0&maxResults=1000";

        //List<UserInfoDTO> response = WebClientUtils.get(webClient, endpoint, new ParameterizedTypeReference<List<UserInfoDTO>>() {}).block();
        int bufferSize = 50; // 버퍼 크기
        Flux<UserInfoDTO> response =  webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToFlux(UserInfoDTO.class)
                .buffer(bufferSize)
                .flatMapSequential(Flux::fromIterable) // 각각의 버퍼를 순차적으로 처리
                .filter(userInfo -> {
                    String name = 이름_추출(userInfo.getDisplayName());
                    return markany.contains(name);
                });


        response.doOnNext(userInfo -> {

            TB_JIRA_USER_Entity existUser = TB_JIRA_USER_JpaRepository.findByAccountId(userInfo.getAccountId());

            Optional<TeamInfo> 팀정보 = Optional.ofNullable(existUser)
                    .map(TB_JIRA_USER_Entity::getDisplayName) // existUser.getDisplayName()이 null일 경우 안전하게 처리
                    .map(name -> 이름_추출(name)) // 이름_추출()이 null일 경우 안전하게 처리
                    .map(TeamInfo::findInfoByName);
            String 팀 = 팀정보.map(TeamInfo::getTeam).orElse(null);
            String 파트 = 팀정보.map(TeamInfo::getPart).orElse(null);

            if(existUser != null){ // 정보 조회시 있으면 업데이트
                existUser.setDisplayName(userInfo.getDisplayName());
                existUser.setAccountId(userInfo.getAccountId());
                existUser.setEmailAddress(userInfo.getEmailAddress());
                existUser.setTeam(팀);
                existUser.setPart(파트);
                TB_JIRA_USER_JpaRepository.save(existUser);

            }else{ // 없으면 추가

                TB_JIRA_USER_Entity userEntity = new TB_JIRA_USER_Entity();
                userEntity.setDisplayName(userInfo.getDisplayName());
                userEntity.setAccountId(userInfo.getAccountId());
                userEntity.setEmailAddress(userInfo.getEmailAddress());
                userEntity.setTeam(팀);
                userEntity.setPart(파트);
                TB_JIRA_USER_JpaRepository.save(userEntity);
            }

        }).subscribe();


        return response;
    }

    @Override
    public String getUserNameByJiraAccountId(String accountId){
        logger.info("[::AccountImpl::] 사용자 이름 아이디로 조회"+accountId);
        TB_JIRA_USER_Entity 유저정보 = TB_JIRA_USER_JpaRepository.findByAccountId(accountId);
        String 이름 = 유저정보.getDisplayName();

        if(이름.contains("(")){
            int startIndex = 이름.indexOf("(");
            return 이름.substring(0, startIndex).trim(); // 대부분 모든 사람의 이름 뒤에 영어 이름이 붙어 나옴
        }else{
            return 이름; // epage dev 케이스
        }
    }

    @Override
    public String 이름_추출(String displayName) {

        if (displayName.contains("(")) {
            String name = displayName.substring(0, displayName.indexOf("(")).trim();
            return name;
        }

        return StringUtils.EMPTY;
    }
}
