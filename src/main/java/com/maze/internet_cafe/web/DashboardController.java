package com.maze.internet_cafe.web;

import com.maze.internet_cafe.computer.ComputerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ComputerService computerService;

    public DashboardController(ComputerService computerService) {
        this.computerService = computerService;
    }

    @GetMapping({"/","/dashboard"})
    public String dashboard(Model model) {
        // provide initial list of computers to render
        model.addAttribute("computers", computerService.list(null, org.springframework.data.domain.Pageable.unpaged()).getContent());
        return "index";
    }
}

