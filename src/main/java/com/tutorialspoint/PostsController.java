package com.tutorialspoint;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.ModelMap;

@Controller
@RequestMapping("/get-posts")
public class PostsController {
    @RequestMapping(method = RequestMethod.GET)
    public String getPosts(ModelMap model) {
        //model.addAttribute("message", "Hello Spring MVC Framework!");
        return "posts";
    }
}

