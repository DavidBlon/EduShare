package com.wb.learningresourcelibrary.controller.admin;

import com.wb.learningresourcelibrary.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadControllerTest {

    @Test
    void uploadCoverRejectsFilenameWithoutSuffix() {
        UploadController controller = new UploadController();
        ReflectionTestUtils.setField(controller, "maxSize", 10_485_760L);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover",
                "image/png",
                "fake-image".getBytes()
        );

        assertThatThrownBy(() -> controller.uploadCover(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("文件格式不正确");
    }
}
