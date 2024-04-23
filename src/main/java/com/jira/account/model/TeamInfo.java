package com.jira.account.model;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public enum TeamInfo {

    TEAM0("기술본부 응용정보기술센터", "0", Arrays.asList("임선정")),
    TEAM1("기술본부 응용정보기술센터 1파트", "1", Arrays.asList("김선근", "강인환", "김태헌", "최수현")),
    TEAM2("기술본부 응용정보기술센터 2파트", "2", Arrays.asList("이호섭", "이선민", "황희원")),
    TEAM3("기술본부 응용정보기술센터 3파트", "3", Arrays.asList("최정오", "강창주", "김찬호"));
    
    private final String team;
    private final String part;
    private final List<String> members;
    
    TeamInfo(String team, String part, List<String> members) {
        this.team = team;
        this.part = part;
        this.members = members;
    }

    public String getTeam() {
        return team;
    }

    public String getPart() {
        return part;
    }
    public List<String> getMembers() {
        return members;
    }
    
    // 이름으로 팀, 파트 정보 가져오기
    public static TeamInfo findInfoByName(String name) {
        for (TeamInfo teamInfo : TeamInfo.values()) {
            if (teamInfo.getMembers().contains(name)) {
                return teamInfo;
            }
        }
        return null; // 사용자 찾을 수 없는 경우
    }
}
