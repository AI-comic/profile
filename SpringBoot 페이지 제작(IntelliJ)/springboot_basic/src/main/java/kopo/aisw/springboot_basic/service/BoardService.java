package kopo.aisw.springboot_basic.service;

import kopo.aisw.springboot_basic.domain.Board;
import kopo.aisw.springboot_basic.domain.FileInfo;
import kopo.aisw.springboot_basic.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import kopo.aisw.springboot_basic.domain.Criteria;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.util.UUID;
import java.nio.file.Paths;
import java.nio.file.Path;
import org.springframework.core.io.UrlResource;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    @Value("${file.upload.directory}")
    private String uploadDirectory;

    @PostConstruct
    public void init() {
        try {
            // 상대 경로를 절대 경로로 변환
            Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            System.out.println("File upload directory created: " + uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    // 게시글 저장
    public void saveBoard(Board board, List<MultipartFile> files) {
        // 게시글 저장
        boardMapper.save(board);

        // board_id를 가져오기 위한 로직 추가
        Long boardId = board.getBoardId();

        // 파일 처리
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        String originalFilename = file.getOriginalFilename();
                        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

                        // 상대 경로를 절대 경로로 변환
                        Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
                        Path filePath = uploadPath.resolve(uniqueFilename).normalize();

                        // 파일 저장
                        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                        // 파일 정보 저장
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.setBoardId(boardId);  // 저장된 게시글의 ID 설정
                        fileInfo.setOriginalFileName(originalFilename);
                        fileInfo.setSavedFileName(uniqueFilename);
                        fileInfo.setFilePath(uploadPath.toString());
                        fileInfo.setFileSize(file.getSize());
                        fileInfo.setFileType(file.getContentType());
                        fileInfo.setRegDate(LocalDateTime.now());

                        boardMapper.saveFile(fileInfo);

                    } catch (IOException e) {
                        throw new RuntimeException("파일 저장 중 오류 발생", e);
                    }
                }
            }
        }
    }

    // 게시글 상세 조회
    public Board getBoardById(Long boardId) {
        // 조회수 증가
        boardMapper.updateViewCount(boardId);
        return boardMapper.findById(boardId);
    }

    // 게시글 목록 조회
    public List<Board> getAllBoards() {
        return boardMapper.findAll();
    }

    // 게시글 수정 (파일 포함)
    @Transactional
    public void updateBoard(Board board, List<MultipartFile> newFiles, List<Long> deleteFileIds) {
        // 기존 파일 삭제 처리
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            for (Long fileId : deleteFileIds) {
                FileInfo fileInfo = boardMapper.findFileById(fileId);
                if (fileInfo != null) {
                    // 물리적 파일 삭제
                    try {
                        Path filePath = Paths.get(fileInfo.getFilePath(), fileInfo.getSavedFileName());
                        Files.deleteIfExists(filePath);
                    } catch (IOException e) {
                        throw new RuntimeException("파일 삭제 중 오류 발생", e);
                    }
                    // DB에서 파일 정보 삭제
                    boardMapper.deleteFile(fileId);
                }
            }
        }

        // 새로운 파일 추가
        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                if (!file.isEmpty()) {
                    saveFile(board.getBoardId(), file);
                }
            }
        }

        // 게시글 정보 업데이트
        boardMapper.update(board);
    }

    // 게시글 삭제 (파일 포함)
    @Transactional
    public void deleteBoard(Long boardId) {
        // 첨부 파일 조회
        List<FileInfo> files = boardMapper.findFilesByBoardId(boardId);

        // 물리적 파일 삭제
        for (FileInfo file : files) {
            try {
                Path filePath = Paths.get(file.getFilePath(), file.getSavedFileName());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new RuntimeException("파일 삭제 중 오류 발생", e);
            }
        }

        // DB에서 파일 정보 삭제
        boardMapper.deleteFilesByBoardId(boardId);

        // 게시글 삭제
        boardMapper.delete(boardId);
    }

    // 단일 파일 삭제
    public void deleteFile(Long fileId) {
        FileInfo fileInfo = boardMapper.findFileById(fileId);
        if (fileInfo != null) {
            try {
                Path filePath = Paths.get(fileInfo.getFilePath(), fileInfo.getSavedFileName());
                Files.deleteIfExists(filePath);
                boardMapper.deleteFile(fileId);
            } catch (IOException e) {
                throw new RuntimeException("파일 삭제 중 오류 발생", e);
            }
        }
    }

    // 페이징 처리 코드 추가
    public List<Board> getListWithPaging(Criteria cri) {
        return boardMapper.getListWithPaging(cri);
    }

    public int getTotalCount() {
        return boardMapper.getTotalCount();
    }

    public ResponseEntity<Resource> downloadFile(Long fileId) {
        FileInfo fileInfo = boardMapper.findFileById(fileId);
        if (fileInfo == null) {
            throw new RuntimeException("파일을 찾을 수 없습니다.");
        }

        try {
            Path filePath = Paths.get(fileInfo.getFilePath(), fileInfo.getSavedFileName());
            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileInfo.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("파일 다운로드 중 오류 발생", e);
        }
    }

    public List<FileInfo> getFilesByBoardId(Long boardId) {
        return boardMapper.findFilesByBoardId(boardId);
    }

    // 단일 파일 저장 메서드
    private void saveFile(Long boardId, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

            // 상대 경로를 절대 경로로 변환
            Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(uniqueFilename).normalize();

            // 파일 저장
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 파일 정보 저장
            FileInfo fileInfo = new FileInfo();
            fileInfo.setBoardId(boardId);
            fileInfo.setOriginalFileName(originalFilename);
            fileInfo.setSavedFileName(uniqueFilename);
            fileInfo.setFilePath(uploadPath.toString());
            fileInfo.setFileSize(file.getSize());
            fileInfo.setFileType(file.getContentType());

            boardMapper.saveFile(fileInfo);

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생: " + file.getOriginalFilename(), e);
        }
    }
}
