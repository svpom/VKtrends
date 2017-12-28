package com.tutorialspoint;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.ModelMap;
import javax.servlet.http.*;

@Controller
@RequestMapping("/get-posts")
public class PostsController {
    @RequestMapping(method = RequestMethod.GET)
    public String getPosts(ModelMap model, HttpServletRequest request, HttpServletResponse respone) {
        String text = request.getParameter("input-name");
        model.addAttribute("message", text);
        return "posts";
    }
}

