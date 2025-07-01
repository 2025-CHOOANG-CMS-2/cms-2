package kr.ac.dhuniv.admin.service;

import jakarta.annotation.PostConstruct; // ⭐ PostConstruct 임포트 추가 ⭐
import jakarta.persistence.EntityNotFoundException;
import kr.ac.dhuniv.notice.domain.Attachment;
import kr.ac.dhuniv.notice.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor // ⭐ 이 어노테이션으로 생성자 주입을 처리합니다. ⭐
@Transactional(readOnly = true)
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    // 파일 저장 경로 (실제 환경에서는 외부 설정 파일 등에서 관리하는 것이 좋습니다.)
    // 현재는 임시로 프로젝트 루트의 'uploads' 폴더에 저장
    private final String UPLOAD_DIR = "uploads/";

    // ⭐ 생성자 대신 @PostConstruct 메서드를 사용하여 디렉토리 생성 ⭐
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    /**
     * 파일을 저장하고 Attachment 엔티티를 생성합니다.
     * @param file 업로드할 파일
     * @return 저장된 Attachment 엔티티
     * @throws IOException 파일 저장 중 오류 발생 시
     */
    @Transactional
    public Attachment uploadAttachment(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + fileExtension; // 고유한 파일 이름 생성
        Path filePath = Paths.get(UPLOAD_DIR + fileName);

        Files.copy(file.getInputStream(), filePath); // 파일 저장

        Attachment attachment = Attachment.builder()
                .uuid(UUID.randomUUID().toString()) // DB에는 String으로 저장
                .originalFileName(originalFileName)
                .filePath(filePath.toString()) // filePath 필드 값 설정
                .fileSize(file.getSize())
                .mimeType(file.getContentType()) // mimeType 필드 값 설정
                .build();

        return attachmentRepository.save(attachment);
    }

    /**
     * UUID로 첨부파일을 조회합니다.
     * @param uuid 첨부파일 UUID
     * @return 조회된 Attachment 엔티티
     */
    public Attachment getAttachmentByUuid(String uuid) {
        return attachmentRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("해당 UUID의 첨부파일을 찾을 수 없습니다: " + uuid));
    }

    /**
     * 첨부파일을 삭제합니다.
     * @param uuid 삭제할 첨부파일 UUID
     */
    @Transactional
    public void deleteAttachment(String uuid) {
        Attachment attachment = attachmentRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("해당 UUID의 첨부파일을 찾을 수 없습니다: " + uuid));
        
        // 실제 파일 시스템에서도 삭제
        try {
            Files.deleteIfExists(Paths.get(attachment.getFilePath()));
        } catch (IOException e) {
            // 파일 시스템에서 삭제 실패해도 DB에서는 삭제 진행 (로그 기록)
            System.err.println("Failed to delete file from filesystem: " + attachment.getFilePath() + " - " + e.getMessage());
        }
        
        attachmentRepository.delete(attachment);
    }
}
