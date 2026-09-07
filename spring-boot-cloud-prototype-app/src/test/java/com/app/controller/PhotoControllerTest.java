package com.app.controller;

import com.app.pojo.CursorPageResult;
import com.app.pojo.dto.PhotoDto;
import com.app.pojo.vo.PhotoVo;
import com.app.service.PhotoService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 相册查询控制器测试
 *
 * @author yanlei
 * @since 2026-09-06
 */
class PhotoControllerTest {

    @Test
    void shouldExposeAlbumQueryDetailAndOriginalDownloadEndpoints() throws Exception {
        PhotoService service = mock(PhotoService.class);
        PhotoDto.ListQuery query = new PhotoDto.ListQuery().setFilter("TODAY").setSize(20);
        PhotoVo.ListItem item = new PhotoVo.ListItem().setPhotoNo("PHO-1")
                .setThumbnailUrl("https://oss.example/thumb").setUploadedTime(LocalDateTime.of(2026, 9, 6, 1, 2));
        when(service.listPhotos(any(PhotoDto.ListQuery.class))).thenReturn(CursorPageResult.of(List.of(item), true,
                Instant.parse("2026-09-06T01:02:00Z"), 9L));
        when(service.getPhotoDetail("PHO-1")).thenReturn(new PhotoVo.Detail().setPhotoNo("PHO-1")
                .setPreviewUrl("https://oss.example/preview"));
        when(service.getOriginalDownloadUrl("PHO-1")).thenReturn(new PhotoVo.DownloadUrl()
                .setDownloadUrl("https://oss.example/original"));
        PhotoController controller = new PhotoController();
        ReflectionTestUtils.setField(controller, "photoService", service);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(get("/api/photo/listPhotos").param("filter", "TODAY").param("size", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].photoNo").value("PHO-1"))
                .andExpect(jsonPath("$.data.items[0].thumbnailUrl").value("https://oss.example/thumb"))
                .andExpect(jsonPath("$.data.hasMore").value(true)).andExpect(jsonPath("$.data.nextCursorId").value(9));
        mvc.perform(get("/api/photo/getPhotoDetail").param("photoNo", "PHO-1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.previewUrl").value("https://oss.example/preview"));
        mvc.perform(get("/api/photo/getOriginalDownloadUrl").param("photoNo", "PHO-1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.downloadUrl").value("https://oss.example/original"));

        verify(service).listPhotos(query);
        verify(service).getPhotoDetail("PHO-1");
        verify(service).getOriginalDownloadUrl("PHO-1");
    }
}
