package kopo.aisw.springboot_basic.controller;

import kopo.aisw.springboot_basic.domain.Board;
import kopo.aisw.springboot_basic.domain.Criteria;
import kopo.aisw.springboot_basic.domain.FileInfo;
import kopo.aisw.springboot_basic.domain.PageDTO;
import kopo.aisw.springboot_basic.service.BoardService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 수정 : 게시글 목록 페이지 (페이징 처리)
    @GetMapping("/list")
    public String list(Criteria cri, Model model) {
        List<Board> boardList = boardService.getListWithPaging(cri);
        int total = boardService.getTotalCount();

        model.addAttribute("boards", boardList);
        model.addAttribute("pageMaker", new PageDTO(cri, total));

        return "board/list";
    }

    // 게시글 작성 페이지
    @GetMapping("/write")
    public String writeForm() {
        return "board/write";
    }

    // 게시글 저장
    @PostMapping("/write")
    public String write(@ModelAttribute Board board,
                        @RequestParam(value = "files", required = false) List<MultipartFile> files,
                        Authentication auth) {
        board.setUserId(auth.getName());
        boardService.saveBoard(board, files);
        return "redirect:/board/list";
    }

    // 게시글 상세 페이지
    @GetMapping("/view/{boardId}")
    public String view(@PathVariable Long boardId, Model model) {
        Board board = boardService.getBoardById(boardId);
        List<FileInfo> files = boardService.getFilesByBoardId(boardId);

        model.addAttribute("board", board);
        model.addAttribute("files", files);
        return "board/view";
    }

    // 게시글 수정 페이지
    @GetMapping("/edit/{boardId}")
    public String editForm(@PathVariable Long boardId, Model model) {
        Board board = boardService.getBoardById(boardId);
        List<FileInfo> files = boardService.getFilesByBoardId(boardId);

        model.addAttribute("board", board);
        model.addAttribute("files", files);
        return "board/edit";
    }

    // 게시글 수정 (파일 포함)
    @PostMapping("/edit/{boardId}")
    public String edit(
        @PathVariable Long boardId,
        Board board,
        @RequestParam(value = "files", required = false) List<MultipartFile> files,
        @RequestParam(value = "deleteFiles", required = false) List<Long> deleteFileIds) {

        board.setBoardId(boardId);

        try {
            boardService.updateBoard(board, files, deleteFileIds);
            return "redirect:/board/view/" + boardId;
        } catch (Exception e) {
            System.out.println("게시글 수정 실패: " + e.getMessage());
            return "redirect:/board/edit/" + boardId;
        }
    }

    // 게시글 삭제
    @PostMapping("/delete/{boardId}")
    public String delete(@PathVariable Long boardId) {
        boardService.deleteBoard(boardId);
        return "redirect:/board/list";
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        return boardService.downloadFile(fileId);
    }

    // 파일 삭제
    @PostMapping("/file/delete/{fileId}")
    @ResponseBody
    public ResponseEntity<String> deleteFile(@PathVariable Long fileId) {
        try {
            boardService.deleteFile(fileId);
            return ResponseEntity.ok("파일이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("파일 삭제 실패: " + e.getMessage());
        }
    }

}
