package kr.ac.dhuniv.admin.api_controller;

import kr.ac.dhuniv.admin.service.AttachmentService;
import kr.ac.dhuniv.notice.domain.Attachment;
import kr.ac.dhuniv.notice.dto.AttachmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * 파일 업로드 API
     * POST /api/admin/attachments/upload
     * @param file 업로드할 파일
     * @return 업로드된 파일 정보 (AttachmentResponse)
     */
    @PostMapping("/upload")
    public ResponseEntity<AttachmentResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Attachment attachment = attachmentService.uploadAttachment(file); // ⭐ saveFile -> uploadAttachment ⭐
            return ResponseEntity.status(HttpStatus.CREATED).body(AttachmentResponse.from(attachment));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 파일 없음 등
        }
    }

    /**
     * 파일 다운로드 API
     * GET /api/admin/attachments/download/{uuid}
     * @param uuid 다운로드할 파일의 UUID
     * @return 파일 (Resource)
     */
    @GetMapping("/download/{uuid}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("uuid") String uuid) {
        try {
            Attachment attachment = attachmentService.getAttachmentByUuid(uuid);
            Path filePath = Paths.get(attachment.getFilePath()); // attachmentService.getUploadDir() 제거
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = attachment.getMimeType();
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 파일 삭제 API
     * DELETE /api/admin/attachments/{uuid}
     * @param uuid 삭제할 파일의 UUID
     * @return 응답 상태
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteFile(@PathVariable("uuid") String uuid) {
        try {
            attachmentService.deleteAttachment(uuid); // ⭐ deleteAttachmentByUuid -> deleteAttachment ⭐
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
