package com.wanderlust.api.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardingController {

    @RequestMapping(value = {"/", "/feed", "/explore/**", "/post/**", "/profile/**", "/itinerary/**", "/bookmarks", "/auth/**"})
    public String forward() {
        return "forward:/index.html";
    }
}
