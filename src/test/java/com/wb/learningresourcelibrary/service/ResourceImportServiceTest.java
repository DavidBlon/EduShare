package com.wb.learningresourcelibrary.service;

import com.wb.learningresourcelibrary.dto.ParsedResourceVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class ResourceImportServiceTest {

    @Autowired
    private ResourceImportService resourceImportService;

    @Test
    void parseTextKeepsFirstLinkWhenBlockContainsMultipleLinks() {
        List<ParsedResourceVo> parsed = resourceImportService.parseText("""
                一年级语文同步练习
                https://pan.example.com/resource
                更多资源见 https://example.com/more
                """);

        assertThat(parsed).hasSize(1);
        assertThat(parsed.get(0).getNetdiskLink()).isEqualTo("https://pan.example.com/resource");
    }

    @Test
    void parseTextDoesNotGuessSubjectCategoryWhenLevelIsUnknownAndSubjectIsDuplicated() {
        List<ParsedResourceVo> parsed = resourceImportService.parseText("""
                英语专项阅读训练
                https://pan.example.com/english
                """);

        assertThat(parsed).hasSize(1);
        assertThat(parsed.get(0).getSubject()).isEqualTo("英语");
        assertThat(parsed.get(0).getCategoryId()).isNull();
    }

    @Test
    void parseTextMatchesExamPaperRangeToLevelCategory() {
        List<ParsedResourceVo> parsed = resourceImportService.parseText("""
                1-6年级全科试卷
                https://pan.example.com/exams
                """);

        assertThat(parsed).hasSize(1);
        ParsedResourceVo vo = parsed.get(0);
        assertThat(vo.getGrade()).isEqualTo("1-6年级");
        assertThat(vo.getCategoryId()).isEqualTo(1L);
        assertThat(vo.getCategoryName()).isEqualTo("小学");
        assertThat(vo.getSubject()).isEqualTo("全科");
    }
}
