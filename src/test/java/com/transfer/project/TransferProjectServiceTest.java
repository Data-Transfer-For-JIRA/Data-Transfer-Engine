package com.transfer.project;

import com.transfer.project.model.dao.TB_JML_JpaRepository;
import com.transfer.project.model.entity.TB_JML_Entity;
import com.transfer.project.service.TransferProjcetImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransferProjectServiceTest {

    @InjectMocks
    private TransferProjcetImpl transferProjcet;
    @Mock
    private TB_JML_JpaRepository jpaRepository; // 목 객체

    @Test
    public void 지라키_이름_생성_테스트() throws Exception{

        // 테스트 데이터 설정
        TB_JML_Entity entity = new TB_JML_Entity();
        entity.setKey("TWSS3");

        // 모의 객체 동작 정의
        //when(jpaRepository.count()).thenReturn(0L); // 최초 삽입시
        when(jpaRepository.count()).thenReturn(1L);
        when(jpaRepository.findTopByOrderByMigratedDateDesc()).thenReturn(entity);

        // 테스트 실행
        String result = transferProjcet.NamingJiraKey();

        // 결과 검증
        //assertEquals("TWSS1", result); // 최초 삽입시
        assertEquals("TWSS4", result);
    }

   /* @Test
    public void 이관데이터_테이블_조회() throws Exception {
        // 모의 객체 동작 정의
        when(jpaRepository.count()).thenReturn(1L);

        // 테스트 실행
        String result = transferProjcet.NamingJiraKey();

        // 결과 검증
        assertEquals("WSS1", result);
    }*/
}
