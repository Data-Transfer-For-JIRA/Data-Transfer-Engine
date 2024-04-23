package com.jira.account.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class 전자문서사업부DTO {

    private String 사업부장;
    private List<사업개발팀> 사업개발팀;

    private List<PIO1> PIO1;

    private List<PIO2> PIO2;


    public class 사업개발팀{
        private String 팀장;
        private List<String> 팀원;
    }


    public class PIO1{
        private String 팀장;
        private List<String> 팀원;
    }

    public class PIO2{
        private String 팀장;
        private PIO2_1파트 PIO2_1파트;
        private PIO2_2파트 PIO2_2파트;
        private PIO2_3파트 PIO2_3파트;
    }

    public class PIO2_1파트{
        private String 파트장;
        private List<String> 팀원;

    }

    public class PIO2_2파트{
        private String 파트장;
        private List<String> 팀원;

    }

    public class PIO2_3파트{
        private String 파트장;
        private List<String> 팀원;

    }
}
