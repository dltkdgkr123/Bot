package com.sh.bdt.thymeleaf.controller;

import com.sh.bdt.thymeleaf.dto.req.PostViewDetailRequest;
import com.sh.bdt.thymeleaf.service.PostViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostViewController {

    private final PostViewService postViewService;

    @GetMapping("/{postId}")
    public String getPostViewDetail(Model model, @PathVariable Long postId, @ModelAttribute PostViewDetailRequest postViewDetailRequest) {
        // WARNING: get userId from query string(not token, ..), skip user auth validate for experiment
        model.addAttribute("post", postViewService.getPostViewDetail(postId, postViewDetailRequest));
        return "post/detail";
    }

}
