package kopo.aisw.springboot_basic.controller;

import kopo.aisw.springboot_basic.domain.User;
import kopo.aisw.springboot_basic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/signup")
    public String signupForm() {
        return "user/signup";
    }

    @PostMapping("/signup")
    public String signup(User user, RedirectAttributes ra) {
        try{
            //아이디 중복 체크
            if (userService.isUserIdExists(user.getUserId())) {
                ra.addFlashAttribute("error", "이미 존재하는 아이디입니다.");
                return "redirect:/user/signup";
            }

            userService.registerUser(user);
            return "redirect:/user/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "회원 가입 중 오류가 발생했습니다.");
            return "redirect:/user/signup";
        }
    }

    @GetMapping("/login")
    public String loginForm() {
        return "user/login";
    }

    @GetMapping("/mypage")
    public String myPage(Authentication auth, Model model) {
        User user = userService.getUserById(auth.getName());
        model.addAttribute("user", user);
        return "user/mypage";
    }

    @PostMapping("/update")
    public String updateUser(User user) {
        userService.updateUser(user);
        return "redirect:/user/mypage";
    }

    @PostMapping("/delete")
    public String deleteUser(Authentication auth) {
        userService.deleteUser(auth.getName());
        return "redirect:/user/logout";
    }
}
